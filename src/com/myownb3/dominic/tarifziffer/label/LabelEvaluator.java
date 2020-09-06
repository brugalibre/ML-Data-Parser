package com.myownb3.dominic.tarifziffer.label;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.myownb3.dominic.tarifziffer.core.parse.content.constants.ContentConstants;
import com.myownb3.dominic.tarifziffer.io.FileSystemUtil;
import com.myownb3.dominic.tarifziffer.io.input.FileImporter;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;
import com.myownb3.dominic.tarifziffer.random.RandomNumber;

/**
 * The {@link LabelEvaluator} is responsible for evaluating an already existing
 * label for a specific invoice-no
 * 
 * @author Dominic
 *
 */
public class LabelEvaluator {

   public static final LabelEvaluator INSTANCE = new LabelEvaluator();
   private static final Logger LOG = Logger.getLogger(LabelEvaluator.class);
   private String path;
   private String fileName;
   private Map<String, String> invoiceId2LabelMap;
   private List<String> labels;
   private boolean isInitialized;

   /**
    * Default constructor. Loads the file from the path 'res\input\label\' and with the default
    * name 'invoiceId2label.txt"
    */
   private LabelEvaluator() {
      String separator = FileSystemUtil.getDefaultFileSystemSeparator();
      this.path = "res" + separator + "input" + separator + "labels" + separator;
      this.fileName = "labeledInvoices.txt";
      this.invoiceId2LabelMap = new HashMap<>();
      this.labels = new ArrayList<>();
      this.isInitialized = false;
   }

   /**
    * @return the possible labels for an invoice
    */
   public List<String> getLabels() {
      return labels;
   }

   /**
    * Returns the label mapped to the given invoice
    * 
    * @param invoiceId
    *        the id of the invoice
    * @return the mapped label
    */
   public String getLabel(String invoiceId) {
      if (hasLabel(invoiceId)) {
         return invoiceId2LabelMap.get(invoiceId);
      }
      throw new IllegalStateException("There is no label for Invoice with id '" + invoiceId + "'!");
   }

   /**
    * Return <code>true</code> if there is a label associated with the given invoice id otherwise <code>false</code>
    * 
    * @param invoiceId
    *        the id of the invoice
    * @return <code>true</code> if there is a label associated with the given invoice id otherwise <code>false</code>
    */
   public boolean hasLabel(String invoiceId) {
      return invoiceId2LabelMap.containsKey(invoiceId);
   }

   /**
    * Initialises this {@link LabelEvaluator}
    */
   public void init() {
      if (isInitialized) {
         return;
      }
      int requestId = RandomNumber.getNext();
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start initializing..", requestId);
      File file = new File(path + fileName);
      List<String> importFile = FileImporter.INTANCE.importFile(file);
      invoiceId2LabelMap = importFile.parallelStream()
            .map(split2KeyValuePairs())
            .collect(collect2Map());
      labels = new ArrayList<>(invoiceId2LabelMap.values())
            .parallelStream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
      this.isInitialized = true;
      LoggerHelper.INSTANCE.endLogInfo(LOG, "End initializing %s\n", requestId);
   }

   private static Function<String, String[]> split2KeyValuePairs() {
      return mapping -> mapping.split(ContentConstants.ELEMENT_DELIMITER);
   }

   private static Collector<String[], ?, Map<String, String>> collect2Map() {
      return Collectors.toMap(keyValuePair -> keyValuePair[0], keyValuePair -> keyValuePair[1]);
   }
}
