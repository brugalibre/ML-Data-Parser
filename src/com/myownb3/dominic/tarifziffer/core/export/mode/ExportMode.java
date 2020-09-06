package com.myownb3.dominic.tarifziffer.core.export.mode;

/**
 * Defines the different modes of the app
 * 
 * @author DStalder
 *
 */
public enum ExportMode {

   /** Counts the occurrences of a single tarifziffer */
   COUNT_SINGLE_TARIFZIFFER(1, 0, 0, 0),

   /** Exports a single tarifziffer with it's tarmed content */
   EXPORT_SINGLE_TARIFZIFFER(1, 0, 0, 0),

   /** Raw export of a single tarifziffer with it's tarmed content */
   EXPORT_SINGLE_TARIFZIFFER_RAW(1, 1, 0, 0),

   /** Export of a single tarifziffer used for Weka */
   EXPORT_SINGLE_TARIFZIFFER_WEKA(1, 0, 0, 1),

   /** Export of a single tarifziffer merged together with it's tarmed content inlc. header */
   EXPORT_SINGLE_TARIFZIFFER_MERGED(1, 0, 1, 0),

   /** Raw export of a single tarifziffer merged together with it's tarmed content */
   EXPORT_SINGLE_TARIFZIFFER_MERGED_RAW(1, 1, 1, 0),

   /** Raw export of a single tarifziffer merged together with it's tarmed content */
   EXPORT_SINGLE_TARIFZIFFER_MERGED_WEKA(1, 1, 1, 1),

   /** Exports all tarifziffer with it's tarmed content */
   EXPORT_ALL_TARIFZIFFER(0, 0, 0, 0),

   /** Raw export of all tarifziffers with it's tarmed content */
   EXPORT_ALL_TARIFZIFFER_RAW(0, 1, 0, 0),

   /** Export of a single tarifziffer merged together with it's tarmed content and header */
   EXPORT_ALL_TARIFZIFFER_MERGED(0, 0, 1, 0),

   /** Raw export of a single tarifziffer merged together with it's tarmed content */
   EXPORT_ALL_TARIFZIFFER_MERGED_RAW(0, 1, 1, 0),

   /** Raw export of all tarifziffer merged together with it's tarmed content */
   EXPORT_ALL_TARIFZIFFER_MERGED_WEKA(0, 1, 1, 1),

   /** Export of a all tarifziffers used for Weka */
   EXPORT_ALL_TARIFZIFFER_WEKA(0, 0, 0, 1);

   private boolean isRawExport;
   private boolean isWekaExport;
   private boolean isMergedExport;
   private boolean isSingleExport;

   private ExportMode(int isSingleExportBit, int isRawExportBit, int isMergedExportBit, int isWekaExportBit) {
      this.isSingleExport = isSingleExportBit == 1;
      this.isWekaExport = isWekaExportBit == 1;
      this.isRawExport = isRawExportBit == 1 || isWekaExport;
      this.isMergedExport = isMergedExportBit == 1;
   }

   /**
    * @return the file extension for this kind of export
    */
   public String getFileExtension() {
      return isWekaExport() ? "arff" : "csv";
   }

   /**
    * @return the line delimiter according to this {@link ExportMode} and file extension
    */
   public String getLineDelimiter() {
      return isWekaExport() ? "," : ";";
   }

   /**
    * 
    * @return <code>true</code> if this {@link ExportMode} exports in raw-mode or <code>false</code> if not
    */
   public boolean isRawExport() {
      return isRawExport;
   }

   /**
    * @return <code>true</code> if the export is for Weka or <code>false</code> if not
    */
   public boolean isWekaExport() {
      return isWekaExport;
   }

   /**
    * @return <code>true</code> if this {@link ExportMode} is an merged export or <code>false</code> if not
    */
   public boolean isMergedExport() {
      return isMergedExport;
   }

   /**
    * @return <code>true</code> if this {@link ExportMode} exports a single tarifziffer or <code>false</code> if more than one are exported
    */
   public boolean isSingleTarifzifferExport() {
      return isSingleExport;
   }
}
