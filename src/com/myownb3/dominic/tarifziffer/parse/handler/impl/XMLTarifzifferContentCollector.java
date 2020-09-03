package com.myownb3.dominic.tarifziffer.parse.handler.impl;

import org.xml.sax.Attributes;

import com.myownb3.dominic.tarifziffer.parse.handler.XMLContentCollector;

public class XMLTarifzifferContentCollector implements XMLContentCollector {
   private static final String IS_EQUAL = "=";
   private static final String RECORD_TARMED_CONTENT_ELEMENT_SEPARATOR = ", ";

   @Override
   public String collectContent(Attributes attributes) {
      StringBuilder contentLine = new StringBuilder();
      for (int i = 0; i < attributes.getLength(); i++) {
         contentLine.append(attributes.getQName(i) + IS_EQUAL);
         contentLine.append(attributes.getValue(i) + RECORD_TARMED_CONTENT_ELEMENT_SEPARATOR);
      }
      return contentLine.toString();
   }
}
