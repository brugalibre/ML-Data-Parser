package com.myownb3.dominic.tarifziffer.app.label;

import com.myownb3.dominic.tarifziffer.label.RandomInvoiceLabeler;

public class RandomInvoiceLabelerApp {

   public static void main(String[] args) {
      String path = args[0];
      String outputFileName = args[1];
      new RandomInvoiceLabeler(path, outputFileName).doRandomLabelingAndSafe2File();
   }
}
