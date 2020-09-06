package com.myownb3.dominic.tarifziffer.core.export.impl;

import static com.myownb3.dominic.invoice.util.StringUtil.flattenList;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.Level;

import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.invoice.exception.AttrHasNoValuesException;
import com.myownb3.dominic.invoice.util.StringUtil;
import com.myownb3.dominic.tarifziffer.core.export.ContentUtil;
import com.myownb3.dominic.tarifziffer.core.export.impl.header.DefaultHeaderSupplier;
import com.myownb3.dominic.tarifziffer.core.export.impl.header.WekaHeaderSupplier;
import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;
import com.myownb3.dominic.tarifziffer.label.LabelEvaluator;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;

public class InvoiceExportContentCollectorImpl extends AbstractInvoiceExportContentCollector {

   protected Supplier<List<String>> headerSupplier;
   protected ExportMode exportMode;

   public InvoiceExportContentCollectorImpl(List<XMLFileParseResult> result, ExportMode exportMode, boolean omitHeader) {
      super();
      this.result = requireNonNull(result, "The result must not be null!");
      this.headerSupplier = omitHeader ? Collections::emptyList : getHeaderSupplier(exportMode);
      this.exportMode = requireNonNull(exportMode);
   }

   @Override
   public List<String> collectContent() {
      beforeCollecting();
      if (result.isEmpty()) {
         return Collections.emptyList();
      }
      List<String> keys = evalServiceDataKeys(result);
      List<String> fileContent = result.parallelStream()
            .map(buildStringRepresentation(keys))
            .collect(Collectors.collectingAndThen(Collectors.toList(), StringUtil.appendLineBreaks()));
      addHeader(fileContent);
      afterCollecting();
      return fileContent;
   }

   /**
    * Returns a list containing all necessary names of services {@link InvoiceAttrs} names as key
    * 
    * @param result
    *        the result of parsed xml files - is used by overriding subclasses
    * @return a list containing all necessary names of services {@link InvoiceAttrs} names as key
    */
   protected List<String> evalServiceDataKeys(List<XMLFileParseResult> result) {
      return InvoiceAttrs.INSTANCE.getAllInvoiceAttrsNames(ContentType.SERVICES_DATA, exportMode.isRawExport());
   }

   private void addHeader(List<String> fileContent) {
      fileContent.addAll(0, headerSupplier.get());
   }

   private Function<XMLFileParseResult, String> buildStringRepresentation(List<String> keys) {
      return xmlFileParseResult -> buildStringRepresentation(keys, xmlFileParseResult);
   }

   private String buildStringRepresentation(List<String> keys, XMLFileParseResult xmlFileParseResult) {
      logBeginCollect(xmlFileParseResult);
      String headerContentLine = collectAndBuildHeaderContentLine(xmlFileParseResult);
      return ContentUtil.getServicesLineContent(xmlFileParseResult)
            .stream()
            .map(appendContentLine2String(keys, xmlFileParseResult, headerContentLine))
            .collect(Collectors.collectingAndThen(Collectors.toList(), StringUtil.flattenListnl()));
   }

   private Function<LineContent, String> appendContentLine2String(List<String> keys, XMLFileParseResult xmlFileParseResult,
         String headerContentLine) {
      return lineContent -> {
         StringBuilder stringBuilder = new StringBuilder();
         appendFileName(stringBuilder, xmlFileParseResult.getXMLFileName());
         String label = getLabel(xmlFileParseResult);
         String serviceDataContentLine = buildExportLine(keys, lineContent, !label.isEmpty()) + label;
         stringBuilder.append(headerContentLine + exportMode.getLineDelimiter() + serviceDataContentLine);
         return stringBuilder.toString();
      };
   }

   private String getLabel(XMLFileParseResult xmlFileParseResult) {
      return exportMode.isWekaExport() ? LabelEvaluator.INSTANCE.getLabel(xmlFileParseResult.getXMLFileName()) : "";
   }

   private String collectAndBuildHeaderContentLine(XMLFileParseResult xmlFileParseResult) {
      StringBuilder stringBuilder = new StringBuilder();
      List<LineContent> noneServiceData = ContentUtil.getInvoiceHeaderContent(xmlFileParseResult);
      Iterator<LineContent> noneServiceDataIter = noneServiceData.iterator();

      while (noneServiceDataIter.hasNext()) {
         LineContent lineContent = noneServiceDataIter.next();
         List<String> lineContentKeys = evalNonServiceDataKeys(lineContent);
         String contentLine = buildExportLine(lineContentKeys, lineContent, noneServiceDataIter.hasNext());
         stringBuilder.append(contentLine);
      }
      return stringBuilder.toString();
   }

   protected List<String> evalNonServiceDataKeys(LineContent lineContent) {
      return InvoiceAttrs.INSTANCE.getAllInvoiceAttrsNames(lineContent.getContentType(), exportMode.isRawExport());
   }

   /**
    * Exports all values within the given {@link LineContent} as single String
    * The given keys, respectively names of {@link InvoiceAttrs}, defines the values which are exported.
    * If a value is missing of a given key, a {@link AttrHasNoValuesException} is thrown. This ensures, that all necessary values are
    * present!
    * 
    * @param keys
    *        the name of attributes whose values are exported
    * @param lineContent
    *        the {@link LineContent} with the values
    * @param hasNextLine
    *        <code>true</code> if there is a next line to append
    * @return a single String with all values
    */
   protected String buildExportLine(List<String> keys, LineContent lineContent, boolean hasNextLine) {
      return keys.stream()
            .map(getLineValue4Key(lineContent))
            .map(addLineDelimiter())
            .collect(Collectors.collectingAndThen(Collectors.toList(),
                  flattenList().andThen(removeLastLineDelimiter(hasNextLine))));
   }

   private Function<String, String> getLineValue4Key(LineContent lineContent) {
      return key -> {
         String value4Key = lineContent.getValue(key);
         if (nonNull(value4Key)) {
            return value4Key;
         }
         throw new AttrHasNoValuesException("Attribute '" + key + "' has no value set!");
      };
   }

   protected Function<String, String> addLineDelimiter() {
      return lineValue -> lineValue + exportMode.getLineDelimiter();
   }

   /*
    * Remove the last delimiter, if there is no next line
    * Since we added one to all values, we have to remove the last one because we don't need any at the last one
    */
   protected Function<String, String> removeLastLineDelimiter(boolean hasNextLine) {
      return value -> {
         if (!hasNextLine) {
            return new StringBuilder(value)
                  .delete(value.length() - 1, value.length())
                  .toString();
         }
         return value;
      };
   }

   private void appendFileName(StringBuilder stringBuilder, String xmlFileName) {
      if (!exportMode.isRawExport()) {
         stringBuilder.append(xmlFileName);
         stringBuilder.append(exportMode.getLineDelimiter());
      }
   }

   private Supplier<List<String>> getHeaderSupplier(ExportMode exportMode) {
      if (exportMode.isWekaExport()) {
         return getWekaHeaderSupplier();
      } else {
         return getDefaultHeaderSupplier(result, exportMode);
      }
   }

   protected WekaHeaderSupplier getWekaHeaderSupplier() {
      return new WekaHeaderSupplier();
   }

   /**
    * Returns the default {@link Supplier} for a header
    * 
    * @param result
    *        the result of all parsed xml files - is used in overriding classes
    * @param exportMode
    *        the {@link ExportMode}
    * @return the default {@link Supplier} for a header
    */
   protected Supplier<List<String>> getDefaultHeaderSupplier(List<XMLFileParseResult> result, ExportMode exportMode) {
      return new DefaultHeaderSupplier(exportMode);
   }

   private static void logBeginCollect(XMLFileParseResult xmlFileParseResult) {
      LoggerHelper.INSTANCE.logIfEnabled(LOG, () -> "Collecting content lines for file '" + xmlFileParseResult.getXMLFileName() + "' with total '"
            + xmlFileParseResult.getContentSize() + "' lines", Level.DEBUG);
   }
}
