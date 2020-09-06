package com.myownb3.dominic.tarifziffer.core.parse;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import com.myownb3.dominic.tarifziffer.io.FileSystemUtil;

public class ExportData {
   private static final String MISSING_OUTPUTDIRECTORY_CHAR = "-";
   private ExportRange exportRange;
   private String xmlContentFolder;
   private String outputFileName;
   private String outputDirectory;

   public ExportData(ExportRange exportRange, String xmlContentFolder, String outputFileName, String outputDirectory) {
      this.exportRange = requireNonNull(exportRange);
      this.xmlContentFolder = requireNonNull(xmlContentFolder);
      this.outputFileName = requireNonNull(outputFileName);
      this.outputDirectory = isNullOrEmpty(outputDirectory) ? FileSystemUtil.getHomeDir() : outputDirectory;
   }

   public ExportRange getExportRange() {
      return exportRange;
   }

   public String getXmlContentFolder() {
      return xmlContentFolder;
   }

   public String getOutputFileName() {
      return outputFileName;
   }

   public String getOutputDirectory() {
      return outputDirectory;
   }

   private static boolean isNullOrEmpty(String outputDirectory) {
      return isNull(outputDirectory) || MISSING_OUTPUTDIRECTORY_CHAR.equals(outputDirectory);
   }
}
