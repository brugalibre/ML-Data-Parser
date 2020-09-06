package com.myownb3.dominic.tarifziffer.core.merging.impl;

import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.merging.ResultMerger;

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
    * @return a {@link ResultMerger}
    */
   public ResultMerger getResultMerger(ExportMode exportMode) {
      if (exportMode.isMergedExport()) {
         return new DefaultResultMergerImpl(exportMode.isRawExport());
      }
      return xmlFileParseResult -> xmlFileParseResult;
   }
}
