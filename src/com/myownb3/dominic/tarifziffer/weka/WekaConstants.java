package com.myownb3.dominic.tarifziffer.weka;

/**
 * Contains the constant names used for weka
 * 
 * @author Dominic
 *
 */
public class WekaConstants {

   private WekaConstants() {
      // private 
   }

   /** String literal for @attribute annotation in weka */
   public static final String AT_ATTRIBUTE = "@attribute";

   /** String literal for '@relation' used in weka */
   public static final String AT_RELATION = "@relation";

   /** String literal for '@data' used in weka */
   public static final String AT_DATA = "@data";

   /** The String pattern for categorical values, like {values} */
   public static final String CATEGORICAL_VALUES_PATTERN = "{%s}";

   /** String literal for 'numeric' in weka */
   public static final String NUMERIC = "numeric";

   /** String literal for 'classification' */
   public static final String CLASSIFIER_ID = "classification";

   /** Line delimiter in weka */
   public static final String LINE_DELIMITER = ",";

   /** The name of the relation of the weka data, e.g. the name or type of data the arff file contains */
   public static final String RELATION_NAME = "invoice-data";
}
