package com.myownb3.dominic.tarifziffer.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LoggerHelper {

   public static final LoggerHelper INSTANCE = new LoggerHelper();
   private ThreadLocal<Long> startTimeStamp = new ThreadLocal<>();

   private LoggerHelper() {
      // private 
   }

   /**
    * Logs the given message with {@link Level#INFO} using the given logger and starts also a time stamp
    * This time stamp is used to indicate a duration between to logged messages
    * 
    * @param logger
    *        the {@link Logger}
    * @param msg
    *        the message to log
    * @see LoggerHelper#endLogInfo(Logger, String)
    */
   public void startLogInfo(Logger logger, String msg) {
      this.startTimeStamp.set(System.currentTimeMillis());
      logger.info(msg);
   }

   /**
    * Logs the given message with {@link Level#INFO} using the given logger and appends the duration between the previous logged message
    * and this one
    * 
    * @param logger
    *        the {@link Logger}
    * @param msg
    *        the message to log
    */
   public void endLogInfo(Logger logger, String msg) {
      String timeConsumedInfo = " (time consumed '" + (System.currentTimeMillis() - startTimeStamp.get()) + "'ms)";
      logger.info(String.format(msg, timeConsumedInfo));
   }
}
