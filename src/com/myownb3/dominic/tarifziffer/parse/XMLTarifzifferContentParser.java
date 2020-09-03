package com.myownb3.dominic.tarifziffer.parse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.myownb3.dominic.tarifziffer.export.FileExporter;
import com.myownb3.dominic.tarifziffer.export.TarifzifferExportContentCollector;
import com.myownb3.dominic.tarifziffer.parse.exception.XMLParseException;
import com.myownb3.dominic.tarifziffer.parse.handler.impl.XMLTarifzifferContentCollector;
import com.myownb3.dominic.tarifziffer.parse.handler.impl.XMLTarifzifferContentParserHandler;
import com.myownb3.dominic.tarifziffer.parse.result.XMLParseResult;

public class XMLTarifzifferContentParser {

   private String xmlContentFolder;
   private String tarifziffer;
   private List<XMLParseResult> result;

   public XMLTarifzifferContentParser(String xmlContentFolder, String tarifziffer) {
      this.result = new ArrayList<>();
      this.xmlContentFolder = xmlContentFolder;
      this.tarifziffer = tarifziffer;
   }

   /**
    * Selects the content from a given location and exports it to the users Desktop
    */
   public void selectAndExportContent() {
      parseAndCollectResults();
      purgeResults();
      exportResults();
   }

   private void parseAndCollectResults() {
      File folder = new File(xmlContentFolder);
      result = Arrays.asList(folder.listFiles())
            .parallelStream()
            .map(parseAndBuildResult())
            .collect(Collectors.toList());
   }

   private void purgeResults() {
      result = new ArrayList<>(result)
            .stream()
            .filter(XMLParseResult::hasContent)
            .sorted(Comparator.comparing(XMLParseResult::getContentSize).reversed())
            .collect(Collectors.toList());
   }

   private void exportResults() {
      TarifzifferExportContentCollector tarifzifferContentExporter = new TarifzifferExportContentCollector(tarifziffer, result);
      FileExporter.INTANCE.export(tarifzifferContentExporter.collectContent());
   }

   private Function<File, XMLParseResult> parseAndBuildResult() {
      return file -> {
         SAXParser saxParser = getSAXParser();
         return parseXML(saxParser, file, createXMLTarifzifferContentParserHandler());
      };
   }

   private XMLTarifzifferContentParserHandler createXMLTarifzifferContentParserHandler() {
      return new XMLTarifzifferContentParserHandler(codeValue -> tarifziffer.equals(codeValue), new XMLTarifzifferContentCollector());
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

   private static XMLParseResult parseXML(SAXParser saxParser, File file, XMLTarifzifferContentParserHandler tarifzifferContentParserHandler) {
      try {
         saxParser.parse(file, tarifzifferContentParserHandler);
         return XMLParseResult.of(file.getName(), tarifzifferContentParserHandler.getRecordTarmedContent());
      } catch (SAXException | IOException e) {
         throw new XMLParseException(e);
      }
   }
}
