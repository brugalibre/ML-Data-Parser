package com.myownb3.dominic.tarifziffer.core.export.impl.header;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.NominalInvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.tarifziffer.label.LabelEvaluator;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;
import com.myownb3.dominic.tarifziffer.random.RandomNumber;
import com.myownb3.dominic.tarifziffer.weka.WekaConstants;

public class WekaHeaderSupplier implements Supplier<List<String>> {

   private static final Logger LOG = Logger.getLogger(WekaHeaderSupplier.class);

   @Override
   public List<String> get() {
      return evalHeader();
   }

   protected List<String> evalHeader() {
      int id = RandomNumber.getNext();
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start collecting weka header informations", id);
      List<String> headerContent = new ArrayList<>();
      headerContent.add(WekaConstants.AT_RELATION + " " + WekaConstants.RELATION_NAME + System.lineSeparator());
      headerContent.add(System.lineSeparator());
      for (ContentType contentType : ContentType.getOrderedHeaderContentTypes()) {
         addAtAttributeAnnotation4ContentType(headerContent, contentType);
      }
      addAtAttributeAnnotation4ContentType(headerContent, ContentType.SERVICES_DATA);
      headerContent.add(buildLabelRep());
      headerContent.add(System.lineSeparator() + WekaConstants.AT_DATA + System.lineSeparator());
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done collecting weka header informations %s\n", id);
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
         headerContent.add(WekaConstants.AT_ATTRIBUTE + " " + invoiceAttr.getName() + " " + typeRep + System.lineSeparator());
      };
   }

   protected List<InvoiceAttr> evalAllInvoiceAttrs4AtAttributeAnnotation(ContentType contentType) {
      return InvoiceAttrs.INSTANCE.getAllRelevantInvoiceAttrs(contentType);
   }

   protected String getTypeRep(InvoiceAttr invoiceAttr) {
      if (isNumeric(invoiceAttr)) {
         return WekaConstants.NUMERIC;
      } else if (invoiceAttr.isNominal()) {
         return buildCategoricalValuesRep(((NominalInvoiceAttr) invoiceAttr).getCategoricalValues());
      }
      throw new IllegalStateException("Attribute '" + invoiceAttr + "' not handled!");
   }

   private static boolean isNumeric(InvoiceAttr invoiceAttr) {
      return invoiceAttr.isDouble() || invoiceAttr.isInteger();
   }

   private static String buildCategoricalValuesRep(List<String> possibleValuesAsList) {
      StringBuilder sb = new StringBuilder();
      for (Iterator<String> iterator = possibleValuesAsList.iterator(); iterator.hasNext();) {
         String value = iterator.next();
         sb.append(value + (iterator.hasNext() ? WekaConstants.LINE_DELIMITER : ""));
      }
      return String.format(WekaConstants.CATEGORICAL_VALUES_PATTERN, sb.toString());
   }

   private static String buildLabelRep() {
      String categoricalValuesRep = buildCategoricalValuesRep(LabelEvaluator.INSTANCE.getLabels());
      return WekaConstants.AT_ATTRIBUTE + " " + WekaConstants.CLASSIFIER_ID + " " + categoricalValuesRep;
   }

}
