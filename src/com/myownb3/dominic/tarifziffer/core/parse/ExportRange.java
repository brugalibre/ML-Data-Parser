package com.myownb3.dominic.tarifziffer.core.parse;

public class ExportRange {
   private int size;
   private int begin;
   private int end;

   /**
    * New {@link ExportRange} is between the given two values
    */
   public ExportRange(int begin, int end) {
      this.begin = begin;
      this.end = end;
      this.size = end - begin;
   }

   /**
    * A increment must be equal or greater than the begin and smaller than the end in order to be
    * proceeded
    * 
    * @param inc
    *        the current increment
    * @return <code>true</code> if the current increment is between this range
    */
   public boolean isWithinRange(int inc) {
      return inc >= getBegin() && inc < getEnd();
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
