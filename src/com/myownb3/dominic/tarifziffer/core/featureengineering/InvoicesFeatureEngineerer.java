package com.myownb3.dominic.tarifziffer.core.featureengineering;

import java.util.List;

import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

/**
 * A {@link InvoicesFeatureEngineerer} adds additionally features to the an entire {@link XMLFileParseResult}
 * 
 * @author Dominic
 *
 */
public interface InvoicesFeatureEngineerer {

   /**
    * Does the actual process of feature engineering and adds additionally features
    * to the given result
    * 
    * @param result
    *        the given result as a {@link XMLFileParseResult}
    * @return a copy of the given {@link XMLFileParseResult}s with additionally features
    */
   public List<XMLFileParseResult> doFeatureEngineering(List<XMLFileParseResult> result);
}
