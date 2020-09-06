package com.myownb3.dominic.tarifziffer.mlclassifier;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;

/**
 * Defines the different machine learning classifiers supported by this parser
 * 
 * @author Dominic
 *
 */
public enum MLClassifiers {

   /**
    * naive bayes. If this classifier is used, all values of nominal {@link InvoiceAttr} starts by one in order to avoid the zero frequency
    * problematic
    */
   NAIVE_BAYES,

   /** The classifier which will be user afterwards is not relevant */
   ANY_OTHER,
}
