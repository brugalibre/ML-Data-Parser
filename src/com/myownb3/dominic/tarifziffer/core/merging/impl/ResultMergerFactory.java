package com.myownb3.dominic.tarifziffer.core.merging.impl;

import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.merging.ResultMerger;
import com.myownb3.dominic.tarifziffer.mlclassifier.MLClassifiers;

public class ResultMergerFactory {

   public static final ResultMergerFactory INSTANCE = new ResultMergerFactory();

   private ResultMergerFactory() {
      // private 
   }

   /**
    * Creates a {@link ResultMerger} for merging the results
    * <b>Note:</b> Only with {@link ExportMode#EXPORT_SINGLE_TARIFZIFFER_MERGED} there is actually a {@link ResultMerger} which merges
    * something
    * 
    * @param exportMode
    *        the {@link ExportMode}
    * @param mlClassifier
    *        the classifier for which the xml are parsed
    * @return a {@link ResultMerger}
    */
   public ResultMerger getResultMerger(ExportMode exportMode, MLClassifiers mlClassifier) {
      if (exportMode.isMergedExport()) {
         return new DefaultResultMergerImpl(exportMode.isRawExport(), mlClassifier);
      }
      return xmlFileParseResult -> xmlFileParseResult;
   }
}
