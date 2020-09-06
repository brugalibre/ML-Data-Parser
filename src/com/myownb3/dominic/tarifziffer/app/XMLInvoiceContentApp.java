package com.myownb3.dominic.tarifziffer.app;

import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.parse.ExportData;
import com.myownb3.dominic.tarifziffer.core.parse.ExportRange;
import com.myownb3.dominic.tarifziffer.core.parse.XMLInvoiceContentParser;

public class XMLInvoiceContentApp {

   private XMLInvoiceContentApp() {
      // private 
   }

   public static void main(String[] args) {
      String inputPath = args[0];
      String outputPath = args[1];
      String outputFileName = args[2];
      String tarifziffer = args[3];
      ExportMode exportMode = ExportMode.valueOf(args[4]);
      ExportData exportData = new ExportData(buildExportRange(args), inputPath, outputFileName, outputPath);
      XMLInvoiceContentParser xmlParser = new XMLInvoiceContentParser(exportData, tarifziffer, exportMode);
      xmlParser.selectAndExportContent();
   }

   private static ExportRange buildExportRange(String[] args) {
      if (args.length == 7) {
         int begin = Integer.parseInt(args[5]);
         int end = Integer.parseInt(args[6]);
         return new ExportRange(begin, end);
      }
      return new ExportRange();
   }
}
