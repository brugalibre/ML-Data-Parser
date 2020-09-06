package com.myownb3.dominic.tarifziffer.core.featureengineering.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.tarifziffer.core.featureengineering.InvoicesFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.featureengineering.impl.xmlcontent.InvoiceTreatmentDurationFeatureEngineererImpl;
import com.myownb3.dominic.tarifziffer.core.featureengineering.linecontent.LineContentFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.featureengineering.xmlcontent.XMLContentFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.XMLContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.LineContentImpl;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLContentImpl;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;

public class InvoicesFeatureEngineererImpl implements InvoicesFeatureEngineerer {
   private static final Logger LOG = Logger.getLogger(InvoicesFeatureEngineererImpl.class);
   private List<LineContentFeatureEngineerer> featureEngineerers;
   private XMLContentFeatureEngineerer xmlContentFeatureEngineerer;

   public InvoicesFeatureEngineererImpl(List<LineContentFeatureEngineerer> featureEngineerers) {
      this.featureEngineerers = requireNonNull(featureEngineerers);
      this.xmlContentFeatureEngineerer = new InvoiceTreatmentDurationFeatureEngineererImpl();
   }

   @Override
   public List<XMLFileParseResult> doFeatureEngineering(List<XMLFileParseResult> result) {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start feature engineering '" + result.size() + "' results");
      List<XMLFileParseResult> engineeredResult = doFeatureIngeneering0(result);
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done feature engineering %s\n");
      return engineeredResult;
   }

   private List<XMLFileParseResult> doFeatureIngeneering0(List<XMLFileParseResult> result) {
      return result.parallelStream()
            .map(doFeatureEngineeringForResult())
            .collect(Collectors.toList());
   }

   private Function<XMLFileParseResult, XMLFileParseResult> doFeatureEngineeringForResult() {
      return xmlFileParseResult -> {
         XMLContent xmlContent = new XMLContentImpl(xmlFileParseResult.getContent()
               .parallelStream()
               .map(doFeatureEngineering(xmlFileParseResult))
               .collect(Collectors.toList()));
         XMLContent engineeredXMLContent = xmlContentFeatureEngineerer.doFeatureIngeneering(xmlContent);
         return XMLFileParseResult.of(xmlFileParseResult.getXMLFileName(), engineeredXMLContent);
      };
   }

   private Function<LineContent, LineContent> doFeatureEngineering(XMLFileParseResult xmlFileParseResult) {
      return lineContent -> {
         List<InvoiceAttr> invoiceAttrs = lineContent.getInvoiceAttrs();
         Optional<LineContentFeatureEngineerer> featureEngineererOpt = findFeatureEngineerer4InvoiceAttrs(invoiceAttrs);
         return new LineContentImpl(featureEngineererOpt
               .map(doFeatureIngeneering0(invoiceAttrs, xmlFileParseResult))
               .orElse(invoiceAttrs), xmlFileParseResult.getXMLFileName());
      };
   }

   private Function<LineContentFeatureEngineerer, List<InvoiceAttr>> doFeatureIngeneering0(List<InvoiceAttr> invoiceAttrs,
         XMLFileParseResult xmlFileParseResult) {
      return featureEngineerer -> featureEngineerer.doFeatureIngeneering(invoiceAttrs, xmlFileParseResult);
   }

   private Optional<LineContentFeatureEngineerer> findFeatureEngineerer4InvoiceAttrs(List<InvoiceAttr> invoiceAttrs) {
      return featureEngineerers.stream()
            .filter(featureEngineerer -> featureEngineerer.has2EngineerInvoiceAttrs(invoiceAttrs))
            .findFirst();
   }
}
