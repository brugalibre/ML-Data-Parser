package com.myownb3.dominic.tarifziffer.label;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class LabelEvaluatorTest {

   @Test
   void testGetLabel_NoLabel() {

      // Given
      LabelEvaluator labelEvaluator = LabelEvaluator.INSTANCE;

      // When
      Executable ex = () -> labelEvaluator.getLabel("test");
      // Then
      assertThrows(IllegalStateException.class, ex);
   }

}
