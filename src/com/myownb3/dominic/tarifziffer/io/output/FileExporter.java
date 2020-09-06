package com.myownb3.dominic.tarifziffer.io.output;
/**
 * 
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.myownb3.dominic.tarifziffer.core.export.InvoiceContentExportContentCollector;
import com.myownb3.dominic.tarifziffer.io.FileSystemUtil;
import com.myownb3.dominic.tarifziffer.io.output.exception.FileExportException;


/**
 * @author Dominic
 *
 */
public class FileExporter {

   /**
    * The file extension of files to export
    */
   public static final FileExporter INTANCE = new FileExporter();
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
    * @param invoiceExportContentCollector
    *        the {@link InvoiceContentExportContentCollector} which provides the content information
    * @param exportDir
    *        the directory to which the content is exported
    */
   public void export(InvoiceContentExportContentCollector invoiceExportContentCollector, String exportDir) {
      List<String> content = invoiceExportContentCollector.collectContent();
      String fileName = invoiceExportContentCollector.getExportFileName();
      String fileExtension = invoiceExportContentCollector.getFileExtension();
      exportInternal(content, evalPath(exportDir, fileName, fileExtension));
   }

   private void exportInternal(List<String> content, String path) {
      LOG.info("Start exporting to '" + path);
      File file = new File(path);

      try (FileWriter writer = new FileWriter(file)) {
         file.createNewFile();
         writeLines(content, writer);
      } catch (IOException e) {
         throw new FileExportException(e);
      }
      LOG.info("Done exporting");
   }

   private static String evalDefaultPath(String fileName, String fileExtension) {
      String path = FileSystemUtil.getHomeDir();
      return evalPath(path, fileName, fileExtension);
   }

   private static String evalPath(String path, String fileName, String fileExtension) {
      return path + "\\" + fileName + "." + fileExtension;
   }

   private void writeLines(List<String> content, FileWriter writer) throws IOException {
      for (String element : content) {
         writer.write(element);
      }
   }
}
