package com.myownb3.dominic.tarifziffer.app;

import com.myownb3.dominic.tarifziffer.parse.XMLTarifzifferContentParser;

public class XMLTarifzifferContentApp {

   public static void main(String[] args) {
      XMLTarifzifferContentParser xmlParser = new XMLTarifzifferContentParser("G:\\Downloads\\output", "00.0020");
      xmlParser.selectAndExportContent();
   }
}
