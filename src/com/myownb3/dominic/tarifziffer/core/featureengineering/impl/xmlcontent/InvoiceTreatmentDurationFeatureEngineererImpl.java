package com.myownb3.dominic.tarifziffer.core.featureengineering.impl.xmlcontent;

import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.TREATMENT_DATA_DURATION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants;
import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.model.DoubleMutableInvoiceAttr;
import com.myownb3.dominic.tarifziffer.core.export.ContentUtil;
import com.myownb3.dominic.tarifziffer.core.featureengineering.impl.AbstractFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.featureengineering.xmlcontent.XMLContentFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.XMLContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLContentImpl;

public class InvoiceTreatmentDurationFeatureEngineererImpl extends AbstractFeatureEngineerer implements XMLContentFeatureEngineerer {

   @Override
   public XMLContent doFeatureIngeneering(XMLContent xmlContent) {
      XMLContent headerDataXMLContent = buildXMLContentWithHeaderContent(xmlContent);
      Map<String, List<LineContent>> tarifziffer2LineContentsMap = mapTarifziffer2LineContant(xmlContent);
      return tarifziffer2LineContentsMap.entrySet()
            .stream()
            .map(Entry::getValue)
            .map(this::changeDurationValues4EachContentLine)
            .flatMap(List::stream)
            .collect(Collectors.collectingAndThen(Collectors.toList(), addLines2XMLContent(headerDataXMLContent)));
   }

   private static Function<List<LineContent>, XMLContent> addLines2XMLContent(XMLContent xmlContent) {
      return lineContents -> {
         lineContents.stream()
               .forEach(xmlContent::add);
         return xmlContent;
      };
   }

   private static XMLContent buildXMLContentWithHeaderContent(XMLContent xmlContent) {
      return ContentUtil.getInvoiceHeaderContent(xmlContent.getContent())
            .stream()
            .collect(Collectors.collectingAndThen(Collectors.toList(), XMLContentImpl::new));
   }

   private List<LineContent> changeDurationValues4EachContentLine(List<LineContent> lineContents4Tarifziffer) {
      double sumDuration = calcSumDuration(lineContents4Tarifziffer);
      for (LineContent lineContent : lineContents4Tarifziffer) {
         DoubleMutableInvoiceAttr doubleMutableInvoiceAttr = getDurationValueAttr(lineContent);
         doubleMutableInvoiceAttr.setValue(sumDuration);
         logFeatureEngineering(getFileName(lineContent), doubleMutableInvoiceAttr, sumDuration);
      }
      return lineContents4Tarifziffer;
   }

   private double calcSumDuration(List<LineContent> lineContents4Tarifziffer) {
      return lineContents4Tarifziffer.stream()
            .map(this::getDurationValueAttr)
            .map(DoubleMutableInvoiceAttr::getTypedValue)
            .reduce(0.0, (dur1, dur2) -> dur1 + dur2);
   }

   private Predicate<LineContent> hasAttribute4Name(String attrName) {
      return lineConent -> lineConent.getInvoiceAttrs()
            .stream()
            .anyMatch(isAttribute4Name(attrName));
   }

   /*
    *  We want to map all 'service-data'-lines (which contains a duration attribute) to their corresponding tariffiziffer
    */
   private Map<String, List<LineContent>> mapTarifziffer2LineContant(XMLContent xmlContent) {
      Map<String, List<LineContent>> tarifziffer2LineContantMap = new HashMap<>();
      List<LineContent> filterRelevantContentLines = getServiceLineContentWithDurationAttr(xmlContent);
      for (LineContent lineContent : filterRelevantContentLines) {
         String codeValue = getCodeValue(lineContent);
         List<LineContent> invoiceAttrs4Tarifziffer = tarifziffer2LineContantMap.getOrDefault(codeValue, new ArrayList<>());
         invoiceAttrs4Tarifziffer.add(lineContent);
         tarifziffer2LineContantMap.put(codeValue, invoiceAttrs4Tarifziffer);
      }
      return tarifziffer2LineContantMap;
   }

   /*
    * We want only 'service-data' and only such service data whose contains a duration attribute
    */
   private List<LineContent> getServiceLineContentWithDurationAttr(XMLContent xmlContent) {
      return ContentUtil.getServiceLineContent(xmlContent.getContent())
            .stream()
            .filter(hasAttribute4Name(TREATMENT_DATA_DURATION))
            .collect(Collectors.toList());
   }

   private String getCodeValue(LineContent lineContent) {
      InvoiceAttr invoiceAttr =
            findAttributeByName(lineContent.getInvoiceAttrs(), InvoiceXMLConstants.RECORD_TARMED_TAG_CODE);
      return invoiceAttr.getValue();
   }

   private DoubleMutableInvoiceAttr getDurationValueAttr(LineContent lineContent) {
      return findAttributeByName(lineContent.getInvoiceAttrs(), InvoiceXMLConstants.TREATMENT_DATA_DURATION, DoubleMutableInvoiceAttr.class);
   }

   private static String getFileName(LineContent lineContent) {
      return lineContent.getOptionalXMLFileName()
            .orElse("nA");
   }
}
