package com.myownb3.dominic.tarifziffer.core.featureengineering.xmlcontent;

import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.XMLContent;

/**
 * A {@link XMLContentFeatureEngineerer} add additionally features to the given {@link XMLContent}
 * 
 * @author Dominic
 *
 */
public interface XMLContentFeatureEngineerer {

   /**
    * Perform some feature engineering considering the {@link LineContent} within the given {@link XMLContent}
    * 
    * @param xmlContent
    *        the {@link XMLContent} to engineer
    * @return a updated {@link XMLContent} or the same one, if there is nothing to do
    */
   public XMLContent doFeatureIngeneering(XMLContent xmlContent);
}
