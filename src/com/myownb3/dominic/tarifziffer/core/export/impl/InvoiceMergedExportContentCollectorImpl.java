package com.myownb3.dominic.tarifziffer.core.export.impl;

import static com.myownb3.dominic.invoice.util.StringUtil.flattenList;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.tarifziffer.core.export.impl.header.MergedWekaHeaderSupplier;
import com.myownb3.dominic.tarifziffer.core.export.impl.header.WekaHeaderSupplier;
import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

public class InvoiceMergedExportContentCollectorImpl extends InvoiceExportContentCollectorImpl {

   public InvoiceMergedExportContentCollectorImpl(List<XMLFileParseResult> result, ExportMode exportMode, boolean omitHeader) {
      super(result, exportMode, omitHeader);
   }

   @Override
   protected WekaHeaderSupplier getWekaHeaderSupplier() {
      return new MergedWekaHeaderSupplier();
   }

   @Override
   protected Supplier<List<String>> getDefaultHeaderSupplier(List<XMLFileParseResult> result, ExportMode exportMode) {
      List<String> allKeys = new LinkedList<>();
      allKeys.addAll(evalAllNonServiceDataKeys(evalAllNonServiceDataLines(result)));
      allKeys.addAll(evalServiceDataKeys(result));
      return exportMode.isRawExport() ? Collections::emptyList
            : () -> mapp2String(exportMode, allKeys);
   }

   private List<String> mapp2String(ExportMode exportMode, List<String> allKeys) {
      StringBuilder stringBuilder = new StringBuilder("File name" + exportMode.getLineDelimiter());
      for (Iterator<String> iterator = allKeys.iterator(); iterator.hasNext();) {
         String key = iterator.next();
         stringBuilder.append(key + (iterator.hasNext() ? exportMode.getLineDelimiter() : System.lineSeparator()));
      }
      return Collections.singletonList(stringBuilder.toString());
   }

   /**
    * Since we are exporting merged attributes, we don't need the keys.
    * We simply export the InvoiceAttr-values we have within the LineContent
    */
   @Override
   protected String buildExportLine(List<String> keys, LineContent lineContent, boolean hasNextLine) {
      return lineContent.getInvoiceAttrs()
            .parallelStream()
            .map(addValueAndLineDelimiter())
            .collect(Collectors.collectingAndThen(Collectors.toList(),
                  flattenList().andThen(removeLastLineDelimiter(hasNextLine))));
   }

   private Function<InvoiceAttr, String> addValueAndLineDelimiter() {
      return invoiceAttr -> invoiceAttr.getValue() + exportMode.getLineDelimiter();
   }

   @Override
   protected List<String> evalServiceDataKeys(List<XMLFileParseResult> result) {
      LineContent firstLineContent = evalFirstServiceDataLine(result);
      return firstLineContent.getInvoiceAttrs()
            .parallelStream()
            .map(InvoiceAttr::getName)
            .collect(Collectors.toList());
   }

   private List<String> evalAllNonServiceDataKeys(List<LineContent> lineContents) {
      return lineContents.stream()
            .map(this::evalNonServiceDataKeys)
            .flatMap(List::stream)
            .collect(Collectors.toList());
   }

   @Override
   protected List<String> evalNonServiceDataKeys(LineContent lineContent) {
      return lineContent.getInvoiceAttrs()
            .stream()
            .map(InvoiceAttr::getName)
            .collect(Collectors.toList());
   }

   private static LineContent evalFirstServiceDataLine(List<XMLFileParseResult> result) {
      return result.parallelStream()
            .map(XMLFileParseResult::getContent)
            .flatMap(List::stream)
            .filter(LineContent::isServicesContent)
            .findFirst()
            .orElseThrow(IllegalStateException::new);
   }

   private static List<LineContent> evalAllNonServiceDataLines(List<XMLFileParseResult> result) {
      Predicate<LineContent> isServiceContext = LineContent::isServicesContent;
      return result.stream()
            .map(XMLFileParseResult::getContent)
            .flatMap(List::stream)
            .filter(isServiceContext.negate())
            .collect(Collectors.toList());
   }
}
