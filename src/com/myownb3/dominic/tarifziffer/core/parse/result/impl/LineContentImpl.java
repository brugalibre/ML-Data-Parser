package com.myownb3.dominic.tarifziffer.core.parse.result.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.tarifziffer.core.parse.content.constants.ContentConstants;
import com.myownb3.dominic.tarifziffer.core.parse.content.util.KeyValue2StringUtil;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;

public class LineContentImpl implements LineContent {

   private List<InvoiceAttr> invoiceAttrs;
   private Map<String, InvoiceAttr> invoiceName2InvoiceAttrMap;
   private String xmlFileName;

   public LineContentImpl(List<? extends InvoiceAttr> invoiceAttrs, String xmlFileName) {
      requireNonNull(invoiceAttrs);
      this.xmlFileName = xmlFileName;
      this.invoiceAttrs = new ArrayList<>(invoiceAttrs);
      this.invoiceName2InvoiceAttrMap = invoiceAttrs.stream()
            .collect(Collectors.toMap(InvoiceAttr::getName, Function.identity()));
      Collections.sort(this.invoiceAttrs, Comparator.comparing(InvoiceAttr::getSequence));
   }

   public LineContentImpl(List<? extends InvoiceAttr> invoiceAttrs) {
      this(invoiceAttrs, null);
   }

   @Override
   public String toString() {
      return KeyValue2StringUtil.toString(invoiceAttrs, ContentConstants.ELEMENT_DELIMITER);
   }

   @Override
   public String getValue(String key) {
      InvoiceAttr invoiceAttr = invoiceName2InvoiceAttrMap.get(key);
      return invoiceAttr != null ? invoiceAttr.getValue() : null;
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

   @Override
   public Optional<String> getOptionalXMLFileName() {
      return Optional.ofNullable(xmlFileName);
   }
}
