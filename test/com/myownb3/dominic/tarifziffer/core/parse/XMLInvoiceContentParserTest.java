package com.myownb3.dominic.tarifziffer.core.parse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.myownb3.dominic.tarifziffer.app.XMLInvoiceContentApp;
import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.io.FileSystemUtil;
import com.myownb3.dominic.tarifziffer.io.input.FileImporter;

class XMLInvoiceContentParserTest {

   private static final String _00_0020 = "00.0020";
   private static final String TEST_RES_INVOICES_CASE1 = "test_res\\invoices_case1\\";
   private static final String TEST_RES_MEDICAL_ROLE_TEST_CASE = "test_res\\medical_role\\";

   @Test
   void testStartApp() throws IOException {
      // Given
      String fileName = "I_dont_care";
      String[] args = new String[5];
      args[0] = TEST_RES_INVOICES_CASE1;
      args[1] = "-";// export directory
      args[2] = fileName;
      args[3] = null;
      args[4] = "EXPORT_ALL_TARIFZIFFER";

      // When
      XMLInvoiceContentApp.main(args);

      // Then delete file
      File exportedFile = getFile(fileName);
      Files.delete(exportedFile.toPath());
      assertThat(exportedFile.exists(), is(false));
   }

   @Test
   void testStartAppWithNoValidPathExpectNoException() {
      // Given
      String fileName = "I_dont_care";
      String[] args = new String[5];
      args[0] = "test_res\\thisfolderdoesnotexist\\";
      args[1] = "-";// export directory
      args[2] = fileName;
      args[3] = null;
      args[4] = "EXPORT_ALL_TARIFZIFFER";

      // When
      XMLInvoiceContentApp.main(args);

      // Then expect no exception
   }

   @Test
   void testNoTarifzifferGivenForSingleExport() {
      // Given
      String tarifziffer = null;

      // When
      Executable ex = () -> {
         ExportInfoContainer exportInfoContainer =
               new ExportInfoContainer(new ExportRange(), ExportMode.COUNT_SINGLE_TARIFZIFFER, TEST_RES_INVOICES_CASE1, "", "");
         new XMLInvoiceContentParser(exportInfoContainer, tarifziffer);
      };

      // Then
      assertThrows(NullPointerException.class, ex);
   }

   @Test
   void testNoOutputDirectory() {
      // Given
      ExportMode exportMode = ExportMode.COUNT_SINGLE_TARIFZIFFER;
      String path = TEST_RES_INVOICES_CASE1;

      // When
      Executable ex = () -> {
         new ExportInfoContainer(new ExportRange(), exportMode, path, null, "");
      };

      // Then
      assertThrows(NullPointerException.class, ex);
   }

   @Test
   void testExportTarifziffer_SetTreatmentStationaryOrStationary() throws IOException {

      // Given
      String fileName = "test_export_statOrAmb";
      List<String> expectedContent = getExpectedContent_Treatment();
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.EXPORT_SINGLE_TARIFZIFFER)
            .withTarifziffer(_00_0020)
            .withInputPath("test_res\\invoices_case_treatment\\")
            .withFileName(fileName)
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile(fileName);
      List<String> importedContent = FileImporter.INTANCE.importFile(exportedFile);
      assertThat(importedContent.size(), is(4));
      assertContent(expectedContent, importedContent);
      Files.delete(exportedFile.toPath());
   }

   @Test
   void testExportTarifziffer_00_0020_WithHeader() throws IOException {

      // Given
      String fileName = "test_export1";
      List<String> expectedContent = getExpectedContent(true);
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.EXPORT_SINGLE_TARIFZIFFER)
            .withTarifziffer(_00_0020)
            .withInputPath(TEST_RES_INVOICES_CASE1)
            .withFileName(fileName)
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile(fileName);
      List<String> importedContent = FileImporter.INTANCE.importFile(exportedFile);
      assertThat(importedContent.size(), is(6));
      assertContent(expectedContent, importedContent);
      Files.delete(exportedFile.toPath());
   }

   @Test
   void testExportTarifziffer_00_0020_WithoutHeader() throws IOException {

      // Given
      String fileName = "test_export2";
      List<String> expectedContent = getExpectedContent(false);
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.EXPORT_SINGLE_TARIFZIFFER_RAW)
            .withTarifziffer(_00_0020)
            .withInputPath(TEST_RES_INVOICES_CASE1)
            .withFileName(fileName)
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile(fileName);
      List<String> importedContent = FileImporter.INTANCE.importFile(exportedFile);
      assertThat(importedContent.size(), is(5));
      assertContent(expectedContent, importedContent);
      Files.delete(exportedFile.toPath());
   }

   @Test
   void testExportTarifziffer_00_0020_DefaultValueMedicalRole_WithoutHeader() throws IOException {

      // Given
      String fileName = "test_export_medicalRoleTest";
      List<String> expectedContent = getExpectedMedicalRoleTestContent();
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.EXPORT_SINGLE_TARIFZIFFER_RAW)
            .withTarifziffer(_00_0020)
            .withInputPath(TEST_RES_MEDICAL_ROLE_TEST_CASE)
            .withFileName(fileName)
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile(fileName);
      List<String> importedContent = FileImporter.INTANCE.importFile(exportedFile);
      assertThat(importedContent.size(), is(1));
      assertContent(expectedContent, importedContent);
      Files.delete(exportedFile.toPath());
   }

   @Test
   void testExportTarifziffer_00_0020_WekaExport() throws IOException {

      // Given
      String fileName = "test_weka_export";
      List<String> expectedContent = getWekaExpectedContent();
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.EXPORT_SINGLE_TARIFZIFFER_WEKA)
            .withTarifziffer(_00_0020)
            .withInputPath(TEST_RES_INVOICES_CASE1)
            .withFileName(fileName)
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile(fileName, ".arff");
      List<String> importedContent = FileImporter.INTANCE.importFile(exportedFile);
      assertThat(importedContent.size(), is(41));
      assertContent(expectedContent, importedContent);
      Files.delete(exportedFile.toPath());
   }

   @Test
   void testExportTarifziffer_All_RawExport() throws IOException {

      // Given
      String fileName = "test_export3";
      List<String> expectedContent = getAllExpectedRawContent();
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.EXPORT_ALL_TARIFZIFFER_RAW)
            .withTarifziffer(null)
            .withInputPath(TEST_RES_INVOICES_CASE1)
            .withFileName(fileName)
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile(fileName, ".csv");
      List<String> importedContent = FileImporter.INTANCE.importFile(exportedFile);
      assertThat(importedContent.size(), is(206));
      assertContent(expectedContent, importedContent);
      Files.delete(exportedFile.toPath());
   }

   @Test
   void testExportTarifziffer_Single_MergedRawExport() throws IOException {

      // Given
      String fileName = "test_export4";
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.EXPORT_SINGLE_TARIFZIFFER_MERGED_RAW)
            .withTarifziffer(_00_0020)
            .withInputPath(TEST_RES_INVOICES_CASE1)
            .withFileName(fileName)
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile(fileName, ".csv");
      List<String> importedContent = FileImporter.INTANCE.importFile(new File("test_res\\testresults\\expectedMergedRawTestCase.txt"));
      List<String> expectedImportedContent =
            FileImporter.INTANCE.importFile(new File("test_res\\testresults\\expectedMergedRawTestCase.txt"));
      assertThat(importedContent.size(), is(1));
      assertThat(expectedImportedContent.get(0), is(importedContent.get(0)));
      Files.delete(exportedFile.toPath());
   }

   @Test
   void testExportTarifziffer_Single_MergedWekaExport() throws IOException {

      // Given
      String fileName = "expectedMergedWekaTestCase";
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.EXPORT_SINGLE_TARIFZIFFER_MERGED_WEKA)
            .withTarifziffer(_00_0020)
            .withInputPath(TEST_RES_INVOICES_CASE1)
            .withFileName(fileName)
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile(fileName, ".arff");
      List<String> actualImportedContent =
            FileImporter.INTANCE.importFile(new File(FileSystemUtil.getHomeDir() + "\\expectedMergedWekaTestCase.arff"));
      List<String> expectedImportedContent =
            FileImporter.INTANCE.importFile(new File("test_res\\testresults\\expectedMergedWekaTestCase.txt"));
      assertThat(actualImportedContent.size(), is(16318));
      assertContent(expectedImportedContent, actualImportedContent);
      Files.delete(exportedFile.toPath());
   }

   @Test
   void testWithInvalidRange() throws IOException {

      // Given
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.EXPORT_ALL_TARIFZIFFER_RAW)
            .withTarifziffer(_00_0020)
            .withInputPath(TEST_RES_INVOICES_CASE1)
            .withOutputPath(FileSystemUtil.getHomeDir())
            .withFileName("test_no_export")
            .withExportRange(new ExportRange(5, 10))
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile("test_no_export");
      List<String> importedContent = FileImporter.INTANCE.importFile(exportedFile);
      assertThat(importedContent.size(), is(0));// zero because the range is out of range and therefore there is no export
      Files.delete(exportedFile.toPath());
   }

   @Test
   void testCountOccurrencesTarifziffer_00_0020() throws IOException {

      // Given
      int expectedOccurrences = 7;
      String fileName = "Auswertung XMLs mit Tarifziffer";
      XMLInvoiceContentParser xmlParser = new ExportTestCaseBuilder()
            .withExportMode(ExportMode.COUNT_SINGLE_TARIFZIFFER)
            .withTarifziffer(_00_0020)
            .withInputPath(TEST_RES_INVOICES_CASE1)
            .withFileName(fileName)
            .build();

      // When
      xmlParser.selectAndExportContent();

      // Then
      File exportedFile = getFile(fileName);
      List<String> importedContent = FileImporter.INTANCE.importFile(exportedFile);
      assertThat(importedContent.size(), is(4));
      String line3 = importedContent.get(3);
      String[] split = line3.split(";");
      assertThat(split.length, is(2));
      assertThat(Integer.valueOf(split[1]), is(expectedOccurrences));
      Files.delete(exportedFile.toPath());
   }

   private static File getFile(String fileName) {
      return getFile(fileName, ".csv");
   }

   private static File getFile(String fileName, String suffix) {
      String exportPath = FileSystemUtil.getHomeDir() + FileSystemUtil.getDefaultFileSystemSeparator() + fileName + suffix;
      return new File(exportPath);
   }

   private static void assertContent(List<String> expectedContent, List<String> importedContent) {
      for (int i = 0; i < importedContent.size(); i++) {
         String importedLine = importedContent.get(i);
         if (expectedContent.get(i).startsWith("@attribute code")) {
            continue;// this line we can't test, because its so large, I can't  even copy & paste it
         }
         assertThat(importedLine, is(expectedContent.get(i)));
      }
   }

   private static class ExportTestCaseBuilder {
      private ExportMode exportMode;
      private String tarifziffer;
      private String inputPath;
      private String fileName;
      private ExportRange exportRange;
      private String outputPath;

      private ExportTestCaseBuilder() {
         this.exportRange = new ExportRange();
      }

      private ExportTestCaseBuilder withFileName(String fileName) {
         this.fileName = fileName;
         return this;
      }

      private ExportTestCaseBuilder withExportRange(ExportRange exportRange) {
         this.exportRange = exportRange;
         return this;
      }

      private ExportTestCaseBuilder withInputPath(String path) {
         this.inputPath = path;
         return this;
      }

      private ExportTestCaseBuilder withOutputPath(String path) {
         this.outputPath = path;
         return this;
      }

      private ExportTestCaseBuilder withTarifziffer(String tarifziffer) {
         this.tarifziffer = tarifziffer;
         return this;
      }

      private ExportTestCaseBuilder withExportMode(ExportMode exportMode) {
         this.exportMode = exportMode;
         return this;
      }

      private XMLInvoiceContentParser build() {
         ExportInfoContainer exportInfoContainer = new ExportInfoContainer(exportRange, exportMode, inputPath, fileName, outputPath);
         return new XMLInvoiceContentParser(exportInfoContainer, tarifziffer);
      }
   }

   private List<String> getAllExpectedRawContent() {
      List<String> expectedContent = new ArrayList<>();
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1021.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1021.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;249.87;0.0;0.0;both;none;41030;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M200;1;940;ambulatory;1.0;0.0;0.0;249.87;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1600.0;0.0;0.0;both;none;39.1502.10.05;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M850;50;3;ambulatory;1.0;0.0;0.0;1600.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;30.73;0.0;0.0;both;none;7680537470274;0.0;1.0;0.0;0.0;self_employed;false;0.2;0.0;0.0;M990;5;402;ambulatory;1.0;0.0;0.0;153.65;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;58.8;0.0;0.0;both;none;3040;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;410;ambulatory;1.05;0.0;0.0;56.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1356.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1356.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.8;0.0;0.0;both;none;1738.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.8;0.0;0.0;both;none;1738.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1668.75;0.0;0.0;both;none;7680563260047;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;1668.75;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1668.75;0.0;0.0;both;none;7680563260047;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;1668.75;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;26.0;0.0;0.0;both;none;1266.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;26.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;26.0;0.0;0.0;both;none;1266.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;317;ambulatory;1.0;0.0;0.0;26.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;26.0;0.0;0.0;both;none;1266.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;317;ambulatory;1.0;0.0;0.0;26.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;132.3;0.0;0.0;both;none;7680654990013;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;132.3;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;132.3;0.0;0.0;both;none;7680654990013;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;132.3;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;3;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;3;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;4;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;4;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.0;0.0;0.0;both;none;4707.10;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;2.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1509.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1509.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1509.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1509.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1020.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1020.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1020.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1020.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.0;0.0;0.0;both;none;1666.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;3;317;ambulatory;1.0;0.0;0.0;1.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.0;0.0;0.0;both;none;1666.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;4;317;ambulatory;1.0;0.0;0.0;1.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1093.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1093.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;13.41;0.0;0.0;both;none;7680529800386;0.0;1.0;0.0;0.0;self_employed;true;0.15;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;89.4;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;13.41;0.0;0.0;both;none;7680529800386;0.0;1.0;0.0;0.0;self_employed;true;0.15;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;89.4;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;13.41;0.0;0.0;both;none;7680529800386;0.0;1.0;0.0;0.0;self_employed;true;0.15;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;89.4;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.98;0.0;0.0;both;none;7680151590587;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M200;1;402;ambulatory;1.0;0.0;0.0;34.9;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1341.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1341.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.8;0.0;0.0;both;none;1479.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.8;0.0;0.0;both;none;1479.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.8;0.0;0.0;both;none;1479.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.8;0.0;0.0;both;none;1479.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1574.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1574.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;47.54;0.0;0.0;both;none;7680563840034;0.0;1.0;0.0;0.0;self_employed;true;0.195;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;243.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;46.32;0.0;0.0;both;none;7680563840034;0.0;1.0;0.0;0.0;self_employed;true;0.19;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;243.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;47.78;0.0;0.0;both;none;7680563840034;0.0;1.0;0.0;0.0;self_employed;true;0.196;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;243.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;47.54;0.0;0.0;both;none;7680563840034;0.0;1.0;0.0;0.0;self_employed;true;0.195;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;243.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1406.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1406.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.6;0.0;0.0;both;none;4701.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M100;4;317;ambulatory;1.0;0.0;0.0;6.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.6;0.0;0.0;both;none;4701.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M100;2;317;ambulatory;1.0;0.0;0.0;6.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.6;0.0;0.0;both;none;4701.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;6.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.6;0.0;0.0;both;none;4701.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;6.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.6;0.0;0.0;both;none;4701.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M100;3;317;ambulatory;1.0;0.0;0.0;6.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.6;0.0;0.0;both;none;4701.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M100;5;317;ambulatory;1.0;0.0;0.0;6.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.95;0.0;0.0;both;none;7680355010294;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;9.75;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.95;0.0;0.0;both;none;7680355010294;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;9.75;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;22.18;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.125;0.0;0.0;M200;1;940;ambulatory;1.0;0.0;0.0;177.44;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.4;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M200;1;940;ambulatory;1.0;0.0;0.0;4.4;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;20.64;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.167;0.0;0.0;M200;1;940;ambulatory;1.0;0.0;0.0;123.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.86;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M200;1;940;ambulatory;1.0;0.0;0.0;24.3;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;5.76;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.028;0.0;0.0;M200;1;940;ambulatory;1.0;0.0;0.0;205.56;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;8.67;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.056;0.0;0.0;M200;1;940;ambulatory;1.0;0.0;0.0;154.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;3.37;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.028;0.0;0.0;M200;1;940;ambulatory;1.0;0.0;0.0;120.24;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;8.41;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.056;0.0;0.0;M200;1;940;ambulatory;1.0;0.0;0.0;150.12;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;7.4;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.083;0.0;0.0;M100;4;940;ambulatory;1.0;0.0;0.0;89.16;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.25;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;4;940;ambulatory;1.0;0.0;0.0;31.25;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;7.4;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.083;0.0;0.0;M100;2;940;ambulatory;1.0;0.0;0.0;89.16;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.85;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;2;940;ambulatory;1.0;0.0;0.0;34.25;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.25;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;31.25;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;9.15;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;45.75;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.25;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;31.25;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;9.15;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;45.75;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.33;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.002;0.0;0.0;M100;5;940;ambulatory;1.0;0.0;0.0;2164.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;7.4;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.083;0.0;0.0;M100;3;940;ambulatory;1.0;0.0;0.0;89.16;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;7.4;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.083;0.0;0.0;M100;5;940;ambulatory;1.0;0.0;0.0;89.16;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.25;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;5;940;ambulatory;1.0;0.0;0.0;31.25;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.85;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;2;940;ambulatory;1.0;0.0;0.0;34.25;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;9.15;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;45.75;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.25;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;31.25;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;9.15;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;45.75;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;6.25;0.0;0.0;both;none;41010;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;31.25;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.08;0.0;0.0;both;none;7680295542268;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M200;1;402;ambulatory;1.0;0.0;0.0;41.66;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.08;0.0;0.0;both;none;7680295542268;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;5;402;ambulatory;1.0;0.0;0.0;41.66;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;515.0;0.0;0.0;both;none;39.1504.00.05;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M850;50;3;ambulatory;1.0;0.0;0.0;515.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.04;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.004;0.0;0.0;M100;4;940;ambulatory;1.0;0.0;0.0;260.41;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;8.61;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.08;0.0;0.0;M100;4;940;ambulatory;1.0;0.0;0.0;107.67;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;12.92;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.12;0.0;0.0;M100;2;940;ambulatory;1.0;0.0;0.0;107.67;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.04;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.004;0.0;0.0;M100;2;940;ambulatory;1.0;0.0;0.0;260.41;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;8.61;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.08;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;107.67;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.04;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.004;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;260.41;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.04;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.004;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;260.41;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;8.61;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.08;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;107.67;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.04;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.004;0.0;0.0;M100;3;940;ambulatory;1.0;0.0;0.0;260.41;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;8.61;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.08;0.0;0.0;M100;5;940;ambulatory;1.0;0.0;0.0;107.67;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.04;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.004;0.0;0.0;M100;5;940;ambulatory;1.0;0.0;0.0;260.41;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;21.53;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.2;0.0;0.0;M100;2;940;ambulatory;1.0;0.0;0.0;107.67;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.04;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.004;0.0;0.0;M100;2;940;ambulatory;1.0;0.0;0.0;260.41;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;8.61;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.08;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;107.67;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.04;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.004;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;260.41;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.04;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.004;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;260.41;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;8.61;0.0;0.0;both;none;40600;0.0;1.0;0.0;0.0;self_employed;true;0.08;0.0;0.0;M100;1;940;ambulatory;1.0;0.0;0.0;107.67;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1518.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1518.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1518.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;135.08;0.0;0.0;both;none;7680535570310;0.0;1.0;0.0;0.0;self_employed;false;0.286;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;472.31;0.0;0.0;false;2.5;0.0;0");
      expectedContent.add(
            "45;male;disease;132.25;0.0;0.0;both;none;7680535570310;0.0;1.0;0.0;0.0;self_employed;false;0.28;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;472.31;0.0;0.0;false;2.5;0.0;0");
      expectedContent.add(
            "45;male;disease;130.83;0.0;0.0;both;none;7680535570310;0.0;1.0;0.0;0.0;self_employed;false;0.277;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;472.31;0.0;0.0;false;2.5;0.0;0");
      expectedContent.add(
            "45;male;disease;131.3;0.0;0.0;both;none;7680535570310;0.0;1.0;0.0;0.0;self_employed;false;0.278;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;472.31;0.0;0.0;false;2.5;0.0;0");
      expectedContent.add(
            "45;male;disease;136.97;0.0;0.0;both;none;7680535570310;0.0;1.0;0.0;0.0;self_employed;false;0.29;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;472.31;0.0;0.0;false;2.5;0.0;0");
      expectedContent.add(
            "45;male;disease;131.3;0.0;0.0;both;none;7680535570310;0.0;1.0;0.0;0.0;self_employed;false;0.278;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;472.31;0.0;0.0;false;2.5;0.0;0");
      expectedContent.add(
            "45;male;disease;500.0;0.0;0.0;both;none;39.1507.00.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M850;50;3;ambulatory;1.0;0.0;0.0;500.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;10.0;0.0;0.0;both;none;1245.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;10.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;10.0;0.0;0.0;both;none;1245.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;10.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;0.84;0.0;0.0;both;none;7680295547072;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M100;4;402;ambulatory;1.0;0.0;0.0;16.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;0.84;0.0;0.0;both;none;7680295547072;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M100;2;402;ambulatory;1.0;0.0;0.0;16.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.67;0.0;0.0;both;none;7680295547072;0.0;1.0;0.0;0.0;self_employed;true;0.1;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;16.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;0.84;0.0;0.0;both;none;7680295547072;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;16.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;0.84;0.0;0.0;both;none;7680295547072;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M100;3;402;ambulatory;1.0;0.0;0.0;16.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.67;0.0;0.0;both;none;7680295547072;0.0;1.0;0.0;0.0;self_employed;true;0.1;0.0;0.0;M100;5;402;ambulatory;1.0;0.0;0.0;16.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;0.84;0.0;0.0;both;none;7680295547072;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M100;2;402;ambulatory;1.0;0.0;0.0;16.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;1.67;0.0;0.0;both;none;7680295547072;0.0;1.0;0.0;0.0;self_employed;true;0.1;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;16.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;0.84;0.0;0.0;both;none;7680295547072;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;16.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;9.0;0.0;0.0;both;none;1718.10;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;9.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.61;0.0;0.0;both;none;7612929521271;0.0;1.0;0.0;0.0;self_employed;true;0.05;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;92.17;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;7.54;0.0;0.0;both;none;7612929506636;0.0;1.0;0.0;0.0;self_employed;true;0.125;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;60.3;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;7.54;0.0;0.0;both;none;7612929506636;0.0;1.0;0.0;0.0;self_employed;true;0.125;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;60.3;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;34.95;0.0;0.0;both;none;7680471620766;0.0;1.0;0.0;0.0;self_employed;false;1.0;0.0;0.0;M990;5;402;ambulatory;1.0;0.0;0.0;34.95;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.5;0.0;0.0;both;none;7680318900631;0.0;1.0;0.0;0.0;self_employed;true;0.02;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;224.9;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.5;0.0;0.0;both;none;7680318900631;0.0;1.0;0.0;0.0;self_employed;true;0.02;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;224.9;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;15.45;8.65;6.8;both;none;00.0020;21.0;0.0;1.0;1.0;employee;true;1.0;1.0;1.0;none;1;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;15.45;8.65;6.8;both;none;00.0020;21.0;0.0;1.0;1.0;employee;true;1.0;1.0;1.0;M200;3;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;true;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;30.9;17.3;13.6;both;none;00.0020;21.0;0.0;1.0;1.0;employee;true;2.0;1.0;1.0;M990;2;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;15.45;8.65;6.8;both;none;00.0020;21.0;0.0;1.0;1.0;employee;true;1.0;1.0;1.0;M100;5;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;true;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;30.9;17.3;13.6;both;none;00.0020;21.0;0.0;1.0;1.0;self_employed;true;2.0;1.0;1.0;M100;3;1;stationary;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1027.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.5;0.0;0.0;both;none;1027.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.5;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;3.2;0.0;0.0;both;none;1207.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;3.2;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;3.2;0.0;0.0;both;none;1207.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;3.2;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;3.2;0.0;0.0;both;none;1207.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;3.2;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;3.2;0.0;0.0;both;none;1207.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;3.2;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.8;0.0;0.0;both;none;1223.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;2.8;0.0;0.0;both;none;1223.00;0.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M000;1;317;ambulatory;1.0;0.0;0.0;2.8;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;5.52;0.0;0.0;both;none;7680434070317;0.0;1.0;0.0;0.0;self_employed;true;0.067;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;82.35;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;5.52;0.0;0.0;both;none;7680434070317;0.0;1.0;0.0;0.0;self_employed;true;0.067;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;82.35;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;27.42;0.0;0.0;both;none;7680434070317;0.0;1.0;0.0;0.0;self_employed;true;0.333;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;82.35;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;10.95;0.0;0.0;both;none;7680434070317;0.0;1.0;0.0;0.0;self_employed;true;0.133;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;82.35;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;27.42;0.0;0.0;both;none;7680434070317;0.0;1.0;0.0;0.0;self_employed;true;0.333;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;82.35;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;14.6;0.0;0.0;both;none;1374.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;14.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;14.6;0.0;0.0;both;none;1374.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;317;ambulatory;1.0;0.0;0.0;14.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;14.6;0.0;0.0;both;none;1374.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;3;317;ambulatory;1.0;0.0;0.0;14.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;14.6;0.0;0.0;both;none;1374.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;14.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;14.6;0.0;0.0;both;none;1374.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;14.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;14.6;0.0;0.0;both;none;1374.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;4;317;ambulatory;1.0;0.0;0.0;14.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;14.6;0.0;0.0;both;none;1374.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;317;ambulatory;1.0;0.0;0.0;14.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;14.6;0.0;0.0;both;none;1374.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;14.6;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.0;0.0;0.0;both;none;4707.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;4.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.0;0.0;0.0;both;none;4707.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;317;ambulatory;1.0;0.0;0.0;4.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.0;0.0;0.0;both;none;4707.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;3;317;ambulatory;1.0;0.0;0.0;4.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.0;0.0;0.0;both;none;4707.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;4.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.0;0.0;0.0;both;none;4707.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;4.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.0;0.0;0.0;both;none;4707.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;4;317;ambulatory;1.0;0.0;0.0;4.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.0;0.0;0.0;both;none;4707.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;1;317;ambulatory;1.0;0.0;0.0;4.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;4.0;0.0;0.0;both;none;4707.00;30.0;1.0;0.0;0.0;self_employed;true;1.0;0.0;0.0;M990;2;317;ambulatory;1.0;0.0;0.0;4.0;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;0.31;0.0;0.0;both;none;7680475040157;0.0;1.0;0.0;0.0;self_employed;true;0.125;0.0;0.0;M000;2;402;ambulatory;1.0;0.0;0.0;2.45;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;12.79;0.0;0.0;both;none;7680494564825;0.0;1.0;0.0;0.0;self_employed;true;0.1;0.0;0.0;M100;1;402;ambulatory;1.0;0.0;0.0;127.9;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;91.79;0.0;0.0;both;none;7680254420835;0.0;1.0;0.0;0.0;self_employed;true;2.312;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;39.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;92.22;0.0;0.0;both;none;7680254420835;0.0;1.0;0.0;0.0;self_employed;true;2.323;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;39.7;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;118.73;0.0;0.0;both;none;7680503580709;0.0;1.0;0.0;0.0;self_employed;true;0.73;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;162.65;0.0;0.0;false;0.0;0.0;0");
      expectedContent.add(
            "45;male;disease;120.36;0.0;0.0;both;none;7680503580709;0.0;1.0;0.0;0.0;self_employed;true;0.74;0.0;0.0;M990;1;402;ambulatory;1.0;0.0;0.0;162.65;0.0;0.0;false;0.0;0.0;0");

      return expectedContent;
   }

   private List<String> getWekaExpectedContent() {
      List<String> expectedContent = new ArrayList<>();
      expectedContent.add("@relation invoice-data");
      expectedContent.add("");
      expectedContent.add("@attribute age numeric");
      expectedContent.add("@attribute gender {male,female}");
      expectedContent.add("@attribute treatment_reason {disease,accident,maternity,prevention,birthdefect,unknown}");
      expectedContent.add("@attribute amount numeric");
      expectedContent.add("@attribute amount_mt numeric");
      expectedContent.add("@attribute amount_tt numeric");
      expectedContent.add("@attribute billing_role {mt,tt,both,none}");
      expectedContent.add("@attribute body_location {none,left,right}");
      expectedContent.add(
            "@attribute code {-11183417,-3080040,0,0.001,0.0015,0.002,0.0025,0.0026,0.003,0.004,0.005,0.0055,0.0056,0.006,0.007,0.0075,0.0076,0.008,0.0095,0.01,0.011,0.012,0.0125,0.0126,0.013,0.0131,0.0132,0.0133,0.0134,0.0135,0.0136,0.0137,0.0138,0.014,0.0141,0.0142,0.0143,0.0144,0.0145,0.0146,0.0147,0.0148,0.015,0.0155,0.016,0.0161,0.0162,0.0163,0.0164,0.0165,0.0166,0.0167,0.0168,0.041,0.0415,0.0416,0.0417,0.042,0.0425,0.043,0.0435,0.0436,0.0437,0.0445,0.045,0.049,0.051,0.0515,0.0516,0.052,0.0525,0.053,0.0535,0.0536,0.061,0.0615,0.0616,0.071,0.0715,0.0716,0.072,0.073,0.075,0.076,0.077,0.078,0.079,0.08,0.084,0.085,0.0855,0.086,0.092,0.093,0.095,0.097,0.098,0.0995,0.1,0.101,0.102,0.111,0.112,0.113,0.114,0.115,0.116,0.117,0.118,0.119,0.12,0.121,0.122,0.123,0.124,0.125,0.126,0.131,0.132,0.1325,0.133,0.1345,0.135,0.136,0.137,0.1375,0.1376,0.139,0.141,0.142,0.143,0.144,0.151,0.152,0.153,0.154,0.155,0.156,0.157,0.158,0.159,0.16,0.161,0.162,0.171,0.172,0.173,0.1735,0.174,0.175,0.176,0.177,0.178,0.179,0.18,0.181,0.182,0.183,0.184,0.185,0.186,0.187,0.1871,0.1872,0.188,0.189,0.1896,0.19,0.193,0.194,0.2,0.202,0.204,0.205,0.208,0.211,0.212,0.2205,0.2206,0.223,0.224,0.2255,0.226,0.2265,0.2285,0.2295,0.2505,0.251,0.252,0.253,0.254,0.255,0.256,0.257,0.258,0.259,1,1.001,1.002,1.004,1.01,1.011,1.012,1.0205,1.021,1.022,1.023,1.024,1.025,1.026,1.0265,1.027,1.0275,1.029,1.0295,1.031,1.032,1.033,10,10.001,10.002,10.0025,10.0026,10.003,10.005,10.006,10.008,10.04,10.051,10.054,10.063,10.064,10.065,10.066,10.067,10.068,10.07,10.071,10.074,10.075,10.077,10.081,10.082,10.085,10.103,10.106,10.107,10.108,10.111,100,100.003,100.004,100.005,100.007,1000,1000.65,1000.67,10000001,10000002,10000003,10000004,10000005,10000007,10000013,10000014,10000020,10000039,10000041,10000045,10000047,10000048,10000049,10000050,10000051,10000052,10000053,10000054,100001,10000108,10000152,10000155,10000213,10000252,10000265,10000283,10000285,10000297,100003,10000302,10000368,10000394,100004,100005,100006,100007,10000731,100009,10001,100012,100014925369,100015,100021,100032,1000435,1000504,1000570,100059,10015525,1002,100401,1006,1007,10079799,1008609,1009556,1009557,101,1010,1011,101103,1012,1012964,1013,101308,1014,1014062,1015529,1017379,1018456,1019,1020,1020.01,1020594,102090370,1020967,1020973,1021,1021252,1022,1022832,1022958,1023,1023.01,1023147,1024.1,1024030,1024037,1026,1027,1027.01,1029,1032,1032054,1032284,1032485,1034,1035,1035199,1036069,1036075,1036678,1037,1039,104022,1041567,1042,1043,1044,1045,1046889,1047,1047.01,104805,1049,105115,105119,1051330,1052335,105242,1053,1055,1057100,1057433,1059,1059001,1059018,1059024,1059030,1059082,1059099,1059107,1059113,1059544,1061,1061297,10613830510402,1063,106302,1064,1065,1065007,1065852,1067,1068,1069,1070,1071,1074710,1074785,1079759,108,1080225,1080260,1080403,1080538,1081,1082,1082649,1082891,1083040,1084,1086,1086535,1087,1087670,1087836,1087842,1088,10884522041270,1088540308861,1089,109,10916,1092955,1092978,1093,1093.01,1094641,1096,1096120,1096597,1098,1098544,1098550,1099.11,11,11.002,11.016,11.021,11.025,11.026,11.047,11.049,11.05,11.071,11.072,11.083,110,1101275,1102642,1106,1108,1109,111,1110,1110050,1110216,1112,1113,1113189,1113284,1114,1114912,1114929,1114941,1115219,112,1120,1121,1121668,1121674,1121680,1122,1123,1124,1126,1126134,1126275,1126312,1126329,1126341,1126855,1127381,1127398,1128,1128707,1129612,1130,1132,1133683,1134,1136,1137,1137652,1138,1138663,1139,1140,1140312,1141.11,1143204,1143210,1143629,1144758,1146,1147,114803,1148331,1148354,115,1150,1150115,1151729,1153125,1154159,1156,1157,1158,116,1160.1,11603,1161.1,1164206,1164413,1164436,1164442,1164459,1165045,1165602,1169,1170,1171,1172,1173,1173010,1174,1175888,1176,1177.1,1181,1182,1182931,1183,1183126,1183132,1184,1184605,1184663,1186,1188.1,1189,1190.1,1191.1,1191143,1191166,1191338,1192,1192208,1192220,1193,1193047,1193053,1193774,1194,1195,1195655,1195661,1195721,1195744,1195767,1195796,1195891,1196488,1196525,1197080,1197364,1199771,1199989,12,12.002,12.003,12.012,12.014,12.016,12.019,12.031,12.043,120,1200,1200557,1201,1202,1205,1206,1207,1207.01,1208,121,1211,1212,121405,1216,1217,1218,1219,1220,1221,1222,1223,1224.1,1225,1226,1227,1229,123,1230,1230.01,1231,1233,1233522,1233539,1233545,1233551,1234,1236710,1236733,1236756,1237402,1239,1239430,1240.1,124082,1240947,1240953,1241,1241496,1242283,1244,1244460,1245,1245.01,1245726,1245956,1246,1247,1247719,1247843,1248825,1249,1249.01,1250,1250472,1251,1252,1252459,1253252,1254,1255,1257,1258605,1258717,1259,1259119,1259125,1259148,1260,1260.01,1260476,1260536,1261,1262,1262570,1262587,1263279,1263322,1263345,1265.1,1266,1267,1267337,1268,126801,126824,1270,1273,1274320,127628,127634,127657,127663,1276715,1278,1278418,1278430,1279,1281.1,1283,1286,1286412,1286926,1286961,1286978,1287,1288,1289,1290,129099,1291206,1291488,1292,1292163,1292677,1293,1295635,1296,1297,1297083,1297930,1297982,1298272,13,13.001,13.002,13.023,13.025,130,1301867,1301873,1301896,1301904,1303493,1303599,1303607,130382903065722,1304512,1304972,1305316,1305569,1307,1307077,1307083,1307172,1307396,1308510,1309,1310240,1310487,1310493,1311,1311.1,1312411,1312440,1312747,1313741,1314,1314249,1315007,1316,1316596,1316722,1316745,1317006,1318,1318135,1318371,1318419,1318425,1320,1321539,1321545,1322409,1322421,1326,1328300,1328553,1328576,1329,1329506,1329937,1330,1330076,1330113,1331,1331696,1332632,1333,1334,1334602,1335659,1335783,1336311,1336630,1336653,1336848,1336854,1336914,1336966,1336972,1337026,1338037,1338043,1338095,1338126,1338161,1338675,1338681,1339367,1340,1340235,1340324,1340330,1340353,1340459,1341,1341.01,1342122,1342168,1342582,1342599,1343009,1343015,1343044,1343920,1344581,1344598,1344724,1344730,1345,1345221,1345706,1345735,1345853,1346019,1346172,1346410,1346427,1348,1348432,1348449,1349,1350222,1350239,1350245,1350251,1350268,1350274,1350535,1351210,1351478,1353522,1356,1356.01,1357,1357313,1357371,1357678,1357951,1358,1358933,1358956,1359,1359476,1360433,1360717,1361,1361496,1362219,1362277,1362308,1362337,1363,1363.01,1363957,1363963,1363986,1364738,1364750,1364773,1364796,1368,1368216,1368222,1369061,1369078,1369210,1370,1370466,1371,1371129,1371135,1371359,1371655,1371678,1371980,1371997,1372,1372.01,1372005,1372011,1372034,1372778,1373,1373648,1373921,1373938,1374,1374056,1375,1375860,1375877,1376925,137696,137704,137727,1377304,1377592,1377600,1379415,1379444,1379591,1379645,1380192,1380217,1383345,1383807,1383836,1383842,1384600,1384617,1385864,1388839,1389170,1389187,1389230,139,1390061,1390084,1390552,1390575,1390807,1390813,1391333,1391988,1392723,1393272,1395213,1396,1396.01,1396431,139666,1396661,1396721,139732,139755,139784,1398944,139962,1399808,1399961,1399978,14.014,14.015,14.02,140,140003,1400291,1401,1401310,1402,1402261,1402841,1403266,1405,1405437,1406,1406.01,1406141,14062,1406224,1407123,1407146,1407152,1407169,1407643,1408803,1409,1410,1410.01,1410.1,1410208,1410272,1410438,1411,1412,1412466,1413419,1414264,1414784,1415,1416,1416398,1416518,1416530,1417825,1418,1419,1419333,1420,1421123,1421146,1421353,1422,1422163,1422192,1422370,1422387,1423151,1423168,1423493,1423501,142357,1423694,1423820,1424624,1425,1426,1427,1427060,1427491,1427516,1427717,1431,1433250,1433706,1433729,1433936,1434404,1434410,1434597,1435088,1435450,1435728,1435763,1435970,1436886,1438.1,1438201,1438483,1438514,1439,1439301,1439904,1440.1,1440764,1440818,1440882,1441,1441255,1441574,1442,1442680,1443,1444,1444.1,1444905,1445,1445.1,1445170,1445187,1445419,1446,1446.1,1446577,1447080,1447542,1449,1449676,1450.1,1451,1451213,1451874,1452170,1452514,1456.1,14560146925474,1456481,1456914,1457,1458161,1458221,1458238,1458250,1458735,1459,1459321,1459640,1459717,1459858,1460,1460815,1461,1463162,1463179,1466048,1466054,1466108,1466723,1467786,1468,1469,1469029,1469041,1469058,1469420,1470,1470601,1471,1471084,1471121,1471138,1472149,1472190,1473,1474.1,1474792,1475.1,1475521,1475567,1476058,1476503,1476526,1476696,1476704,1476727,1476816,1477796,1478,1479,1479.01,1479080,1479157,1479163,1479625,1479631,1479648,1479714,1479720,1479737,1479743,1479915,1480,1480232,1480261,1480574,1480640,1480657,1480686,1480829,1481898,1482490,1482509,1482604,1483,1483070,1484106,1484431,1484796,1484810,1484945,1486,1488,1488127,1488446,1488452,1489,1490,1490118,1491,1491017,1491649,1493,1494,1496,1497,1497586,1497847,1498,1498887,1498893,1498901,1498918,1498924,15,15.003,15.004,15.006,15.011,15.012,15.013,15.014,15.015,15.016,15.017,15.02,15.021,15.022,15.023,15.024,15.026,15.027,15.0285,15.0286,15.029,15.03,15.032,15.033,15.034,15.044,15.045,15.046,15.047,15.05,15.051,15.052,15.055,15.059,15.063,15.071,15.072,15.073,15.074,15.075,1501.1,1502,1503,1503140,15038020,15050474500072,15050474500089,1508,1509,1509.01,1510,1511,1511582,1511978,1512,1512937,1513,1514132,1514149,1514267,1515,1515752,1516912,1516929,1517,1518,1518035,1518041,1520,1520658,1521,15218,1523,1524,1524047,1524113,1525,1525443,1526,1526276,1526282,1526313,1526336,1527,1527761,1527778,1528,1528068,1528335,1528341,1528453,1528476,1528660,1529,153,1531567,1532,1534,1536607,1536926,1537,1537742,1539,1541,1542,1542186,1542192,1546,1547,1547858,1547864,155.99,1551245,1551251,1551280,1551311,1551328,1551498,1551529,1551535,1552233,1552256,1552279,1552291,1552351,1552428,1552463,1554746,1555,1555361,1556,1556277,1556283,1556314,1556372,1556656,1556828,1557549,1558247,1562,1562958,1562964,1563099,1563490,1563857,1563863,1563892,1563900,1564928,1564934,1565336,1565537,1565543,1565566,1566028,1567482,1568,1570538,1571561,1572,1573235,1574,1574370,1574387,1575033,1575062,1575636,1575903,1576,1576.01,1577,1579,1580494,1580502,1580519,1580525,1580790,1581,1581499,1583,1583.01,1583529,1585221,1585238,1585327,1585787,1585793,1585801,1586108,1586261,1587,1587071,1587823,1588,1588426,1589,1589236,1589242,1589348,1590,1590386,1590392,1590446,1590469,1590860,1591,1591167,1591173,1591687,1592,1592.01,1592155,1592362,1592385,1592770,1592818,1593137,1593143,1593172,15...");
      expectedContent.add("@attribute treatment_duration numeric");
      expectedContent.add("@attribute external_factor numeric");
      expectedContent.add("@attribute external_factor_mt numeric");
      expectedContent.add("@attribute external_factor_tt numeric");
      expectedContent.add("@attribute medical_role {self_employed,employee}");
      expectedContent.add("@attribute obligation {true,false}");
      expectedContent.add("@attribute quantity numeric");
      expectedContent.add("@attribute scale_factor_mt numeric");
      expectedContent.add("@attribute scale_factor_tt numeric");
      expectedContent.add("@attribute section_code {M000,M050,M100,M200,M300,M400,M500,M600,M700,M800,M850,M900,M950,M960,M970,M990,none}");
      expectedContent.add("@attribute session numeric");
      expectedContent.add("@attribute tariff_type numeric");
      expectedContent.add("@attribute treatment {ambulatory,stationary}");
      expectedContent.add("@attribute unit_factor numeric");
      expectedContent.add("@attribute unit_factor_mt numeric");
      expectedContent.add("@attribute unit_factor_tt numeric");
      expectedContent.add("@attribute unit numeric");
      expectedContent.add("@attribute unit_mt numeric");
      expectedContent.add("@attribute unit_tt numeric");
      expectedContent.add("@attribute validate {true,false}");
      expectedContent.add("@attribute vat_rate numeric");
      expectedContent.add("@attribute cost_fraction numeric");
      expectedContent.add("@attribute service_attributes numeric");
      expectedContent.add("@attribute classification {Freigegeben,InBearbeitung,Zurueckgewiesen}");
      expectedContent.add("@data");
      expectedContent.add(
            "45,male,disease,15.45,8.65,6.8,both,none,00.0020,21.0,0.0,1.0,1.0,employee,true,1.0,1.0,1.0,none,1,1,ambulatory,0.0,0.83,0.83,0.0,10.42,8.19,false,0.0,0.0,0,Freigegeben");
      expectedContent.add(
            "45,male,disease,15.45,8.65,6.8,both,none,00.0020,21.0,0.0,1.0,1.0,employee,true,1.0,1.0,1.0,M200,3,1,ambulatory,0.0,0.83,0.83,0.0,10.42,8.19,true,0.0,0.0,0,Freigegeben");
      expectedContent.add(
            "45,male,disease,30.9,17.3,13.6,both,none,00.0020,21.0,0.0,1.0,1.0,employee,true,2.0,1.0,1.0,M990,2,1,ambulatory,0.0,0.83,0.83,0.0,10.42,8.19,false,0.0,0.0,0,Freigegeben");
      expectedContent.add(
            "45,male,disease,15.45,8.65,6.8,both,none,00.0020,21.0,0.0,1.0,1.0,employee,true,1.0,1.0,1.0,M100,5,1,ambulatory,0.0,0.83,0.83,0.0,10.42,8.19,true,0.0,0.0,0,Freigegeben");
      expectedContent.add(
            "45,male,disease,30.9,17.3,13.6,both,none,00.0020,21.0,0.0,1.0,1.0,self_employed,true,2.0,1.0,1.0,M100,3,1,stationary,0.0,0.83,0.83,0.0,10.42,8.19,false,0.0,0.0,0,Freigegeben");
      return expectedContent;
   }

   private static List<String> getExpectedContent(boolean withHeader) {
      List<String> expectedContent = new LinkedList<>();
      if (withHeader) {
         expectedContent.add(
               "XML-File;birthdate;age;gender;treatment_reason;treatment_date_begin;treatment_type;amount;amount_mt;amount_tt;billing_role;body_location;code;date_begin;date_end;treatment_duration;external_factor;external_factor_mt;external_factor_tt;medical_role;name;obligation;provider_id;quantity;record_id;ref_code;responsible_id;scale_factor_mt;scale_factor_tt;section_code;session;tariff_type;treatment;unit_factor;unit_factor_mt;unit_factor_tt;unit;unit_mt;unit_tt;validate;vat_rate;cost_fraction;remark;service_attributes");
         expectedContent.add(
               "test_invoice.xml;1974-01-07;45;male;disease;2019-11-08;ambulatory;15.45;8.65;6.8;both;none;00.0020;2019-12-11;2019-12-16;21.0;0.0;1.0;1.0;employee;-;true;7601002000208;1.0;3400.0;00.0010;7601000368706;1.0;1.0;none;1;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;-;0");
         expectedContent.add(
               "test_invoice.xml;1974-01-07;45;male;disease;2019-11-08;ambulatory;15.45;8.65;6.8;both;none;00.0020;2019-12-11;2019-12-17;21.0;0.0;1.0;1.0;employee;+ Konsultation, jede weiteren 5 Min. (Konsultationszuschlag);true;7601002000208;1.0;6000.0;00.0010;7601000368706;1.0;1.0;M200;3;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;true;0.0;0.0;-;0");
         expectedContent.add(
               "test_invoice.xml;1974-01-07;45;male;disease;2019-11-08;ambulatory;30.9;17.3;13.6;both;none;00.0020;2019-12-12;2019-12-12;21.0;0.0;1.0;1.0;employee;+ Konsultation, jede weiteren 5 Min. (Konsultationszuschlag);true;7601002000208;2.0;18300.0;00.0010;7601000505187;1.0;1.0;M990;2;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;-;0");
         expectedContent.add(
               "test_invoice.xml;1974-01-07;45;male;disease;2019-11-08;ambulatory;15.45;8.65;6.8;both;none;00.0020;2020-01-06;2020-01-08;21.0;0.0;1.0;1.0;employee;+ Konsultation, jede weiteren 5 Min. (Konsultationszuschlag);true;7601002000208;1.0;20600.0;00.0010;7601000116178;1.0;1.0;M100;5;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;true;0.0;0.0;-;0");
         expectedContent.add(
               "test_invoice.xml;1974-01-07;45;male;disease;2019-11-08;ambulatory;30.9;17.3;13.6;both;none;00.0020;2020-01-04;2020-01-12;21.0;0.0;1.0;1.0;self_employed;+ Konsultation, jede weiteren 5 Min. (Konsultationszuschlag);true;7601000532589;2.0;23500.0;00.0010;7601000767387;1.0;1.0;M100;3;1;stationary;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;-;0");
      } else {
         expectedContent.add(
               "45;male;disease;15.45;8.65;6.8;both;none;00.0020;21.0;0.0;1.0;1.0;employee;true;1.0;1.0;1.0;none;1;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;0");
         expectedContent.add(
               "45;male;disease;15.45;8.65;6.8;both;none;00.0020;21.0;0.0;1.0;1.0;employee;true;1.0;1.0;1.0;M200;3;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;true;0.0;0.0;0");
         expectedContent.add(
               "45;male;disease;30.9;17.3;13.6;both;none;00.0020;21.0;0.0;1.0;1.0;employee;true;2.0;1.0;1.0;M990;2;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;0");
         expectedContent.add(
               "45;male;disease;15.45;8.65;6.8;both;none;00.0020;21.0;0.0;1.0;1.0;employee;true;1.0;1.0;1.0;M100;5;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;true;0.0;0.0;0");
         expectedContent.add(
               "45;male;disease;30.9;17.3;13.6;both;none;00.0020;21.0;0.0;1.0;1.0;self_employed;true;2.0;1.0;1.0;M100;3;1;stationary;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;0");
      }
      return expectedContent;
   }

   private static List<String> getExpectedMedicalRoleTestContent() {
      return Collections.singletonList(
            "45;male;disease;15.45;8.65;6.8;both;none;00.0020;5.0;0.0;1.0;1.0;self_employed;true;1.0;1.0;1.0;none;1;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;0");
   }

   private static List<String> getExpectedContent_Treatment() {
      List<String> expectedContent = new LinkedList<>();
      expectedContent.add(
            "XML-File;birthdate;age;gender;treatment_reason;treatment_date_begin;treatment_type;amount;amount_mt;amount_tt;billing_role;body_location;code;date_begin;date_end;treatment_duration;external_factor;external_factor_mt;external_factor_tt;medical_role;name;obligation;provider_id;quantity;record_id;ref_code;responsible_id;scale_factor_mt;scale_factor_tt;section_code;session;tariff_type;treatment;unit_factor;unit_factor_mt;unit_factor_tt;unit;unit_mt;unit_tt;validate;vat_rate;cost_fraction;remark;service_attributes");
      expectedContent.add(
            "test_1_ambulatory_invoiceMissingValue.xml;1955-07-16;64;male;disease;2019-11-08;ambulatory;15.45;8.65;6.8;both;none;00.0020;2019-12-11;2019-12-16;5.0;0.0;1.0;1.0;employee;-;true;7601002000208;1.0;3400.0;00.0010;7601000368706;1.0;1.0;none;1;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;-;0");
      // I know there is a mismatch with stationary (defined in invoice:stationary and ambulatory (defined within services data) but I wanna make sure the right one is determined
      expectedContent.add(
            "test_1_ambulatory_invoiceWrongValue.xml;1955-07-16;64;male;disease;2019-11-08;stationary;15.45;8.65;6.8;both;none;00.0020;2019-12-11;2019-12-16;5.0;0.0;1.0;1.0;employee;-;true;7601002000208;1.0;3400.0;00.0010;7601000368706;1.0;1.0;none;1;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;-;0");
      expectedContent.add(
            "test_2_stationary_invoice.xml;1955-07-16;64;male;disease;2019-11-08;stationary;15.45;8.65;6.8;both;none;00.0020;2019-12-11;2019-12-16;5.0;0.0;1.0;1.0;employee;-;true;7601002000208;1.0;3400.0;00.0010;7601000368706;1.0;1.0;none;1;1;ambulatory;0.0;0.83;0.83;0.0;10.42;8.19;false;0.0;0.0;-;0");
      return expectedContent;
   }

   private static void printImportecdContent(List<String> importedContent) {
      for (String string : importedContent) {
         System.err.println(string);
      }
   }
}
