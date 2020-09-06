package com.myownb3.dominic.tarifziffer.core.parse;

/**
 * The ExportRange defines the range to export including the {@link ExportRange#getBegin()} and exclusively {@link ExportRange#getEnd()}
 * 
 * @author Dominic
 *
 */
public class ExportRange {
   private int size;
   private int begin;
   private int end;

   /**
    * New {@link ExportRange} is between the given two values
    */
   public ExportRange(int begin, int end) {
      sanityCheck(begin, end);
      this.begin = begin;
      this.end = end;
      this.size = end - begin;
   }

   private void sanityCheck(int begin, int end) {
      if (begin < 0 || begin > end) {
         throw new IllegalStateException("Begin '" + begin + "' must not be smaller than 0 and not greater than '" + end + "'");
      }
   }

   /**
    * Default {@link ExportRange} is between 0 and {@link Integer#MAX_VALUE}
    */
   public ExportRange() {
      this.begin = 0;
      this.end = Integer.MAX_VALUE;
      this.size = Integer.MAX_VALUE;
   }

   public int getEnd() {
      return end;
   }

   public int getBegin() {
      return begin;
   }

   public int getSize() {
      return size;
   }
}
