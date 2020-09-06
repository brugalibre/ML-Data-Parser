package com.myownb3.dominic.tarifziffer.core.datacleaning;

import java.util.List;

import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

/**
 * The {@link InvoiceDataCleaner} is used to clean imported invoice data
 * This is e.g. used to set default values
 * 
 * @author Dominic
 *
 */
@FunctionalInterface
public interface InvoiceDataCleaner {

   /**
    * Purges and sorts the given {@link XMLFileParseResult}s
    * 
    * @param result
    *        the given {@link XMLFileParseResult}s
    * @return purged {@link XMLFileParseResult}s
    */
   List<XMLFileParseResult> purgeResults(List<XMLFileParseResult> result);
}
