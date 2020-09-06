package com.myownb3.dominic.tarifziffer.core.export.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;

public class TarifzifferCounterExportContentCollectorImpl extends AbstractInvoiceExportContentCollector {

   public TarifzifferCounterExportContentCollectorImpl(String tarifZiffer, List<XMLFileParseResult> result) {
      super();
      this.result = result;
      this.tarifZiffer = tarifZiffer;
   }

   @Override
   public List<String> collectContent() {
      beforeCollecting();
      String nl = System.lineSeparator();
      String title = "Total XML-Files die Tarifziffer '" + tarifZiffer + "' enthalten: '" + result.size() + "'" + nl + nl;
      String headerLine = "File; Anzahl Vorkommen der Tarifziffer '" + tarifZiffer + nl;
      List<String> fileContent = result.stream()
            .map(XMLFileParseResult::createExportMsg)
            .collect(Collectors.toList());
      fileContent.add(0, headerLine);
      fileContent.add(0, title);
      afterCollecting();
      return fileContent;
   }
}
