package com.myownb3.dominic.tarifziffer.parse.result;

import static java.util.Objects.requireNonNull;

import java.util.List;

public class XMLParseResult {
   private String xmlName;
   private List<String> xmlContent;

   private XMLParseResult(String xmlName, List<String> xmlContent) {
      this.xmlName = xmlName;
      this.xmlContent = requireNonNull(xmlContent);
   }

   public Integer getContentSize() {
      return xmlContent.size();
   }

   public boolean hasContent() {
      return !xmlContent.isEmpty();
   }

   public String createExportMsg() {
      return xmlName + ";" + xmlContent.size() + ";" + xmlContent + "\n";
   }

   public static XMLParseResult of(String xmlName, List<String> xmlContent) {
      return new XMLParseResult(xmlName, xmlContent);
   }
}
