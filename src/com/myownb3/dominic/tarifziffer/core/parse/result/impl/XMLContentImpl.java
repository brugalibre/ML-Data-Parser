package com.myownb3.dominic.tarifziffer.core.parse.result.impl;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.XMLContent;

public class XMLContentImpl implements XMLContent {

   private List<LineContent> lineContent;

   public XMLContentImpl(List<LineContent> lineContent) {
      this.lineContent = requireNonNull(lineContent);
   }

   public XMLContentImpl() {
      lineContent = new ArrayList<>();
   }

   @Override
   public int size() {
      return lineContent.size();
   }

   @Override
   public boolean hasServicesContent() {
      return lineContent.stream()
            .anyMatch(LineContent::isServicesContent);
   }

   @Override
   public void add(LineContent content) {
      lineContent.add(content);
   }

   @Override
   public String toString() {
      return lineContent.toString();
   }

   @Override
   public List<LineContent> getContent() {
      return Collections.unmodifiableList(lineContent);
   }
}
