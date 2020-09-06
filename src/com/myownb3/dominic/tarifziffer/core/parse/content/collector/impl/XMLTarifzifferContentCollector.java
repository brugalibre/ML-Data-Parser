package com.myownb3.dominic.tarifziffer.core.parse.content.collector.impl;

import static com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants.RECORD_TARMED_TAG_CODE;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.xml.sax.Attributes;

import com.myownb3.dominic.invoice.attrs.constants.InvoiceXMLConstants;
import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.tarifziffer.core.parse.content.collector.XMLContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.LineContentImpl;

public class XMLTarifzifferContentCollector implements XMLContentCollector {

   private Predicate<String> tarifzifferPredicate;

   public XMLTarifzifferContentCollector(Predicate<String> tarifzifferPredicate) {
      this.tarifzifferPredicate = requireNonNull(tarifzifferPredicate);
   }

   @Override
   public LineContent collectContent(String parentQName, Attributes attributes) {
      List<InvoiceAttr> invoiceAttrs = new ArrayList<>();
      for (int i = 0; i < attributes.getLength(); i++) {
         InvoiceAttr invoiceAttr = InvoiceAttrs.INSTANCE.buildInvoiceAttr(attributes.getQName(i), attributes.getValue(i));
         invoiceAttrs.add(invoiceAttr);
      }
      return new LineContentImpl(invoiceAttrs);
   }


   @Override
   public boolean areAttributesRelevant(String qName, Attributes attributes) {
      if (isQNameTarifzifferRelevant(qName)) {
         String codeValue = attributes.getValue(RECORD_TARMED_TAG_CODE);
         return tarifzifferPredicate.test(codeValue);
      }
      return false;
   }

   private static boolean isQNameTarifzifferRelevant(String qName) {
      return InvoiceXMLConstants.RECORD_OTHER.equals(qName)
            || InvoiceXMLConstants.RECORD_DRG.equals(qName)
            || InvoiceXMLConstants.RECORD_DRUG.equals(qName)
            || InvoiceXMLConstants.RECORD_MIGEL.equals(qName)
            || InvoiceXMLConstants.RECORD_PARAMED.equals(qName)
            || InvoiceXMLConstants.RECORD_LAB.equals(qName)
            || InvoiceXMLConstants.RECORD_TARMED.equals(qName);
   }
}
