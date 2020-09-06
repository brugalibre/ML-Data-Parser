package com.myownb3.dominic.tarifziffer.core.parse.content.collector.impl;

import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.TREATMENT_SUFFIX;
import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.TREATMENT_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants;
import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.tarifziffer.core.parse.content.collector.XMLContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.LineContentImpl;

public class XMLTreatmentContentCollector implements XMLContentCollector {

   @Override
   public LineContent collectContent(String parentQName, Attributes attributes) {
      List<InvoiceAttr> invoiceAttrs = new ArrayList<>();
      for (int i = 0; i < attributes.getLength(); i++) {
         String qName = attributes.getQName(i);
         if (isTreatmentValue(qName)) {
            InvoiceAttr invoiceAttr = InvoiceAttrs.buildInvoiceAttr(TREATMENT_SUFFIX + qName, attributes.getValue(i));
            invoiceAttrs.add(invoiceAttr);
         }
      }
      if (isAmbulatoryOrStationaryType(parentQName)) {
         String value = parentQName.replace(InvoiceXMLConstants.INVOICE_PREFIX, "");
         InvoiceAttr invoiceAttr = InvoiceAttrs.buildInvoiceAttr(TREATMENT_TYPE, value);
         invoiceAttrs.add(invoiceAttr);
      }
      return new LineContentImpl(invoiceAttrs);
   }

   private static boolean isTreatmentValue(String qName) {
      return InvoiceXMLConstants.TREATMENT_DATA.equals(qName)
            || InvoiceXMLConstants.TREATMENT_DATA_BEGIN.equals(qName)
            || InvoiceXMLConstants.TREATMENT_DATA_REASON.equals(qName);
   }

   @Override
   public boolean areAttributesRelevant(String qName, Attributes attributes) {
      return InvoiceXMLConstants.TREATMENT_DATA.equals(qName)
            || isAmbulatoryOrStationaryType(qName);
   }

   private static boolean isAmbulatoryOrStationaryType(String qName) {
      return InvoiceXMLConstants.TREATMENT_AMBULARTORY_TYPE.equals(qName)
            || InvoiceXMLConstants.TREATMENT_STATIONARY_TYPE.equals(qName);
   }
}
