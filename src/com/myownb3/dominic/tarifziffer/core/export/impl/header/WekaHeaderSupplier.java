package com.myownb3.dominic.tarifziffer.core.export.impl.header;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.myownb3.dominic.invoice.attrs.constants.InvoiceConstants;
import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.NominalInvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;

public class WekaHeaderSupplier implements Supplier<List<String>> {

   protected static final String NUMERIC = "numeric";
   private static Logger LOG = Logger.getLogger(WekaHeaderSupplier.class);

   @Override
   public List<String> get() {
      return evalHeader();
   }

   protected List<String> evalHeader() {
      LOG.info("Start collecting weka header informations");
      List<String> headerContent = new ArrayList<>();
      headerContent.add("@relation invoice-data" + "\n");
      headerContent.add("\n");
      for (ContentType contentType : ContentType.getOrderedHeaderContentTypes()) {
         addAtAttributeAnnotation4ContentType(headerContent, contentType);
      }
      addAtAttributeAnnotation4ContentType(headerContent, ContentType.SERVICES_DATA);
      headerContent.add("@attribute" + " classification " + buildPossibleValuesRep(InvoiceConstants.LABELS));
      headerContent.add("\n" + "@data" + "\n");
      LOG.info("Done collecting weka header informations");
      return headerContent;
   }

   private void addAtAttributeAnnotation4ContentType(List<String> headerContent, ContentType contentType) {
      evalAllInvoiceAttrs4AtAttributeAnnotation(contentType)
            .stream()
            .forEach(addAtAttribute(headerContent));
   }

   private Consumer<InvoiceAttr> addAtAttribute(List<String> headerContent) {
      return invoiceAttr -> {
         String typeRep = getTypeRep(invoiceAttr);
         headerContent.add("@attribute " + invoiceAttr.getName() + " " + typeRep + "\n");
      };
   }

   protected List<InvoiceAttr> evalAllInvoiceAttrs4AtAttributeAnnotation(ContentType contentType) {
      return InvoiceAttrs.getAllRelevantInvoiceAttrs(contentType);
   }

   protected String getTypeRep(InvoiceAttr invoiceAttr) {
      if (isNumeric(invoiceAttr)) {
         return NUMERIC;
      } else if (invoiceAttr.isNominal()) {
         return buildPossibleValuesRep(((NominalInvoiceAttr) invoiceAttr).getCategoricalValues());
      }
      throw new IllegalStateException("Attribute '" + invoiceAttr + "' not handled!");
   }

   private static boolean isNumeric(InvoiceAttr invoiceAttr) {
      return invoiceAttr.isDouble() || invoiceAttr.isInteger();
   }

   private static String buildPossibleValuesRep(List<String> possibleValuesAsList) {
      String rep = "";
      for (Iterator<String> iterator = possibleValuesAsList.iterator(); iterator.hasNext();) {
         String value = iterator.next();
         rep += value + (iterator.hasNext() ? "," : "");
      }
      return "{" + rep + "}";
   }
}
