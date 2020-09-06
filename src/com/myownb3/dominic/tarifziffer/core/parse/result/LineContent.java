package com.myownb3.dominic.tarifziffer.core.parse.result;

import java.util.List;
import java.util.Optional;

import com.myownb3.dominic.invoice.attrs.metadata.InvoiceAttr;
import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.tarifziffer.core.parse.content.collector.XMLContentCollector;

/**
 * Represents a single parsed line of a XML-file which the content a {@link XMLContentCollector} collects
 * 
 * @author DStalder
 *
 */
public interface LineContent {

   /**
    * Returns the value associated with the given key or <code>null</code> if there is no such value
    * 
    * @param key
    *        the key
    * @return the value associated with the given key or <code>null</code> if there is no such value
    */
   String getValue(String key);

   /**
    * @return <code>true</code> if this {@link LineContent} is from type {@link ContentType#SERVICES_DATA}
    */
   boolean isServicesContent();

   /**
    * @return the {@link ContentType} of this {@link LineContent}
    */
   ContentType getContentType();

   /**
    * @return all the {@link InvoiceAttr} of this {@link LineContent}
    */
   List<InvoiceAttr> getInvoiceAttrs();

   /**
    * Returns the file name of the xml file this {@link LineContent} belongs
    * 
    * @return the file name of the xml file this {@link LineContent} belongs
    */
   Optional<String> getOptionalXMLFileName();
}
