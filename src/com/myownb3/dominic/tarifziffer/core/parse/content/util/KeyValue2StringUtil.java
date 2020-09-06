package com.myownb3.dominic.tarifziffer.core.parse.content.util;

import static java.util.Objects.isNull;

import java.util.List;
import java.util.Objects;

public class KeyValue2StringUtil {
   private static final String EMPTY_STRING = "";

   private KeyValue2StringUtil() {
      // private 
   }

   /**
    * Returns a String representation for all values within the given list
    * 
    * @param list
    *        the given list
    * @param lineDelimiter
    * @return a String representation for all values within the given list
    */
   public static String toString(List<? extends Object> list, String lineDelimiter) {
      return list.stream()
            .filter(Objects::nonNull)
            .map(Object::toString)
            .reduce(EMPTY_STRING, (a, b) -> concatObjects2String(a, b, lineDelimiter));
   }

   private static String concatObjects2String(Object a, Object b, String lineDelimiter) {
      return isEmptyString(a) ? toString(b) : toString(a) + lineDelimiter + toString(b);
   }

   private static boolean isEmptyString(Object object) {
      return EMPTY_STRING.equals(toString(object));
   }

   private static String toString(Object object) {
      return isNull(object) ? "" : object.toString();
   }
}
