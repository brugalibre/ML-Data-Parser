package com.myownb3.dominic.tarifziffer.label;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.myownb3.dominic.tarifziffer.core.parse.content.constants.ContentConstants;
import com.myownb3.dominic.tarifziffer.io.input.FileImporter;

/**
 * The {@link LabelEvaluator} is responsible for evaluating an already existing
 * label for a specific invoice-no
 * 
 * @author Dominic
 *
 */
public class LabelEvaluator {

   private String path;
   private String fileName;
   private Map<String, String> invoiceId2LabelMap;

   /**
    * Default constructor. Loads the file from the path 'res\input\label\' and with the default
    * name 'invoiceId2label.txt"
    */
   public LabelEvaluator() {
      this.path = "res\\input\\labels\\";
      this.fileName = "labeledInvoices.txt";
      invoiceId2LabelMap = new HashMap<>();
   }

   /**
    * Returns the label mapped to the given invoice
    * 
    * @param invoiceId
    *        the id of the invoice
    * @return the mapped label
    */
   public String getLabel(String invoiceId) {
      if (invoiceId2LabelMap.containsKey(invoiceId)) {
         return invoiceId2LabelMap.get(invoiceId);
      }
      throw new IllegalStateException("There is no label for Invoice with id '" + invoiceId + "'!");
   }

   /**
    * Initialises this {@link LabelEvaluator}
    */
   public void init() {
      File file = new File(path + fileName);
      List<String> importFile = FileImporter.INTANCE.importFile(file);
      invoiceId2LabelMap = importFile.stream()
            .map(split2KeyValuePairs())
            .collect(collect2Map());
   }

   private static Function<String, String[]> split2KeyValuePairs() {
      return mapping -> mapping.split(ContentConstants.ELEMENT_DELIMITER);
   }

   private static Collector<String[], ?, Map<String, String>> collect2Map() {
      return Collectors.toMap(keyValuePair -> keyValuePair[0], keyValuePair -> keyValuePair[1]);
   }
}
