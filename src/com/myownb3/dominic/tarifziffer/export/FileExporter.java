package com.myownb3.dominic.tarifziffer.export;
/**
 * 
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import com.myownb3.dominic.tarifziffer.export.exception.XMLParseExportException;


/**
 * @author Dominic
 *
 */
public class FileExporter {

   /**
    * The file extension of files to export
    */
   public static final String FILE_EXTENSION = "csv";
   public static final FileExporter INTANCE = new FileExporter();

   private FileExporter() {
      // private Constructor
   }

   /**
    * Exports the given list of {@link String} to the Desktop
    * 
    * @param content
    *        the content to export
    * @throws FileExportException
    *         if there was a {@link IOException}
    */
   public void export(List<String> content) {
      String fileName = "Auswertung XMLs mit Tarifziffer";

      String getDefaultPath = evalDefaultPath(fileName);
      File file = new File(getDefaultPath);

      try (FileWriter writer = new FileWriter(file)) {
         file.createNewFile();
         writeLines(content, writer);
      } catch (IOException e) {
         throw new XMLParseExportException(e);
      }
   }

   private static String evalDefaultPath(String fileName) {
      FileSystemView filesys = FileSystemView.getFileSystemView();
      File homeDirectory = filesys.getHomeDirectory();
      String path = homeDirectory.getPath();
      return path + "\\" + fileName + "." + FILE_EXTENSION;
   }

   private void writeLines(List<String> content, FileWriter writer) throws IOException {
      for (String element : content) {
         writer.write(element);
      }
   }
}
