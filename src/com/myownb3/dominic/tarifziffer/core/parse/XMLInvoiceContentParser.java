
package com.myownb3.dominic.tarifziffer.core.parse;

import static java.util.Objects.isNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.myownb3.dominic.invoice.attrs.metadata.constants.InvoiceAttrs;
import com.myownb3.dominic.tarifziffer.core.datacleaning.InvoiceDataCleaner;
import com.myownb3.dominic.tarifziffer.core.datacleaning.impl.InvoiceDataCleanerImpl;
import com.myownb3.dominic.tarifziffer.core.export.InvoiceContentExportContentCollector;
import com.myownb3.dominic.tarifziffer.core.export.impl.InvoiceExportContentCollectorImpl;
import com.myownb3.dominic.tarifziffer.core.export.impl.InvoiceMergedExportContentCollectorImpl;
import com.myownb3.dominic.tarifziffer.core.export.impl.TarifzifferCounterExportContentCollectorImpl;
import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.featureengineering.InvoicesFeatureEngineerer;
import com.myownb3.dominic.tarifziffer.core.featureengineering.impl.InvoicesFeatureEngineererImpl;
import com.myownb3.dominic.tarifziffer.core.featureengineering.impl.linecontent.FeatureEngineeres;
import com.myownb3.dominic.tarifziffer.core.merging.ResultMerger;
import com.myownb3.dominic.tarifziffer.core.merging.impl.ResultMergerFactory;
import com.myownb3.dominic.tarifziffer.core.parse.content.collector.XMLContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.content.collector.impl.XMLPatientContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.content.collector.impl.XMLTarifzifferContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.content.collector.impl.XMLTreatmentContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.exception.XMLParseException;
import com.myownb3.dominic.tarifziffer.core.parse.handler.XMLInvoiceContentParserHandler;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;
import com.myownb3.dominic.tarifziffer.io.output.ExportInfo;
import com.myownb3.dominic.tarifziffer.io.output.FileExporter;
import com.myownb3.dominic.tarifziffer.label.LabelEvaluator;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;
import com.myownb3.dominic.tarifziffer.mlclassifier.MLClassifiers;

public class XMLInvoiceContentParser {

   private static final Logger LOG = Logger.getLogger(XMLInvoiceContentParser.class);
   private String tarifziffer;
   private List<XMLFileParseResult> parsedXMLResult;
   private List<String> parsedXMLStringResult;
   private ExportMode exportMode;
   private MLClassifiers classifier;
   private ExportInfoContainer exportInfoContainer;

   public XMLInvoiceContentParser(ExportInfoContainer exportInfoContainer, String tarifziffer) {
      this(exportInfoContainer, tarifziffer, MLClassifiers.ANY_OTHER);
   }

   public XMLInvoiceContentParser(ExportInfoContainer exportInfoContainer, String tarifziffer, MLClassifiers classifier) {
      this.parsedXMLResult = new ArrayList<>();
      this.parsedXMLStringResult = new ArrayList<>();
      this.exportInfoContainer = Objects.requireNonNull(exportInfoContainer);
      this.classifier = Objects.requireNonNull(classifier);
      this.exportMode = Objects.requireNonNull(exportInfoContainer.getExportMode());
      this.tarifziffer = requireNonNull(tarifziffer);
      init();
   }

   private void init() {
      // initialize the InvoiceAttrs in order to build all maps of Attrs
      InvoiceAttrs.INSTANCE.init();
      if (exportMode.isWekaExport()) {
         LabelEvaluator.INSTANCE.init();
      }
   }

   /**
    * Selects the content from a given location and exports it to the users Desktop
    */
   public void selectAndExportContent() {
      logAppStarted();
      parseAndCollectResults();
      purgeResults();
      doFeatureEngineering();
      mergeResults();
      convertResultAndCollect();
      exportResults();
      logAppFinished();
   }

   /**
    * parses all the xml files in the given folder and builds the internal model
    */
   private void parseAndCollectResults() {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start parse and collecting results..");
      File folder = new File(exportInfoContainer.getInputDirectory());
      parsedXMLResult = getFiles(folder)
            .parallelStream()
            .map(parseAndBuildResult())
            .collect(Collectors.toList());
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done parse and collecting '" + parsedXMLResult.size() + "' results %s\n");
   }

   private void doFeatureEngineering() {
      InvoicesFeatureEngineerer xmlFileFeatureEngineerer = new InvoicesFeatureEngineererImpl(FeatureEngineeres.getAllFeatureEngineerers());
      parsedXMLResult = xmlFileFeatureEngineerer.doFeatureEngineering(parsedXMLResult);
   }

   private void mergeResults() {
      ResultMerger resultMerger = ResultMergerFactory.INSTANCE.getResultMerger(exportMode, classifier);
      parsedXMLResult = resultMerger.mergeLineContent(parsedXMLResult);
   }

   private void purgeResults() {
      InvoiceDataCleaner invoiceDataCleaner = new InvoiceDataCleanerImpl(exportMode.isRawExport());
      parsedXMLResult = invoiceDataCleaner.purgeResults(parsedXMLResult);
   }

   private void convertResultAndCollect() {
      InvoiceContentExportContentCollector invoiceExportContentCollector = getContentCollector();
      parsedXMLStringResult = invoiceExportContentCollector.collectContent();
   }

   private void exportResults() {
      ExportInfo exportInfo = new ExportInfo(parsedXMLStringResult, exportMode.getFileExtension(), exportInfoContainer.getOutputFileName(),
            exportInfoContainer.getOutputDirectory());
      FileExporter.INSTANCE.export(exportInfo);
   }

   private Function<File, XMLFileParseResult> parseAndBuildResult() {
      return file -> parseXML(getSAXParser(), file, createXMLInvoiceContentParserHandler());
   }

   private XMLInvoiceContentParserHandler createXMLInvoiceContentParserHandler() {
      List<XMLContentCollector> xmlContentCollectors = Arrays.asList(
            new XMLTarifzifferContentCollector(tarifzifferPredicate()),
            new XMLPatientContentCollector(),
            //            new XMLInvoiceBalanceContentCollector(),
            new XMLTreatmentContentCollector());
      return new XMLInvoiceContentParserHandler(xmlContentCollectors);
   }

   private static SAXParser getSAXParser() {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(true);
      try {
         SAXParser newSAXParser = factory.newSAXParser();
         newSAXParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
         newSAXParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
         return newSAXParser;
      } catch (ParserConfigurationException | SAXException e) {
         throw new XMLParseException(e);
      }
   }

   private static XMLFileParseResult parseXML(SAXParser saxParser, File file, XMLInvoiceContentParserHandler tarifzifferContentParserHandler) {
      try {
         saxParser.parse(file, tarifzifferContentParserHandler);
         return XMLFileParseResult.of(file.getName(), tarifzifferContentParserHandler.getRecordTarmedContent());
      } catch (SAXException | IOException e) {
         throw new XMLParseException(e);
      }
   }

   private InvoiceContentExportContentCollector getContentCollector() {
      if (exportMode == ExportMode.COUNT_SINGLE_TARIFZIFFER) {
         return new TarifzifferCounterExportContentCollectorImpl(tarifziffer, parsedXMLResult);
      } else if (exportMode.isMergedExport()) {
         return new InvoiceMergedExportContentCollectorImpl(parsedXMLResult, exportMode, exportInfoContainer.isOmitHeader());
      } else {
         return new InvoiceExportContentCollectorImpl(parsedXMLResult, exportMode, exportInfoContainer.isOmitHeader());
      }
   }

   private String requireNonNull(String tarifziffer) {
      if (exportMode.isSingleTarifzifferExport() && isNull(tarifziffer)) {
         throw new NullPointerException("Tarifziffer is mandatory for ExportMode '" + exportMode + "'");
      }
      return tarifziffer;
   }

   private Predicate<String> tarifzifferPredicate() {
      return exportMode.isSingleTarifzifferExport() ? tarifziffer::equals : Objects::nonNull;
   }

   private List<File> getFiles(File folder) {
      ExportRange exportRange = exportInfoContainer.getExportRange();
      File[] listFiles = folder.listFiles() != null ? folder.listFiles() : new File[0];

      int rangeEnd = Math.min(exportRange.getEnd(), listFiles.length);
      List<File> sortedFilesInFolder = getSortedFiles(listFiles);
      List<File> files2Import = new ArrayList<>();
      for (int i = exportRange.getBegin(); i < rangeEnd; i++) {
         files2Import.add(sortedFilesInFolder.get(i));
      }
      return files2Import;
   }

   // Sort all files in order to guarantee the same sequence of the files at each run
   private List<File> getSortedFiles(File[] listFiles) {
      return Arrays.asList(listFiles)
            .stream()
            .filter(isRelevant())
            .sorted(Comparator.comparing(File::getName))
            .collect(Collectors.toList());
   }

   /*
    * If we export the labels (e.g. WEKA-Export) we need to exclude files for which we don't have any label
    * I can't explain why but the rule based system does (obviously) not import and test each invoice..
    */
   private Predicate<File> isRelevant() {
      return file -> !exportMode.isWekaExport() || LabelEvaluator.INSTANCE.hasLabel(file.getName());
   }

   private static void logAppFinished() {
      LoggerHelper.INSTANCE.endLogInfo(LOG, "XMLInvoiceContentParser finished %s\n", 1);
   }

   private void logAppStarted() {
      String rangeRep = "Export range: " + exportInfoContainer.getExportRange().getBegin() + " - " + exportInfoContainer.getExportRange().getEnd();
      String omitHeaderRep = "omit header: " + exportInfoContainer.isOmitHeader();
      String usingClassifierRep = "classifier: " + classifier;
      String exportModeRep = "export-mode: " + exportInfoContainer.getExportMode();
      String tarifzifferRep = "tarifziffer: " + tarifziffer;
      LoggerHelper.INSTANCE.startLogInfo(LOG,
            "XMLInvoiceContentParser started with " + exportModeRep + "; "
                  + rangeRep + "; "
                  + tarifzifferRep + "; "
                  + omitHeaderRep + "; "
                  + usingClassifierRep + System.lineSeparator(),
            1);
   }
}
