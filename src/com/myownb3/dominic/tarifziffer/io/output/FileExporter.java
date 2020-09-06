package com.myownb3.dominic.tarifziffer.io.output;
/**
 * 
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.myownb3.dominic.tarifziffer.io.FileSystemUtil;
import com.myownb3.dominic.tarifziffer.io.output.exception.FileExportException;
import com.myownb3.dominic.tarifziffer.logging.LoggerHelper;


/**
 * @author Dominic
 *
 */
public class FileExporter {

   /**
    * The file extension of files to export
    */
   public static final FileExporter INSTANCE = new FileExporter();
   private static final Logger LOG = Logger.getLogger(FileExporter.class);

   private FileExporter() {
      // private Constructor
   }

   /**
    * Simply exports the given list of {@link String}s to the users home with the given
    * name for file and file extension
    * 
    * @param content
    *        the strings to export
    * @param fileName
    *        the name of the file
    * @param fileExtension
    *        the file extension
    */
   public void export(List<String> content, String fileName, String fileExtension) {
      String path = evalDefaultPath(fileName, fileExtension);
      exportInternal(content, path);
   }

   /**
    * Exports the given list of {@link String} to the Desktop
    * 
    * @param exportInfo
    *        the {@link ExportInfo} which provides the content information
    */
   public void export(ExportInfo exportInfo) {
      String path = evalPath(exportInfo.getExportDir(), exportInfo.getExportFileName(), exportInfo.getFileExtension());
      exportInternal(exportInfo.getExportContent(), path);
   }

   private void exportInternal(List<String> content, String path) {
      LoggerHelper.INSTANCE.startLogInfo(LOG, "Start exporting '" + content.size() + "' elements to '" + path + "'");
      File file = new File(path);

      try (FileWriter writer = new FileWriter(file)) {
         file.createNewFile();
         writeLines(content, writer);
      } catch (IOException e) {
         LOG.error("Error during exporting file '" + file + "'", e);
         throw new FileExportException(e);
      }
      LoggerHelper.INSTANCE.endLogInfo(LOG, "Done exporting %s\n");
   }

   private static String evalDefaultPath(String fileName, String fileExtension) {
      String path = FileSystemUtil.getHomeDir();
      return evalPath(path, fileName, fileExtension);
   }

   private static String evalPath(String path, String fileName, String fileExtension) {
      return path + FileSystemUtil.getDefaultFileSystemSeparator() + fileName + "." + fileExtension;
   }

   private void writeLines(List<String> content, FileWriter writer) throws IOException {
      for (String element : content) {
         writer.write(element);
      }
   }
}
