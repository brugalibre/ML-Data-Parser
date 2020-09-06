
package com.myownb3.dominic.tarifziffer.core.parse;

import static java.util.Objects.isNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.myownb3.dominic.tarifziffer.io.output.FileExporter;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;

public class XMLInvoiceContentParser {

   private static final Logger LOG = Logger.getLogger(XMLInvoiceContentParser.class);
   private String tarifziffer;
   private List<XMLFileParseResult> result;
   private ExportMode exportMode;
   private ExportData exportData;

   public XMLInvoiceContentParser(ExportData exportData, String tarifziffer, ExportMode exportMode) {
      this.result = new ArrayList<>();
      this.exportData = Objects.requireNonNull(exportData);
      this.exportMode = Objects.requireNonNull(exportMode);
      this.tarifziffer = requireNonNull(tarifziffer);
   }

   /**
    * Selects the content from a given location and exports it to the users Desktop
    */
   public void selectAndExportContent() {
      parseAndCollectResults();
      purgeResults();
      doFeatureEngineering();
      mergeResults();
      exportResults();
   }

   /**
    * parses all the xml files in the given folder and builds the internal model
    * 
    * @return a list of {@link XMLFileParseResult} which contained the parsed results
    */
   public List<XMLFileParseResult> selectAndParseResults() {
      parseAndCollectResults();
      return result;
   }

   /**
    * parses all the xml files in the given folder and builds the internal model
    */
   private void parseAndCollectResults() {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start collecting '" + exportData.getExportRange().getSize() + "' results..");
      File folder = new File(exportData.getXmlContentFolder());
      result = getFiles(folder)
            .parallelStream()
            .map(parseAndBuildResult())
            .collect(Collectors.toList());
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done collecting results %s \n");
   }

   private void doFeatureEngineering() {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start feature eingeering");
      InvoicesFeatureEngineerer xmlFileFeatureEngineerer = new InvoicesFeatureEngineererImpl(FeatureEngineeres.getAllFeatureEngineerers());
      result = xmlFileFeatureEngineerer.doFeatureIngeneering(result);
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done feature eingeering %s \n");
   }

   private void mergeResults() {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start merging results..");
      ResultMerger resultMerger = ResultMergerFactory.INSTANCE.getResultMerger(exportMode);
      result = resultMerger.mergeLineContent(result);
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done merging results %s \n");
   }

   private void purgeResults() {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start purging results, call InvoiceDataCleaner ");
      InvoiceDataCleaner invoiceDataCleaner = new InvoiceDataCleanerImpl(exportMode.isRawExport());
      result = invoiceDataCleaner.purgeResults(result);
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done purging results %s \n");
   }

   private void exportResults() {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start exporting results");
      InvoiceContentExportContentCollector invoiceExportContentCollector = getContentCollector();
      FileExporter.INTANCE.export(invoiceExportContentCollector, exportData.getOutputDirectory());
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done exporting results %s \n");
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
         return new TarifzifferCounterExportContentCollectorImpl(tarifziffer, result);
      } else if (exportMode.isMergedExport()) {
         return new InvoiceMergedExportContentCollectorImpl(result, exportData.getOutputFileName(), exportMode);
      } else {
         return new InvoiceExportContentCollectorImpl(result, exportData.getOutputFileName(), exportMode);
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
      ExportRange exportRange = exportData.getExportRange();
      if (exportRange.getEnd() == Integer.MAX_VALUE) {
         return Arrays.asList(folder.listFiles());
      }
      File[] listFiles = folder.listFiles();
      List<File> files2Import = new ArrayList<>(exportRange.getSize());
      for (int i = 0; i < listFiles.length; i++) {
         if (exportRange.isWithinRange(i)) {
            files2Import.add(listFiles[i]);
         } else {
            break;
         }
      }
      return files2Import;
   }
}
