package com.myownb3.dominic.tarifziffer.core.featureengineering.linecontent;

import java.util.List;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

/**
 * A {@link LineContentFeatureEngineerer} add additionally features to the given list of {@link InvoiceAttr}s
 * 
 * @author Dominic
 *
 */
public interface LineContentFeatureEngineerer {

   /**
    * Does the actual process of feature engineering and adds additionally features
    * to the given result
    * 
    * @param invoiceAttrs
    *        the given {@link InvoiceAttr} which contains the {@link InvoiceAttr} to engineer
    * @param xmlFileParseResult
    *        the entire {@link XMLFileParseResult} with the other {@link LineContent}s
    * @return a copy of the given {@link InvoiceAttr}s with additionally features
    */
   public List<InvoiceAttr> doFeatureIngeneering(List<InvoiceAttr> invoiceAttrs, XMLFileParseResult xmlFileParseResult);

   /**
    * Return <code>true</code> if this {@link LineContentFeatureEngineerer} is responsible for the given {@link InvoiceAttr} otherwise returns
    * <code>false</code>
    * 
    * @param invoiceAttrs
    *        the given {@link InvoiceAttr} to verify
    * @return <code>true</code> if this {@link LineContentFeatureEngineerer} is responsible for the given {@link InvoiceAttr} otherwise returns
    *         <code>false</code>
    * 
    */
   boolean has2EngineerInvoiceAttrs(List<InvoiceAttr> invoiceAttrs);
}
