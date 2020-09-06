package com.myownb3.dominic.tarifziffer.core.parse.content.collector;

import org.xml.sax.Attributes;

import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.tarifziffer.core.parse.handler.XMLInvoiceContentParserHandler;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;

/**
 * The {@link XMLContentCollector} is used to collect the data which is contained within one or more xml-tag.
 * Note that all this data should be of the same {@link ContentType}.
 * This is done by using a {@link XMLInvoiceContentParserHandler}
 * 
 * @author Dominic
 *
 */
public interface XMLContentCollector {

   /**
    * Collects data from the given {@link Attributes}
    * 
    * @param parentQName
    *        the name of the parent xml-tag
    * @param attributes
    *        the given {@link Attributes}
    * @return the content
    */
   LineContent collectContent(String parentQName, Attributes attributes);

   /**
    * Verifies if the given attributes within the given xml-tag (qName) are relevant for this {@link XMLContentCollector}
    * 
    * @param qName
    *        the name of the current checked xml-tag
    * @param attributes
    *        the current parsed {@link Attributes}
    * @return <code>true</code> if this {@link XMLContentCollector} is sensitive to the given {@link Attributes} or <code>false</code> if
    *         not
    */
   boolean areAttributesRelevant(String qName, Attributes attributes);
}
