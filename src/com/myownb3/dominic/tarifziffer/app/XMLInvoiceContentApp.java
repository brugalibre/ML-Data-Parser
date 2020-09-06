package com.myownb3.dominic.tarifziffer.app;

import org.apache.log4j.Logger;

import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.parse.ExportInfoContainer;
import com.myownb3.dominic.tarifziffer.core.parse.ExportRange;
import com.myownb3.dominic.tarifziffer.core.parse.XMLInvoiceContentParser;
import com.myownb3.dominic.tarifziffer.mlclassifier.MLClassifiers;

public class XMLInvoiceContentApp {

   private static final Logger LOG = Logger.getLogger(XMLInvoiceContentApp.class);

   private XMLInvoiceContentApp() {
      // private 
   }

   public static void main(String[] args) {
      validateArguments(args);
      String inputPath = args[0];
      String outputPath = args[1];
      String outputFileName = args[2];
      String tarifziffer = args[3];
      ExportMode exportMode = ExportMode.valueOf(args[4]);
      ExportRange exportRange = buildExportRange(args);
      boolean omitHeader = isOmitHeader(args);
      MLClassifiers mlClassifier = getMLClassifier(args);
      outputFileName = exportRange.getBegin() + "_" + exportRange.getEnd() + "_" + outputFileName;
      ExportInfoContainer exportInfoContainer = new ExportInfoContainer(exportRange, exportMode, inputPath, outputFileName, outputPath, omitHeader);
      XMLInvoiceContentParser xmlParser = new XMLInvoiceContentParser(exportInfoContainer, tarifziffer, mlClassifier);
      xmlParser.selectAndExportContent();
   }

   private static ExportRange buildExportRange(String[] args) {
      if (args.length >= 7) {
         int begin = Integer.parseInt(args[5]);
         int end = Integer.parseInt(args[6]);
         return new ExportRange(begin, end);
      }
      return new ExportRange();
   }

   private static boolean isOmitHeader(String[] args) {
      if (args.length >= 8) {
         return Boolean.valueOf(args[7]);
      }
      return false;
   }

   private static MLClassifiers getMLClassifier(String[] args) {
      if (args.length == 9) {
         return MLClassifiers.valueOf(args[8]);
      }
      return MLClassifiers.ANY_OTHER;
   }

   private static void validateArguments(String[] args) {
      if (args.length < 5 || args.length > 9) {
         LOG.error("Invalid amount of arguments!" + System.lineSeparator()
               + "Usage: 'java <vm-arguments> -jar XMLInvoiceContentApp.jar input-path output-path output-file-name tarifziffer export-mode [begin-of-the-export-Range] [end-of-the-export-Range] [omit-header] [ml-classifier]'"
               + System.lineSeparator()
               + "Example #1: java -Xmx25G -jar XMLInvoiceContentApp.jar /home/work/input/ /home/work/input/output/ all_train_data 00.0020 EXPORT_SINGLE_TARIFZIFFER_MERGED_WEKA"
               + System.lineSeparator()
               + "Example #2: java -jar XMLInvoiceContentApp.jar /home/work/input/ /home/work/input/output/ 0_1_train_data i-dont-care EXPORT_ALL_TARIFZIFFER_MERGED_WEKA 0 11000 false NAIVE_BAYES"
               + System.lineSeparator()
               + "Note #1: Depending on the used exportMode, the argument 'tarifziffer' is mandatory but not used (e.g. this is true for all export modes starting with 'EXPORT_ALL_')!"
               + System.lineSeparator()
               + "Note #2: Whereas it's possible to omit optional arguments at the very end like 'mlClassifier', it is not possible to omit preceding ones like 'beginExportRange'!"
               + System.lineSeparator()
               + "Possible exportModes: " + getExportModesRep()
               + System.lineSeparator()
               + "Possible mlClassifiers: " + getMLClassifierRep());
         System.exit(-1);
      }
   }

   private static String getMLClassifierRep() {
      return getObjectArrayRep(MLClassifiers.values());
   }

   private static String getExportModesRep() {
      return getObjectArrayRep(ExportMode.values());
   }

   private static String getObjectArrayRep(Object[] objects) {
      StringBuilder sb = new StringBuilder(System.lineSeparator());
      for (Object object : objects) {
         sb.append(" - " + object.toString() + System.lineSeparator());
      }
      return sb.toString();
   }
}
