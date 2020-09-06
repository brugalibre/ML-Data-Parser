package com.myownb3.dominic.tarifziffer.core.featureengineering.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

public abstract class AbstractFeatureEngineerer {
   private static final Logger LOG = Logger.getLogger(AbstractFeatureEngineerer.class);

   protected void logFeatureEngineering(InvoiceAttr engineeredInvoiceAttr, Object value) {
      LOG.info("Attribute '" + engineeredInvoiceAttr.getName() + "' with value '" + value + " engineered");
   }

   /**
    * Adds the given engineered {@link InvoiceAttrs} to the given {@link InvoiceAttrs}s. If any of the new ones
    * Already exist, those existed {@link InvoiceAttrs} are removed first
    * 
    * @param existingInvoiceAttrrs
    *        the existing{@link InvoiceAttr} to sort
    * @param engineeredInvoiceAttrs
    *        the new, engineered {@link InvoiceAttr}
    * @return a sorted combination of existing {@link InvoiceAttr}s combined with the new ones
    */
   protected List<InvoiceAttr> getSorted(List<InvoiceAttr> existingInvoiceAttrsIn, InvoiceAttr... engineeredInvoiceAttrs) {
      List<InvoiceAttr> existingInvoiceAttrs = new ArrayList<>(existingInvoiceAttrsIn);
      removeAlreadyExistingAttrs(existingInvoiceAttrsIn, existingInvoiceAttrs, engineeredInvoiceAttrs);
      existingInvoiceAttrs.addAll(Arrays.asList(engineeredInvoiceAttrs));
      return existingInvoiceAttrs.stream()
            .sorted(Comparator.comparing(InvoiceAttr::getSequence))
            .collect(Collectors.toList());
   }

   private static void removeAlreadyExistingAttrs(List<InvoiceAttr> existingInvoiceAttrsIn, List<InvoiceAttr> existingInvoiceAttrs,
         InvoiceAttr... engineeredInvoiceAttrs) {
      for (InvoiceAttr egineeredInvoiceAttr : engineeredInvoiceAttrs) {
         existingInvoiceAttrsIn.stream()
               .filter(invoiceAttr -> invoiceAttr.getName().equals(egineeredInvoiceAttr.getName()))
               .forEach(existingInvoiceAttrs::remove);
      }
   }

   /**
    * Return all {@link InvoiceAttr}s for the given {@link XMLFileParseResult}<
    * 
    * @param xmlFileParseResult
    * @return all {@link InvoiceAttr}s for the given {@link XMLFileParseResult}<
    */
   protected List<InvoiceAttr> getAllInvoiceAttrs4File(XMLFileParseResult xmlFileParseResult) {
      return xmlFileParseResult.getContent()
            .stream()
            .map(LineContent::getInvoiceAttrs)
            .flatMap(List::stream)
            .collect(Collectors.toList());
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
   protected InvoiceAttr findAttribute4Name(List<InvoiceAttr> invoiceAttrs, String attrName) {
      return invoiceAttrs.stream()
            .filter(isAttribute4Name(attrName))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No Attribute found for name '" + attrName + "'!"));
   }

   protected Predicate<InvoiceAttr> isAttribute4Name(String name) {
      return invoiceAttr -> name.equals(invoiceAttr.getName());
   }
}
