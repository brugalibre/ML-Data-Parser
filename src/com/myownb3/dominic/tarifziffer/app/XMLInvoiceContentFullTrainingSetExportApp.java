package com.myownb3.dominic.tarifziffer.app;

import static java.lang.Math.min;

import org.apache.log4j.Logger;

import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.parse.ExportInfoContainer;
import com.myownb3.dominic.tarifziffer.core.parse.ExportRange;
import com.myownb3.dominic.tarifziffer.core.parse.XMLInvoiceContentParser;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;
import com.myownb3.dominic.tarifziffer.mlclassifier.MLClassifiers;
import com.myownb3.dominic.tarifziffer.random.RandomNumber;

public class XMLInvoiceContentFullTrainingSetExportApp {

   private static final String DATA_SUFFIX = "_data";
   private static final Logger LOG = Logger.getLogger(XMLInvoiceContentFullTrainingSetExportApp.class);

   private XMLInvoiceContentFullTrainingSetExportApp() {
      // private 
   }

   public static void main(String[] args) {
      validateArguments(args);
      String inputPath = args[0];
      String outputPath = args[1];
      ExportRange totalExportRange = buildExportRange(args);
      int currentExportRangeBegin = totalExportRange.getBegin();
      boolean omitHeader = isOmitHeader(args);
      MLClassifiers mlClassifier = getMLClassifier(args);

      int requestId = RandomNumber.getNext();
      startLogInfo(currentExportRangeBegin, totalExportRange.getEnd(), requestId);
      for (TrainingDataType trainingDataType : TrainingDataType.values()) {
         ExportRange currentExportRange = buildCurrentExportRange(currentExportRangeBegin, totalExportRange, trainingDataType);
         String outputFileName = buildCurrentFileName(trainingDataType, currentExportRange, mlClassifier);
         ExportInfoContainer exportInfoContainer = buildExportInfoContainer(inputPath, outputPath, omitHeader, currentExportRange, outputFileName);
         XMLInvoiceContentParser xmlParser = new XMLInvoiceContentParser(exportInfoContainer, "na", mlClassifier);
         xmlParser.selectAndExportContent();

         currentExportRangeBegin = currentExportRange.getEnd();
      }
      endLogInfo(requestId);
   }

   private static ExportRange buildCurrentExportRange(int currentExportRangeBegin, ExportRange totalExportRange, TrainingDataType trainingDataType) {
      int relativeAmountOfElements4DataType = (int) Math.round(totalExportRange.getSize() * trainingDataType.ratio);
      int currentExportRangeEnd = currentExportRangeBegin + relativeAmountOfElements4DataType;
      return new ExportRange(currentExportRangeBegin, min(totalExportRange.getEnd(), currentExportRangeEnd));
   }

   private static ExportRange buildExportRange(String[] args) {
      int begin = Integer.parseInt(args[2]);
      int end = Integer.parseInt(args[3]);
      return new ExportRange(begin, end);
   }

   private static ExportInfoContainer buildExportInfoContainer(String inputPath, String outputPath, boolean omitHeader,
         ExportRange currentExportRange, String outputFileName) {
      return new ExportInfoContainer(currentExportRange, ExportMode.EXPORT_ALL_TARIFZIFFER_MERGED_WEKA, inputPath,
            outputFileName, outputPath, omitHeader);
   }

   private static String buildCurrentFileName(TrainingDataType trainingDataType, ExportRange currentExportRange, MLClassifiers mlClassifier) {
      String mlClassInName = mlClassifier == MLClassifiers.NAIVE_BAYES ? "_nb_" : "_";
      return currentExportRange.getBegin() + "_" + currentExportRange.getEnd() + mlClassInName + trainingDataType.name + DATA_SUFFIX;
   }

   private static boolean isOmitHeader(String[] args) {
      if (args.length >= 5) {
         return Boolean.valueOf(args[4]);
      }
      return false;
   }

   private static MLClassifiers getMLClassifier(String[] args) {
      if (args.length == 6) {
         return MLClassifiers.valueOf(args[5]);
      }
      return MLClassifiers.ANY_OTHER;
   }

   private static void validateArguments(String[] args) {
      if (args.length < 4 || args.length > 6) {
         LOG.error("Invalid amount of arguments!" + System.lineSeparator()
               + "Usage: 'java <vm-arguments> -jar XMLInvoiceContentFullTrainingSetExportApp.jar input-path output-path begin-of-the-export-Range end-of-the-export-Range [omit-header] [ml-classifier]'"
               + System.lineSeparator()
               + "Example #1: java -Xmx25G -jar XMLInvoiceContentFullTrainingSetExportApp.jar /home/work/input/ /home/work/input/output/ 0 11000"
               + System.lineSeparator()
               + "Example #2: java -jar XMLInvoiceContentFullTrainingSetExportApp.jar /home/work/input/ /home/work/input/output/ 10000 20000 false NAIVE_BAYES"
               + System.lineSeparator()
               + "Note #2: Whereas it's possible to omit optional arguments at the very end like 'mlClassifier', it is not possible to omit preceding ones like 'omit-header'!"
               + System.lineSeparator()
               + "Possible mlClassifiers: " + getMLClassifierRep());
         System.exit(-1);
      }
   }

   private static String getMLClassifierRep() {
      return getObjectArrayRep(MLClassifiers.values());
   }

   private static String getObjectArrayRep(Object[] objects) {
      StringBuilder sb = new StringBuilder(System.lineSeparator());
      for (Object object : objects) {
         sb.append(" - " + object.toString() + System.lineSeparator());
      }
      return sb.toString();
   }

   private static void endLogInfo(int requestId) {
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done with the full training-data set export %s\n", requestId);
   }

   private static void startLogInfo(int currentExportRangeBegin, int exportRangeEnd, int requestId) {
      LoggerHelper.INSTANCE.startLogInfo(LOG,
            "Start the export of a full training-data set within the range " + currentExportRangeBegin + " - " + exportRangeEnd,
            requestId);
   }

   private enum TrainingDataType {
      TRAIN(0.7, "train"),
      TEST(0.23, "test"),
      DEV(0.07, "dev");

      private double ratio;
      private String name;

      private TrainingDataType(double ratio, String name) {
         this.name = name;
         this.ratio = ratio;
      }
   }
}
