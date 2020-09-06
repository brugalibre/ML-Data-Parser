package com.myownb3.dominic.tarifziffer.core.merging;

import java.util.List;

import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

/**
 * A {@link ResultMerger} merges all single {@link LineContent} of a {@link XMLFileParseResult} to one single line
 * 
 * @author Dominic
 *
 */
public interface ResultMerger {

   /**
    * Does the actual process of mergine the {@link LineContent}s
    * 
    * @param result
    *        the given result as a {@link XMLFileParseResult}
    * @return a copy of the given {@link XMLFileParseResult}s. with all {@link LineContent} merged together
    */
   public List<XMLFileParseResult> mergeLineContent(List<XMLFileParseResult> result);
}
