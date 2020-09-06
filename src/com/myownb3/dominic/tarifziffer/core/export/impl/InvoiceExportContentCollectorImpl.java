package com.myownb3.dominic.tarifziffer.core.export.impl;

import static java.util.Objects.nonNull;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

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

public class InvoiceExportContentCollectorImpl extends AbstractInvoiceExportContentCollector {

   private static final Logger LOG = Logger.getLogger(InvoiceExportContentCollectorImpl.class);
   protected Supplier<List<String>> headerSupplier;
   private ExportMode exportMode;
   private String fileName;
   private LabelEvaluator labelEvaluator;

   public InvoiceExportContentCollectorImpl(List<XMLFileParseResult> result, String fileName, ExportMode exportMode) {
      super();
      this.result = result;
      this.fileName = fileName;
      this.headerSupplier = getHeaderSupplier(exportMode);
      this.exportMode = exportMode;
      if (exportMode.isWekaExport()) {
         this.labelEvaluator = buildAndInitLabelEvaluator();
      }
   }

   @Override
   public List<String> collectContent() {
      LOG.info("Start collecting results. Total results: '" + result.size() + "'");
      List<String> keys = evalServiceDataKeys(result);
      List<String> fileContent = result.parallelStream()
            .map(buildStringRepresentation(keys))
            .collect(Collectors.collectingAndThen(Collectors.toList(), StringUtil.appendLineBreaks()));
      addHeader(fileContent);
      LOG.info("Done collecting results");
      return fileContent;
   }

   protected List<String> evalServiceDataKeys(List<XMLFileParseResult> result) {
      return InvoiceAttrs.getAllInvoiceAttrsNames(ContentType.SERVICES_DATA, exportMode.isRawExport());
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
            .parallelStream()
            .map(appendContentLine2String(keys, xmlFileParseResult, headerContentLine))
            .collect(Collectors.collectingAndThen(Collectors.toList(), StringUtil.flattenList()));
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
      return nonNull(labelEvaluator) ? labelEvaluator.getLabel(xmlFileParseResult.getXMLFileName()) : "";
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
      return InvoiceAttrs.getAllInvoiceAttrsNames(lineContent.getContentType(), exportMode.isRawExport());
   }

   private String buildExportLine(List<String> keys, LineContent lineContent, boolean hasNextLine) {
      StringBuilder stringBuilder = new StringBuilder();
      Iterator<String> keyIterator = keys.iterator();
      while (keyIterator.hasNext()) {
         stringBuilder.append(getLineValue4Key(lineContent, keyIterator.next()));
         addLineDelimiterIfNecessary(keyIterator.hasNext(), stringBuilder);
      }
      addLineDelimiterIfNecessary(hasNextLine, stringBuilder);
      return stringBuilder.toString();
   }

   private void addLineDelimiterIfNecessary(boolean hasNextLine, StringBuilder stringBuilder) {
      if (hasNextLine) {
         stringBuilder.append(exportMode.getLineDelimiter());
      }
   }

   private static String getLineValue4Key(LineContent lineContent, String key) {
      String value4Key = lineContent.getValue(key);
      if (nonNull(value4Key)) {
         return value4Key;
      }
      throw new AttrHasNoValuesException("Attribute '" + key + "' has no value set!");
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

   protected Supplier<List<String>> getDefaultHeaderSupplier(List<XMLFileParseResult> result, ExportMode exportMode) {
      return new DefaultHeaderSupplier(exportMode);
   }

   @Override
   public String getExportFileName() {
      return fileName;
   }

   @Override
   public String getFileExtension() {
      return exportMode.getFileExtension();
   }

   private static void logBeginCollect(XMLFileParseResult xmlFileParseResult) {
      LOG.info("Collecting content lines for file '" + xmlFileParseResult.getXMLFileName() + "' with total '" + xmlFileParseResult.getContentSize()
            + "' lines");
   }

   private static LabelEvaluator buildAndInitLabelEvaluator() {
      LOG.info("Start initializing the LabelEvaluator");
      LabelEvaluator labelEvaluator = new LabelEvaluator();
      labelEvaluator.init();
      LOG.info("Done initializing the LabelEvaluator");
      return labelEvaluator;
   }
}
