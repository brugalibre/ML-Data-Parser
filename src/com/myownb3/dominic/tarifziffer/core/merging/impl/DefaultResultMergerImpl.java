package com.myownb3.dominic.tarifziffer.core.merging.impl;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.NominalInvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.invoice.attrs.model.DoubleMutableInvoiceAttr;
import com.myownb3.dominic.invoice.attrs.model.impl.DoubleMutableInvoiceAttrImpl;
import com.myownb3.dominic.tarifziffer.core.export.ContentUtil;
import com.myownb3.dominic.tarifziffer.core.merging.ResultMerger;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.XMLContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.LineContentImpl;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLContentImpl;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;
import com.myownb3.dominic.tarifziffer.mlclassifier.MLClassifiers;

public class DefaultResultMergerImpl implements ResultMerger {

   private static final Logger LOG = Logger.getLogger(DefaultResultMergerImpl.class);
   private boolean isRawExport;
   private MLClassifiers classifier;

   public DefaultResultMergerImpl(boolean isRawExport, MLClassifiers classifier) {
      this.isRawExport = isRawExport;
      this.classifier = classifier;
   }

   @Override
   public List<XMLFileParseResult> mergeLineContent(List<XMLFileParseResult> result) {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start merging '" + result.size() + "' results");
      List<XMLFileParseResult> mergeLineContent = mergeLineContent0(result);
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done merging %s\n");
      return mergeLineContent;
   }

   private List<XMLFileParseResult> mergeLineContent0(List<XMLFileParseResult> result) {
      if (result.isEmpty()) {
         return result;
      }
      // For performance reasons: lets do the merging one time, since all files should have the same attributes, the length of this merged attribues should be the same for all files
      List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs = mergeParsedXMLFile2SingleLine(result.get(0));
      return result.parallelStream()
            .map(mergeXMLFileParseResult(mergedInvoiceAttrs))
            .collect(Collectors.toList());
   }

   private Function<XMLFileParseResult, XMLFileParseResult> mergeXMLFileParseResult(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrsIn) {
      return xmlFileParseResult -> {
         Map<String, DoubleMutableInvoiceAttr> mergedInvoiceAttrsMap = createCopy(mergedInvoiceAttrsIn);
         List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs = fillMergedInvoiceAttrs(mergedInvoiceAttrsMap, xmlFileParseResult);
         logFileMerged(xmlFileParseResult);
         return XMLFileParseResult.of(xmlFileParseResult.getXMLFileName(), buildXMLContent(mergedInvoiceAttrs));
      };
   }

   private static Map<String, DoubleMutableInvoiceAttr> createCopy(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrsIn) {
      return mergedInvoiceAttrsIn.parallelStream()
            .map(DoubleMutableInvoiceAttrImpl::copy)
            .collect(Collectors.toMap(InvoiceAttr::getName, Function.identity()));
   }

   private static XMLContent buildXMLContent(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs) {
      Predicate<InvoiceAttr> isServiceDataPredicate = invoiceAttr -> invoiceAttr.getContentType() == ContentType.SERVICES_DATA;
      LineContent nonServiceDataLineContent = new LineContentImpl(filter(mergedInvoiceAttrs, isServiceDataPredicate.negate()));
      LineContent serviceDataLineContent = new LineContentImpl(filter(mergedInvoiceAttrs, isServiceDataPredicate));
      return new XMLContentImpl(Arrays.asList(nonServiceDataLineContent, serviceDataLineContent));
   }

   private static List<DoubleMutableInvoiceAttr> filter(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs,
         Predicate<InvoiceAttr> isServiceDataPredicate) {
      return mergedInvoiceAttrs.parallelStream()
            .filter(isServiceDataPredicate)
            .collect(Collectors.toList());
   }

   /*
    * The merged InvoiceAttrs has to be filled, according to the occurrences of each InvoiceAttr within the XMLFileParseResult
    * This result then represents the XMLFileParseResult as one single line 
    */
   private List<DoubleMutableInvoiceAttr> fillMergedInvoiceAttrs(Map<String, DoubleMutableInvoiceAttr> mergedInvoiceAttrsMap,
         XMLFileParseResult xmlFileParseResult) {
      setInvoiceAttrValues(mergedInvoiceAttrsMap, xmlFileParseResult);
      scaleNonNominalValues(xmlFileParseResult, mergedInvoiceAttrsMap.values());
      return mergedInvoiceAttrsMap.values()
            .parallelStream()
            .collect(Collectors.toList());
   }

   private void setInvoiceAttrValues(Map<String, DoubleMutableInvoiceAttr> mergedInvoiceAttrsMap, XMLFileParseResult xmlFileParseResult) {
      ContentUtil.getAllInvoiceAttrs4File(xmlFileParseResult)
            .stream()
            .forEach(invoiceAttr -> setInvoiceAttrValue(mergedInvoiceAttrsMap, invoiceAttr));
   }

   private void setInvoiceAttrValue(Map<String, DoubleMutableInvoiceAttr> mergedInvoiceAttrs, InvoiceAttr invoiceAttr) {
      String attrName4Lookup = evalLookupName(invoiceAttr);
      Optional<DoubleMutableInvoiceAttr> mergedInvoiceAttrOpt = evalInvoiceAttr(attrName4Lookup, mergedInvoiceAttrs);
      incrementValueIfPresent(mergedInvoiceAttrOpt, invoiceAttr);
   }

   private static void scaleNonNominalValues(XMLFileParseResult xmlFileParseResult, Collection<DoubleMutableInvoiceAttr> filledMergedInvoiceAttrs) {
      Predicate<DoubleMutableInvoiceAttr> isNominal = InvoiceAttr::isNominal;
      filledMergedInvoiceAttrs.parallelStream()
            .filter(isNominal.negate())
            .filter(isServicesData())
            .forEach(divideValueBySize(xmlFileParseResult.getContentSize()));
   }

   private static Consumer<DoubleMutableInvoiceAttr> divideValueBySize(int contentSize) {
      return invoiceAttr -> divideValueBySize02(contentSize, invoiceAttr);
   }

   private static void divideValueBySize02(int contentSize, DoubleMutableInvoiceAttr invoiceAttr) {
      invoiceAttr.setValue(invoiceAttr.getTypedValue() / contentSize);
   }

   private static String evalLookupName(InvoiceAttr invoiceAttr) {
      return invoiceAttr.isNominal() ? ((NominalInvoiceAttr) invoiceAttr).buildCategoricalAttrName(invoiceAttr.getValue()) : invoiceAttr.getName();
   }

   private void incrementValueIfPresent(Optional<DoubleMutableInvoiceAttr> mergedInvoiceAttrOpt, InvoiceAttr invoiceAttr) {
      mergedInvoiceAttrOpt.ifPresent(mergedInvoiceAttr -> incrementValue(mergedInvoiceAttr, invoiceAttr));
   }

   private void incrementValue(DoubleMutableInvoiceAttr mergedInvoiceAttr, InvoiceAttr invoiceAttr) {
      // All occurrences of nominal values are incremented
      if (invoiceAttr.isNominal()) {
         double currentValue = mergedInvoiceAttr.getTypedValue();
         mergedInvoiceAttr.setValue(++currentValue);
      } else if (invoiceAttr.isDouble()) {
         // All others are added (and divided by the amount of total attrs in the end). The other value has to be double, otherwise we are in trouble!
         double otherValue = Double.parseDouble(invoiceAttr.getValue());
         mergedInvoiceAttr.setValue(mergedInvoiceAttr.getTypedValue() + otherValue);
      } else if (invoiceAttr.isInteger()) {
         // All others are added (and divided by the amount of total attrs in the end). The other value has to be double, otherwise we are in trouble!
         double otherValue = Integer.parseInt(invoiceAttr.getValue());
         mergedInvoiceAttr.setValue(mergedInvoiceAttr.getTypedValue() + otherValue);
      } else {
         LoggerHelper.INSTANCE.logIfEnabled(LOG,
               () -> "Attribute '" + invoiceAttr.getName() + "' is not merged because it's neither nominal nor floating point!", Level.WARN);
      }
   }

   private static Optional<DoubleMutableInvoiceAttr> evalInvoiceAttr(String name, Map<String, DoubleMutableInvoiceAttr> mergedInvoiceAttrs) {
      DoubleMutableInvoiceAttr doubleMutableInvoiceAttr = mergedInvoiceAttrs.get(name);
      return Optional.ofNullable(doubleMutableInvoiceAttr);
   }

   /*
    * Evaluates for the given parsed invoice a list of merged InvoiceAttrs which represents all single lines 
    */
   private List<DoubleMutableInvoiceAttr> mergeParsedXMLFile2SingleLine(XMLFileParseResult xmlFileParseResult) {
      if (!xmlFileParseResult.hasContent()) {
         return Collections.emptyList();
      }
      List<LineContent> invoiceHeaderContent = ContentUtil.getInvoiceHeaderContent(xmlFileParseResult);
      LineContent servicesLineContent = ContentUtil.getServicesLineContent(xmlFileParseResult).get(0);// We take the first, since all lines must contains all attributes!
      List<InvoiceAttr> invoiceHeaderWithoutNominalAttrs = mapContentLines2Attrs(invoiceHeaderContent, servicesLineContent);
      return InvoiceAttrs.INSTANCE.evalAllInvoiceAttrsIncludingCategoricalValues(invoiceHeaderWithoutNominalAttrs, false)
            .parallelStream()
            .map(createNewDoubleMutableInvoiceAttrImpl())
            .collect(Collectors.toList());
   }

   private Function<InvoiceAttr, DoubleMutableInvoiceAttr> createNewDoubleMutableInvoiceAttrImpl() {
      ToDoubleFunction<InvoiceAttr> defaultValueFunction = getDefaultValueFunction();
      return invoiceAttr -> DoubleMutableInvoiceAttrImpl.of(invoiceAttr, defaultValueFunction.applyAsDouble(invoiceAttr));
   }

   private ToDoubleFunction<InvoiceAttr> getDefaultValueFunction() {
      return invoiceAttr -> {
         if (invoiceAttr.isNominal()) {
            // In order to avoid the zero-frequency-problem
            return classifier == MLClassifiers.NAIVE_BAYES ? 1.0 : 0.0;
         }
         return 0.0;
      };
   }

   private List<InvoiceAttr> mapContentLines2Attrs(List<LineContent> invoiceHeaderContent, LineContent servicesLineContent) {
      invoiceHeaderContent.add(servicesLineContent);
      return invoiceHeaderContent.parallelStream()
            .map(LineContent::getInvoiceAttrs)
            .flatMap(List::stream)
            .filter(filterInvoiceAttr())
            .collect(Collectors.toList());
   }

   private Predicate<InvoiceAttr> filterInvoiceAttr() {
      return invoiceAttr -> !isRawExport || invoiceAttr.isRelevant4Vectorizing();
   }

   private static Predicate<InvoiceAttr> isServicesData() {
      return invoiceAttr -> invoiceAttr.getContentType() == ContentType.SERVICES_DATA;
   }

   private static void logFileMerged(XMLFileParseResult xmlFileParseResult) {
      LoggerHelper.INSTANCE.logIfEnabled(LOG,
            () -> "Done merging file '" + xmlFileParseResult.getXMLFileName() + "'", Level.DEBUG);
   }
}
