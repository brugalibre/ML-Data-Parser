package com.myownb3.dominic.tarifziffer.app;

import org.apache.log4j.Logger;

import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.parse.ExportData;
import com.myownb3.dominic.tarifziffer.core.parse.ExportRange;
import com.myownb3.dominic.tarifziffer.core.parse.XMLInvoiceContentParser;

public class IncrementalXMLInvoiceContentApp {

   private static final Logger LOG = Logger.getLogger(IncrementalXMLInvoiceContentApp.class);
   private static final int TOTAL_AMOUNT_INVOICES = 100000;
   private static final int STEP_WIDTH = 50;

   private IncrementalXMLInvoiceContentApp() {
      // private 
   }

   public static void main(String[] args) {
      String inputPath = args[0];
      String outputPath = args[1];
      String outputFileName = args[2];
      String tarifziffer = args[3];
      ExportMode exportMode = ExportMode.valueOf(args[4]);
      int totalAmountInvoices = args.length > 5 ? Integer.parseInt(args[5]) : TOTAL_AMOUNT_INVOICES;
      startIncrementalParser(inputPath, outputPath, outputFileName, tarifziffer, exportMode, totalAmountInvoices);
   }

   /*
    * Proceed all invoices step by step in order to prevent a OOM-Exception 
    */
   private static void startIncrementalParser(String inputPath, String outputPath, String outputFileName,
         String tarifziffer, ExportMode exportMode, int totalAmountInvoices) {
      int counter = 0;
      while (counter < totalAmountInvoices) {
         int rangeEnd = counter + STEP_WIDTH;
         ExportRange exportRange = new ExportRange(counter, rangeEnd);
         LOG.error("Process invoices from '" + exportRange.getBegin() + "' to '" + exportRange.getEnd() + "'");
         ExportData exportData = new ExportData(exportRange, inputPath, outputFileName + "_" + rangeEnd, outputPath);
         XMLInvoiceContentParser xmlParser = new XMLInvoiceContentParser(exportData, tarifziffer, exportMode);
         xmlParser.selectAndExportContent();
         counter = rangeEnd;
      }
   }
}
