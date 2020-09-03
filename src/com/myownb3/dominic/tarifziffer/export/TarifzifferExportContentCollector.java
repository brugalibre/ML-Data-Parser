package com.myownb3.dominic.tarifziffer.export;

import java.util.List;
import java.util.stream.Collectors;

import com.myownb3.dominic.tarifziffer.parse.result.XMLParseResult;

public class TarifzifferExportContentCollector {

   private String tarifZiffer;
   private List<XMLParseResult> result;

   public TarifzifferExportContentCollector(String tarifZiffer, List<XMLParseResult> result) {
      this.result = result;
      this.tarifZiffer = tarifZiffer;
   }

   public List<String> collectContent() {
      String title = "Total XML-Files die Tarifziffer '" + tarifZiffer + "' enthalten: '" + result.size() + "'\n\n";
      String headerLine = "File; Anzahl Vorkommen der Tarifziffer '" + tarifZiffer + "'; Inhalt\n";
      List<String> fileContent = result.stream()
            .map(XMLParseResult::createExportMsg)
            .collect(Collectors.toList());
      fileContent.add(0, headerLine);
      fileContent.add(0, title);
      return fileContent;
   }
}
