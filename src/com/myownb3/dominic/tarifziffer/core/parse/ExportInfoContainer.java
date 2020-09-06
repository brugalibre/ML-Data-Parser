package com.myownb3.dominic.tarifziffer.core.parse;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import com.myownb3.dominic.tarifziffer.core.export.mode.ExportMode;
import com.myownb3.dominic.tarifziffer.io.FileSystemUtil;

/**
 * An {@link ExportInfoContainer} defines how and where the parsed xml files are exported
 * It also says where those input files are located
 * 
 * @author Dominic
 *
 */
public class ExportInfoContainer {
   private static final String MISSING_OUTPUTDIRECTORY_CHAR = "-"; // because a programm argument can not be empty
   private ExportRange exportRange;
   private ExportMode exportMode;

   private String inputDirectory;
   private String outputFileName;
   private String outputDirectory;
   private boolean omitHeader;


   /**
    * Creates a new {@link ExportInfoContainer} with the given inputs. The header is not omited by default
    * 
    * @param exportRange
    *        the range of the xml files to export
    * @param exportMode
    *        the {@link ExportMode} which defines how the export content will look like
    * @param inputDirectory
    *        the input directory
    * @param outputFileName
    *        the name of the exported result file
    * @param outputDirectory
    *        directory
    */
   public ExportInfoContainer(ExportRange exportRange, ExportMode exportMode, String inputDirectory, String outputFileName, String outputDirectory) {
      this(exportRange, exportMode, inputDirectory, outputFileName, outputDirectory, false);
   }

   /**
    * Creates a new {@link ExportInfoContainer} with the given inputs.
    * 
    * @param exportRange
    *        the range of the xml files to export
    * @param exportMode
    *        the {@link ExportMode} which defines how the export content will look like
    * @param inputDirectory
    *        the input directory
    * @param outputFileName
    *        the name of the exported result file
    * @param outputDirectory
    *        directory
    * @param omitHeader
    *        <code>true</code> if no header is exported or <code>false</code> if not
    */
   public ExportInfoContainer(ExportRange exportRange, ExportMode exportMode, String inputDirectory, String outputFileName, String outputDirectory,
         boolean omitHeader) {
      this.exportRange = requireNonNull(exportRange);
      this.exportMode = requireNonNull(exportMode);
      this.inputDirectory = requireNonNull(inputDirectory);
      this.outputFileName = requireNonNull(outputFileName);
      this.outputDirectory = isNotSet(outputDirectory) ? FileSystemUtil.getHomeDir() : outputDirectory;
      this.omitHeader = omitHeader;
   }

   /**
    * @return true if exporting the header should be omited
    */
   public boolean isOmitHeader() {
      return omitHeader;
   }

   /**
    * @return the range of the export
    */
   public ExportRange getExportRange() {
      return exportRange;
   }

   /**
    * @return the input directory in which the xml to parse are located
    */
   public String getInputDirectory() {
      return inputDirectory;
   }

   /**
    * @return the name of the exported file
    */
   public String getOutputFileName() {
      return outputFileName;
   }

   /**
    * 
    * @return the directory to which the parsed xml files are exported
    */
   public String getOutputDirectory() {
      return outputDirectory;
   }

   /**
    * @return the {@link ExportMode}
    */
   public ExportMode getExportMode() {
      return exportMode;
   }

   private static boolean isNotSet(String outputDirectory) {
      return isNull(outputDirectory) || MISSING_OUTPUTDIRECTORY_CHAR.equals(outputDirectory);
   }
}
