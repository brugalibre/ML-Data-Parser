package com.myownb3.dominic.tarifziffer.io.output;

import java.util.List;

/**
 * Contains the necessary informations about the content or destination to export
 * 
 * @author Dominic
 *
 */
public class ExportInfo {

   private String exportDir;
   private String fileExtension;
   private String exportFileName;
   private List<String> exportContent;

   public ExportInfo(List<String> exportContent, String fileExtension, String exportFileName, String exportDir) {
      this.exportContent = exportContent;
      this.exportFileName = exportFileName;
      this.exportDir = exportDir;
      this.fileExtension = fileExtension;
   }

   /**
    * @return the extension of the file in which the content is exported
    */
   public String getFileExtension() {
      return fileExtension;
   }

   /**
    * @return the name of the file in which the content is exported
    */
   public String getExportFileName() {
      return exportFileName;
   }

   /**
    * @return the directory to which the file is exported
    */
   public String getExportDir() {
      return exportDir;
   }

   /**
    * @return the actual content to export
    */
   public List<String> getExportContent() {
      return exportContent;
   }
}
