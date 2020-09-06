package com.myownb3.dominic.tarifziffer.core.datacleaning.impl;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.invoice.attrs.model.impl.MutableInvoiceAttrFactory;
import com.myownb3.dominic.tarifziffer.core.datacleaning.InvoiceDataCleaner;
import com.myownb3.dominic.tarifziffer.core.export.ContentUtil;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.XMLContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.LineContentImpl;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLContentImpl;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

public class InvoiceDataCleanerImpl implements InvoiceDataCleaner {

   private static final Logger LOG = Logger.getLogger(InvoiceDataCleanerImpl.class);
   private boolean isRawExport;

   public InvoiceDataCleanerImpl(boolean isRawExport) {
      this.isRawExport = isRawExport;
   }

   @Override
   public List<XMLFileParseResult> purgeResults(List<XMLFileParseResult> result) {
      LOG.info("Start purging the results..");
      return new ArrayList<>(result)
            .parallelStream()
            .filter(XMLFileParseResult::hasContent)
            .map(mergeHeaderContentLinesFromSameType())
            .map(addMissingValues())
            .sorted(Comparator.comparing(XMLFileParseResult::getContentSize).reversed())
            .collect(Collectors.toList());
   }

   /*
    * Collects all header-InvoiceAttrs from the same ContentType which are distributed in more than one LineContent.
    * Those attrs are then added to a single LineContent  
    */
   private Function<XMLFileParseResult, XMLFileParseResult> mergeHeaderContentLinesFromSameType() {
      return xmlFileParseResult -> {
         XMLContent xmlContent = new XMLContentImpl();
         Map<ContentType, List<LineContent>> headerContentType2ContentMap = fillUpHeaderContentType2ContentLinesMap(xmlFileParseResult);
         mergeAndAddHeaderContent(headerContentType2ContentMap, xmlContent);
         addServicesData(xmlFileParseResult, xmlContent);
         return XMLFileParseResult.of(xmlFileParseResult.getXMLFileName(), xmlContent);
      };
   }

   private static void mergeAndAddHeaderContent(Map<ContentType, List<LineContent>> contentType2ContentMap, XMLContent xmlContent) {
      contentType2ContentMap.entrySet()
            .stream()
            .map(collectInvoiceAttrs4SameType())
            .map(LineContentImpl::new)
            .forEach(xmlContent::add);
   }

   private static void addServicesData(XMLFileParseResult xmlFileParseResult, XMLContent xmlContent) {
      ContentUtil.getServicesLineContent(xmlFileParseResult)
            .stream()
            .forEach(xmlContent::add);
   }

   private static Map<ContentType, List<LineContent>> fillUpHeaderContentType2ContentLinesMap(XMLFileParseResult xmlFileParseResult) {
      Map<ContentType, List<LineContent>> contentType2ContentMap = new EnumMap<>(ContentType.class);
      for (LineContent lineContent : ContentUtil.getInvoiceHeaderContent(xmlFileParseResult)) {
         ContentType contentType = lineContent.getContentType();
         List<LineContent> lineContent4Type = contentType2ContentMap.getOrDefault(contentType, new ArrayList<>());
         lineContent4Type.add(lineContent);
         contentType2ContentMap.put(contentType, lineContent4Type);
      }
      return contentType2ContentMap;
   }

   private static Function<Entry<ContentType, List<LineContent>>, List<InvoiceAttr>> collectInvoiceAttrs4SameType() {
      return entry -> entry.getValue()
            .stream()
            .map(LineContent::getInvoiceAttrs)
            .flatMap(List::stream)
            .collect(Collectors.toList());
   }

   private Function<XMLFileParseResult, XMLFileParseResult> addMissingValues() {
      return xmlFileParseResult -> {
         XMLContent xmlContent = buildNewXMLWithMissingContent(xmlFileParseResult);
         return XMLFileParseResult.of(xmlFileParseResult.getXMLFileName(), xmlContent);
      };
   }

   private XMLContent buildNewXMLWithMissingContent(XMLFileParseResult xmlFileParseResult) {
      return new XMLContentImpl(xmlFileParseResult.getContent()
            .stream()
            .map(addMissingValue4Line())
            .map(fixInvalidValues(xmlFileParseResult))
            .collect(Collectors.toList()));
   }

   private Function<LineContent, LineContent> fixInvalidValues(XMLFileParseResult xmlFileParseResult) {
      return lineContent -> fixInvalidValues(xmlFileParseResult, lineContent);
   }

   private static LineContent fixInvalidValues(XMLFileParseResult xmlFileParseResult, LineContent lineContent) {
      List<InvoiceAttr> currentParsedInvoiceAttrs = lineContent.getInvoiceAttrs();
      List<InvoiceAttr> fixedParsedInvoiceAttrs = new ArrayList<>(lineContent.getInvoiceAttrs().size());
      for (InvoiceAttr invoiceAttr : currentParsedInvoiceAttrs) {
         if (!invoiceAttr.isValueValid(invoiceAttr.getValue())) {
            List<InvoiceAttr> noneServiceData = getHeaderInvoiceAttrs(xmlFileParseResult);
            String newValue = invoiceAttr.getValidValue(invoiceAttr.getValue(), noneServiceData);
            fixedParsedInvoiceAttrs.add(MutableInvoiceAttrFactory.INSTANCE.createNewMutableInvoiceAttr(invoiceAttr, newValue));
            logAttributeWithWrongValue(invoiceAttr.getName(), invoiceAttr.getValue(), newValue);
         } else {
            fixedParsedInvoiceAttrs.add(invoiceAttr);
         }
      }
      return new LineContentImpl(fixedParsedInvoiceAttrs);
   }

   private static List<InvoiceAttr> getHeaderInvoiceAttrs(XMLFileParseResult xmlFileParseResult) {
      return ContentUtil.getInvoiceHeaderContent(xmlFileParseResult)
            .stream()
            .map(LineContent::getInvoiceAttrs)
            .flatMap(List::stream)
            .collect(Collectors.toList());
   }

   /*
    * Adds attributes whose are missing in the current line.
    * This is the case, if the corresponding xml-tag was an empty string
    */
   private Function<LineContent, LineContent> addMissingValue4Line() {
      return lineContent -> {
         List<String> allAttrNames = InvoiceAttrs.getAllInvoiceAttrsNames(lineContent.getContentType(), isRawExport);
         List<InvoiceAttr> parsedInvoiceAttrs = new ArrayList<>(lineContent.getInvoiceAttrs());
         for (String attrName : allAttrNames) {
            if (!hasLineValue4Key(lineContent, attrName)) {
               InvoiceAttr invoiceAttr = InvoiceAttrs.getInvoiceAttrByName(attrName);
               String defaultValue = getDefaultValue(invoiceAttr, lineContent);
               parsedInvoiceAttrs.add(MutableInvoiceAttrFactory.INSTANCE.createNewMutableInvoiceAttr(invoiceAttr, defaultValue));
               logMissingAttribute(attrName);
            }
         }
         return new LineContentImpl(parsedInvoiceAttrs);
      };
   }

   private static String getDefaultValue(InvoiceAttr invoiceAttr, LineContent lineContent) {
      if (isServicesAttrDateEnd(invoiceAttr)) {
         // Obviously it's normal that the end-date is missing. In cases like that, the end-date is equal than the begin date
         return lineContent.getValue(InvoiceAttrs.DATE_BEGIN.getName());
      }
      return invoiceAttr.getDefaultValue();
   }

   private static boolean isServicesAttrDateEnd(InvoiceAttr invoiceAttr) {
      return invoiceAttr.getName().equals(InvoiceAttrs.DATE_END.getName());
   }

   private static boolean hasLineValue4Key(LineContent lineContent, String key) {
      return nonNull(lineContent.getValue(key));
   }

   private static void logMissingAttribute(String attrName) {
      if (LOG.isInfoEnabled()) {
         LOG.info("Add missing value for attribute '" + attrName + "'");
      }
   }

   private static void logAttributeWithWrongValue(String attrName, String value, String newValue) {
      if (LOG.isInfoEnabled()) {
         LOG.info("Wrong value '" + value + "' for Attribute '" + attrName + "' was set. Use new value '" + newValue + "' instead");
      }
   }
}
