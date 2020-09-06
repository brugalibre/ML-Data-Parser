package com.myownb3.dominic.tarifziffer.core.featureengineering.impl.linecontent;

import java.util.Arrays;
import java.util.List;

import com.myownb3.dominic.tarifziffer.core.featureengineering.linecontent.LineContentFeatureEngineerer;

/**
 * 
 * The {@link FeatureEngineeres} knows all available {@link LineContentFeatureEngineerer}s
 * 
 * @author Dominic
 *
 */
public class FeatureEngineeres {
   private FeatureEngineeres() {
      // private 
   }

   /**
    * @return all registered {@link LineContentFeatureEngineerer}s
    */
   public static List<LineContentFeatureEngineerer> getAllFeatureEngineerers() {
      return Arrays.asList(new PatientDataFeatureEngineererImpl(), new ServiceDataTreatmentDurationFeatureEngineererImpl());
   }
}
