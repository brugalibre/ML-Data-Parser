package com.myownb3.dominic.tarifziffer.core.parse.handler;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.myownb3.dominic.tarifziffer.core.parse.content.collector.XMLContentCollector;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.XMLContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLContentImpl;

/**
 * The {@link XMLInvoiceContentParserHandler} is a SAX-XML-Handler and is therefore
 * responsible for parsing a single invoice xml.
 * It contains multiple {@link XMLContentCollector} which are during the parsing used in order to collect the actual content
 * 
 * The content of this parsed invoice is then stored within a {@link XMLContent}
 * 
 * @author DStalder
 *
 */
public class XMLInvoiceContentParserHandler extends DefaultHandler {

   private XMLContent xmlContent;
   private List<XMLContentCollector> xmlContentCollectors;

   public XMLInvoiceContentParserHandler(List<XMLContentCollector> xmlContentCollectors) {
      super();
      this.xmlContent = new XMLContentImpl();
      this.xmlContentCollectors = requireNonNull(xmlContentCollectors);
   }

   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      super.startElement(uri, localName, qName, attributes);
      Optional<XMLContentCollector> xmlContentCollectorOpt = findXMLContentCollector(qName, attributes);
      xmlContentCollectorOpt.ifPresent(xmlContentCollector -> {
         LineContent lineContent = xmlContentCollector.collectContent(qName, attributes);
         xmlContent.add(lineContent);
      });
   }

   private Optional<XMLContentCollector> findXMLContentCollector(String qName, Attributes attributes) {
      return xmlContentCollectors.stream()
            .filter(areAttributesOrQNameRelevant(qName, attributes))
            .findFirst();
   }

   private static Predicate<XMLContentCollector> areAttributesOrQNameRelevant(String qName, Attributes attributes) {
      return xmlContentCollector -> xmlContentCollector.areAttributesRelevant(qName, attributes);
   }

   /**
    * @return the parsed and collected content of the xml file
    */
   public XMLContent getRecordTarmedContent() {
      return xmlContent;
   }
}
