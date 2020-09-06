package com.myownb3.dominic.tarifziffer.core.parse.handler.content.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.myownb3.dominic.invoice.attrs.metadata.type.ContentType;
import com.myownb3.dominic.tarifziffer.core.parse.result.LineContent;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.LineContentImpl;

class LineContentImplTest {

   @Test
   void testGetContentType_Empty() {
      // Given
      LineContent lineContent = new LineContentImpl(Collections.emptyList());

      // When
      ContentType actualContentType = lineContent.getContentType();
      String actualToString = lineContent.toString();

      // Then
      assertThat(actualContentType, is(nullValue()));
      assertThat(actualToString, is(""));
   }
}
