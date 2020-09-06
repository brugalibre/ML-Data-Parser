
package com.myownb3.dominic.tarifziffer.io;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

public class FileSystemUtil {

   private FileSystemUtil() {
      // private
   }

   /**
    * @return the path as String to the users home directory
    */
   public static String getHomeDir() {
      FileSystemView filesys = FileSystemView.getFileSystemView();
      File homeDirectory = filesys.getHomeDirectory();
      return homeDirectory.getPath();
   }
}
