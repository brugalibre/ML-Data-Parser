package com.myownb3.dominic.tarifziffer.core.parse.result;

import java.util.List;

import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.tarifziffer.core.parse.content.collector.XMLContentCollector;

/**
 * Represents the content of a entire xml-file a {@link XMLContentCollector} collects
 * 
 * @author DStalder
 *
 */
public interface XMLContent {

   /**
    * @return the amount of single {@link LineContent}s
    */
   int size();

   /**
    * @return <code>true</code> if this Content contains any {@link LineContent} which is from {@link ContentType#SERVICES_DATA} or
    *         <code>false</code> * if not
    */
   boolean hasServicesContent();

   /**
    * Adds the given {@link LineContent}
    * 
    * @param content
    */
   void add(LineContent content);

   /**
    * @return the all {@link LineContent}s of this {@link XMLContent}
    */
   List<LineContent> getContent();
}
