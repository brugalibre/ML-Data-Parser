package com.myownb3.dominic.tarifziffer.core.parse.result.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.tarifziffer.core.parse.content.constants.ContentConstants;
import com.myownb3.dominic.tarifziffer.core.parse.content.util.KeyValue2StringUtil;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;

public class LineContentImpl implements LineContent {

   private List<InvoiceAttr> invoiceAttrs;

   public LineContentImpl(List<? extends InvoiceAttr> invoiceAttrs) {
      requireNonNull(invoiceAttrs);
      this.invoiceAttrs = new ArrayList<>(invoiceAttrs);
      Collections.sort(this.invoiceAttrs, Comparator.comparing(InvoiceAttr::getSequence));
   }

   @Override
   public String toString() {
      return KeyValue2StringUtil.toString(invoiceAttrs, ContentConstants.ELEMENT_DELIMITER);
   }

   @Override
   public String getValue(String key) {
      return invoiceAttrs.parallelStream()
            .filter(invoiceAttr -> invoiceAttr.getName().equals(key))
            .map(InvoiceAttr::getValue)
            .findFirst()
            .orElse(null);
   }

   @Override
   public boolean isServicesContent() {
      return getContentType() == ContentType.SERVICES_DATA;
   }

   @Override
   public ContentType getContentType() {
      return invoiceAttrs.isEmpty() ? null : invoiceAttrs.get(0).getContentType();
   }

   @Override
   public List<InvoiceAttr> getInvoiceAttrs() {
      return Collections.unmodifiableList(invoiceAttrs);
   }
}
