package com.myownb3.dominic.tarifziffer.core.export.impl.header;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.parse.content.util.KeyValue2StringUtil;

public class DefaultHeaderSupplier implements Supplier<List<String>> {

   private ExportMode exportMode;

   public DefaultHeaderSupplier(ExportMode exportMode) {
      this.exportMode = exportMode;
   }

   @Override
   public List<String> get() {
      if (!exportMode.isRawExport()) {
         return Collections.singletonList(buildDefaultHeader());
      }
      return Collections.emptyList();
   }

   private String buildDefaultHeader() {
      List<String> keys = InvoiceAttrs.getAllInvoiceAttrsNames(ContentType.SERVICES_DATA, exportMode.isRawExport());
      List<String> allKeys = new LinkedList<>();
      for (ContentType contentType : ContentType.getOrderedHeaderContentTypes()) {
         allKeys.addAll(InvoiceAttrs.getAllInvoiceAttrsNames(contentType, exportMode.isRawExport()));
      }
      allKeys.addAll(keys);
      String lineDelimiter = exportMode.getLineDelimiter();
      return "XML-File" + lineDelimiter + KeyValue2StringUtil.toString(allKeys, lineDelimiter) + "\n";
   }
}
