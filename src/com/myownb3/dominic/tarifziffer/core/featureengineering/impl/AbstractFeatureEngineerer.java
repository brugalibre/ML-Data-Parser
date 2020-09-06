package com.myownb3.dominic.tarifziffer.core.featureengineering.impl;

import java.util.List;
import java.util.function.Predicate;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.model.MutableInvoiceAttr;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;

public abstract class AbstractFeatureEngineerer {
   private static final Logger LOG = Logger.getLogger(AbstractFeatureEngineerer.class);

   /**
    * Return the {@link MutableInvoiceAttr} for the given name and class
    * 
    * @param invoiceAttrs
    *        all {@link InvoiceAttr}
    * @param attrName
    *        the name of the desired {@link InvoiceAttr}
    * @param clazz
    *        the specific class of {@link MutableInvoiceAttr}
    * @return the {@link InvoiceAttr} for the given name
    */
   protected <T extends MutableInvoiceAttr<?>> T findAttributeByName(List<InvoiceAttr> invoiceAttrs, String attrName, Class<T> clazz) {
      return invoiceAttrs.stream()
            .filter(isAttribute4Name(attrName))
            .filter(clazz::isInstance)
            .map(clazz::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No double attribute found for name '" + attrName + "'!"));
   }

   /**
    * Return the {@link InvoiceAttr} for the given name
    * 
    * @param invoiceAttrs
    *        all {@link InvoiceAttr}
    * @param attrName
    *        the name of the desired {@link InvoiceAttr}
    * @return the {@link InvoiceAttr} for the given name
    */
   protected InvoiceAttr findAttributeByName(List<InvoiceAttr> invoiceAttrs, String attrName) {
      return invoiceAttrs.stream()
            .filter(isAttribute4Name(attrName))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No attribute found for name '" + attrName + "'!"));
   }

   protected Predicate<InvoiceAttr> isAttribute4Name(String name) {
      return invoiceAttr -> name.equals(invoiceAttr.getName());
   }

   protected void logFeatureEngineering(String fileName, InvoiceAttr engineeredInvoiceAttr, Object value) {
      LoggerHelper.INSTANCE.logIfEnabled(LOG,
            () -> "File '" + fileName + "', Attribute '" + engineeredInvoiceAttr.getName() + "' with value '" + value + "' engineered", Level.DEBUG);
   }
}
