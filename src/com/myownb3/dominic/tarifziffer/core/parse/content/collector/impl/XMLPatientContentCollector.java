package com.myownb3.dominic.tarifziffer.core.parse.content.collector.impl;

import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.PATIENT_DATA_BIRTHDATE;
import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.PATIENT_DATA_GENDER;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;

import com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants;
import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.tarifziffer.core.parse.content.collector.XMLContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.LineContentImpl;

public class XMLPatientContentCollector implements XMLContentCollector {

   @Override
   public LineContent collectContent(String parentQName, Attributes attributes) {
      List<InvoiceAttr> invoiceAttrs = new ArrayList<>();
      for (int i = 0; i < attributes.getLength(); i++) {
         String qName = attributes.getQName(i);
         if (isGenderOrBirthdateValue(qName)) {
            InvoiceAttr invoiceAttr = InvoiceAttrs.buildInvoiceAttr(qName, attributes.getValue(i));
            invoiceAttrs.add(invoiceAttr);
         }
      }
      return new LineContentImpl(invoiceAttrs);
   }

   private static boolean isGenderOrBirthdateValue(String qName) {
      return PATIENT_DATA_GENDER.equals(qName)
            || PATIENT_DATA_BIRTHDATE.equals(qName);
   }

   @Override
   public boolean areAttributesRelevant(String qName, Attributes attributes) {
      return InvoiceXMLConstants.PATIENT_DATA.equals(qName);
   }
}
