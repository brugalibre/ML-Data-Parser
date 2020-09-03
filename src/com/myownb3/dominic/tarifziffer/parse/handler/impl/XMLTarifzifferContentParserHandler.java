package com.myownb3.dominic.tarifziffer.parse.handler.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.myownb3.dominic.tarifziffer.parse.handler.XMLContentCollector;

public class XMLTarifzifferContentParserHandler extends DefaultHandler {
   private static final String RECORD_TARMED = "record_tarmed";
   private static final String TAG_NAME_CODE = "code";

   private Predicate<String> tarifzifferPredicate;
   private List<String> content;
   private XMLContentCollector xmlContentCollector;

   public XMLTarifzifferContentParserHandler(Predicate<String> tarifzifferPredicate, XMLContentCollector xmlContentCollector) {
      super();
      this.content = new ArrayList<>();
      this.tarifzifferPredicate = requireNonNull(tarifzifferPredicate);
      this.xmlContentCollector = requireNonNull(xmlContentCollector);
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      super.startElement(uri, localName, qName, attributes);
      if (isRecordTarmedElement(qName)) {
         String codeValue = attributes.getValue(TAG_NAME_CODE);
         if (isRelevantTarifzifferCode(codeValue)) {
            String contentLine = xmlContentCollector.collectContent(attributes);
            content.add(contentLine);
         }
      }
   }

   public List<String> getRecordTarmedContent() {
      return content;
   }

   private boolean isRelevantTarifzifferCode(String codeValue) {
      return tarifzifferPredicate.test(codeValue);
   }

   private boolean isRecordTarmedElement(String qName) {
      return containsElementValue(qName, RECORD_TARMED);
   }

   private boolean containsElementValue(String qName, String qValue) {
      return qName.contains(qValue);
   }
}
