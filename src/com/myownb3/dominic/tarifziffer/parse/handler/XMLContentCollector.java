package com.myownb3.dominic.tarifziffer.parse.handler;

import org.w3c.dom.Attr;
import org.xml.sax.Attributes;

@FunctionalInterface
public interface XMLContentCollector {

   /**
    * Collects data from the given {@link Attributes}
    * 
    * @param attributes
    *        the given {@link Attr}
    * @return a String representing the collected data
    */
   String collectContent(Attributes attributes);

}
