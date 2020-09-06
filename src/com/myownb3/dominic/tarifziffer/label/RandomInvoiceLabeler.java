package com.myownb3.dominic.tarifziffer.label;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.myownb3.dominic.invoice.attrs.constants.InvoiceConstants;
import com.myownb3.dominic.invoice.util.StringUtil;
import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.core.parse.ExportData;
import com.myownb3.dominic.tarifziffer.core.parse.ExportRange;
import com.myownb3.dominic.tarifziffer.core.parse.XMLInvoiceContentParser;
import com.myownb3.dominic.tarifziffer.core.parse.content.constants.ContentConstants;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;
import com.myownb3.dominic.tarifziffer.io.output.FileExporter;

public class RandomInvoiceLabeler {

   private String outputFileName;
   private XMLInvoiceContentParser xmlParser;

   public RandomInvoiceLabeler(String path, String outputFileName) {
      this.outputFileName = outputFileName;
      ExportData exportData = new ExportData(new ExportRange(), path, outputFileName, null);
      this.xmlParser = new XMLInvoiceContentParser(exportData, null, ExportMode.EXPORT_ALL_TARIFZIFFER_RAW);
   }

   public void doRandomLabelingAndSafe2File() {
      List<String> fileName2Labels = evalFileName2Label();
      FileExporter.INTANCE.export(fileName2Labels, outputFileName, ".txt");
   }

   private List<String> evalFileName2Label() {
      return xmlParser.selectAndParseResults()
            .stream()
            .map(evalLabelAndMap2Name())
            .collect(Collectors.collectingAndThen(Collectors.toList(), StringUtil.appendLineBreaks()));
   }

   private static Function<XMLFileParseResult, String> evalLabelAndMap2Name() {
      return xmlFileParsRes -> xmlFileParsRes.getXMLFileName() + ContentConstants.ELEMENT_DELIMITER + evalNextLabel();
   }

   private static String evalNextLabel() {
      double random = Math.random();
      if (random < 1 / 3d) {
         return InvoiceConstants.LABELS.get(0);
      } else if (random < 2 / 3d) {
         return InvoiceConstants.LABELS.get(1);
      } else {
         return InvoiceConstants.LABELS.get(2);
      }
   }
}
