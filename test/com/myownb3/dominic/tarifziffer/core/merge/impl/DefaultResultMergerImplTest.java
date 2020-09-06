package com.myownb3.dominic.tarifziffer.core.merge.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.myownb3.dominic.tarifziffer.core.merging.impl.DefaultResultMergerImpl;
import com.myownb3.dominic.tarifziffer.core.parse.result.impl.XMLFileParseResult;
import com.myownb3.dominic.tarifziffer.mlclassifier.MLClassifiers;

class DefaultResultMergerImplTest {

   @Test
   void testMergeLineContent() {

      // Given
      DefaultResultMergerImpl defaultResultMergerImpl = new DefaultResultMergerImpl(false, MLClassifiers.ANY_OTHER);
      List<XMLFileParseResult> result = Collections.emptyList();

      // When
      List<XMLFileParseResult> mergeLineContent = defaultResultMergerImpl.mergeLineContent(result);

      // Then
      assertThat(mergeLineContent, is(Collections.emptyList()));
   }

}
