package com.myownb3.dominic.tarifziffer.core.merging.impl;


import static com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs.evalAllInvoiceAttrsIncludingCategoricalValues;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.NominalInvoiceAttr;
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

public class DefaultResultMergerImpl implements ResultMerger {

   private static final Logger LOG = Logger.getLogger(DefaultResultMergerImpl.class);
   private boolean isRawExport;

   public DefaultResultMergerImpl(boolean isRawExport) {
      this.isRawExport = isRawExport;
   }

   @Override
   public List<XMLFileParseResult> mergeLineContent(List<XMLFileParseResult> result) {
      if (result.isEmpty()) {
         return result;
      }
      // For performance reasons: lets do the merging one time, since all files should have the same attributes, the length of this merged attribues should be the same for all files
      List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs = mergeParsedXMLFile2SingleLine(result.get(0));
      return result.stream()
            .map(mergeXMLFileParseResult(mergedInvoiceAttrs))
            .collect(Collectors.toList());
   }

   private Function<XMLFileParseResult, XMLFileParseResult> mergeXMLFileParseResult(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrsIn) {
      return xmlFileParseResult -> {
         List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs = createCopy(mergedInvoiceAttrsIn);
         mergedInvoiceAttrs = fillMergedInvoiceAttrs(mergedInvoiceAttrs, xmlFileParseResult);
         return XMLFileParseResult.of(xmlFileParseResult.getXMLFileName(), buildXMLContent(mergedInvoiceAttrs));
      };
   }

   private List<DoubleMutableInvoiceAttr> createCopy(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrsIn) {
      return mergedInvoiceAttrsIn.stream()
            .map(DoubleMutableInvoiceAttr::getInvoiceAttr)
            .map(DoubleMutableInvoiceAttrImpl::of)
            .collect(Collectors.toList());
   }

   private static XMLContent buildXMLContent(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs) {
      Predicate<InvoiceAttr> isServiceDataPredicate = invoiceAttr -> invoiceAttr.getContentType() == ContentType.SERVICES_DATA;
      LineContent nonServiceDataLineContent = new LineContentImpl(filter(mergedInvoiceAttrs, isServiceDataPredicate.negate()));
      LineContent serviceDataLineContent = new LineContentImpl(filter(mergedInvoiceAttrs, isServiceDataPredicate));
      return new XMLContentImpl(Arrays.asList(nonServiceDataLineContent, serviceDataLineContent));
   }

   private static List<DoubleMutableInvoiceAttr> filter(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs,
         Predicate<InvoiceAttr> isServiceDataPredicate) {
      return mergedInvoiceAttrs.stream()
            .filter(isServiceDataPredicate)
            .collect(Collectors.toList());
   }

   /*
    * The merged InvoiceAttrs has to be filled, according to the occurrences of each InvoiceAttr within the XMLFileParseResult
    * This result then represents the XMLFileParseResult as one single line 
    */
   private List<DoubleMutableInvoiceAttr> fillMergedInvoiceAttrs(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs,
         XMLFileParseResult xmlFileParseResult) {
      evalAllInvoiceAttrs(xmlFileParseResult).stream()
            .forEach(invoiceAttr -> setInvoiceAttrValue(mergedInvoiceAttrs, invoiceAttr));
      scaleNonNominalValues(xmlFileParseResult, mergedInvoiceAttrs);
      return mergedInvoiceAttrs;
   }

   private static void setInvoiceAttrValue(List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs, InvoiceAttr invoiceAttr) {
      String attrName4Lookup = evalLookupName(invoiceAttr);
      Optional<DoubleMutableInvoiceAttr> mergedInvoiceAttrOpt = evalInvoiceAttr(attrName4Lookup, mergedInvoiceAttrs);
      incrementValueIfPresent(mergedInvoiceAttrOpt, invoiceAttr);
   }

   private void scaleNonNominalValues(XMLFileParseResult xmlFileParseResult, List<DoubleMutableInvoiceAttr> filledMergedInvoiceAttrs) {
      Predicate<DoubleMutableInvoiceAttr> isNominal = InvoiceAttr::isNominal;
      filledMergedInvoiceAttrs.stream()
            .filter(isNominal.negate())
            .filter(isServicesData())
            .forEach(divideValueBySize(xmlFileParseResult.getContentSize()));
   }

   private static Consumer<DoubleMutableInvoiceAttr> divideValueBySize(int contentSize) {
      return invoiceAttr -> invoiceAttr.setValue(invoiceAttr.getTypedValue() / contentSize);
   }

   private static String evalLookupName(InvoiceAttr invoiceAttr) {
      return invoiceAttr.isNominal() ? ((NominalInvoiceAttr) invoiceAttr).buildAttrName(invoiceAttr.getValue())
            : invoiceAttr.getName();
   }

   private static List<InvoiceAttr> evalAllInvoiceAttrs(XMLFileParseResult xmlFileParseResult) {
      return xmlFileParseResult.getContent()
            .stream()
            .map(LineContent::getInvoiceAttrs)
            .flatMap(List::stream)
            .collect(Collectors.toList());
   }

   private static void incrementValueIfPresent(Optional<DoubleMutableInvoiceAttr> mergedInvoiceAttrOpt, InvoiceAttr invoiceAttr) {
      mergedInvoiceAttrOpt.ifPresent(mergedInvoiceAttr -> incrementValue(mergedInvoiceAttr, invoiceAttr));
   }

   private static void incrementValue(DoubleMutableInvoiceAttr mergedInvoiceAttr, InvoiceAttr invoiceAttr) {
      // All occurrences of nominal values are incremented
      double currentValue = mergedInvoiceAttr.getTypedValue();
      if (invoiceAttr.isNominal()) {
         mergedInvoiceAttr.setValue(++currentValue);
      } else if (invoiceAttr.isDouble()) {
         // All others are added (and divided by the amount of total attrs in the end). The other value has to be double, otherwise we are in trouble!
         double otherValue = Double.parseDouble(invoiceAttr.getValue());
         mergedInvoiceAttr.setValue(currentValue + otherValue);
      } else if (invoiceAttr.isInteger()) {
         // All others are added (and divided by the amount of total attrs in the end). The other value has to be double, otherwise we are in trouble!
         double otherValue = Integer.parseInt(invoiceAttr.getValue());
         mergedInvoiceAttr.setValue(currentValue + otherValue);
      } else {
         LOG.warn("Attribute '" + invoiceAttr.getName() + "' is not merged because it's neither nominal nor floating point!");
      }
   }

   private static Optional<DoubleMutableInvoiceAttr> evalInvoiceAttr(String name, List<DoubleMutableInvoiceAttr> mergedInvoiceAttrs) {
      return mergedInvoiceAttrs.stream()
            .filter(invoiceAttr -> invoiceAttr.getName().equals(name))
            .findFirst();
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
      List<InvoiceAttr> evalInvoiceAttrs4CategoricalValues = evalAllInvoiceAttrsIncludingCategoricalValues(invoiceHeaderWithoutNominalAttrs);
      return evalInvoiceAttrs4CategoricalValues.stream()
            .map(DoubleMutableInvoiceAttrImpl::of)
            .collect(Collectors.toList());
   }

   private List<InvoiceAttr> mapContentLines2Attrs(List<LineContent> invoiceHeaderContent, LineContent servicesLineContent) {
      List<InvoiceAttr> mergedInvoicesHeaderAttrs = new ArrayList<>();
      invoiceHeaderContent.add(servicesLineContent);
      invoiceHeaderContent.stream()
            .map(LineContent::getInvoiceAttrs)
            .flatMap(List::stream)
            .filter(filterInvoiceAttr())
            .forEach(mergedInvoicesHeaderAttrs::add);
      Collections.sort(mergedInvoicesHeaderAttrs, Comparator.comparing(InvoiceAttr::getSequence));
      return mergedInvoicesHeaderAttrs;
   }

   private Predicate<InvoiceAttr> filterInvoiceAttr() {
      return invoiceAttr -> !isRawExport || invoiceAttr.isRelevant4Vectorizing();
   }

   private Predicate<InvoiceAttr> isServicesData() {
      return invoiceAttr -> invoiceAttr.getContentType() == ContentType.SERVICES_DATA;
   }
}
