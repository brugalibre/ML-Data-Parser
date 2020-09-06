package com.myownb3.dominic.tarifziffer.core.parse.result.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;

import com.myownb3.dominic.tarifziffer.core.parse.content.constants.ContentConstants;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.XMLContent;

public class XMLFileParseResult {
   private String xmlName;
   private XMLContent xmlContent;

   private XMLFileParseResult(String xmlName, XMLContent xmlContent) {
      this.xmlName = requireNonNull(xmlName);
      this.xmlContent = requireNonNull(xmlContent);
   }

   /**
    * @return the size of the content
    */
   public int getContentSize() {
      return (int) xmlContent.getContent()
            .stream()
            .filter(LineContent::isServicesContent)
            .count();
   }

   /**
    * @return <code>true</code> if this result has content or <code>false</code> if not
    */
   public boolean hasContent() {
      return xmlContent.hasServicesContent();
   }

   /**
    * @return a String for an export
    */
   public String createExportMsg() {
      return xmlName + ContentConstants.ELEMENT_DELIMITER + xmlContent.size() + System.lineSeparator();
   }

   /**
    * @return the content of this result
    */
   public List<LineContent> getContent() {
      return xmlContent.getContent();
   }

   /**
    * @return the filename of the parsed file
    */
   public String getXMLFileName() {
      return xmlName;
   }

   /**
    * Creates a new {@link XMLFileParseResult}
    * 
    * @param xmlName
    *        the name of the parsed xml-file
    * @param xmlContent
    *        the parsed content
    * @return a new {@link XMLFileParseResult}
    */
   public static XMLFileParseResult of(String xmlName, XMLContent xmlContent) {
      return new XMLFileParseResult(xmlName, xmlContent);
   }
}
