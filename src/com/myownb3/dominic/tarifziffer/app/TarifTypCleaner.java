package com.myownb3.dominic.tarifziffer.app;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.myownb3.dominic.tarifziffer.io.FileSystemUtil;
import com.myownb3.dominic.tarifziffer.io.input.FileImporter;
import com.myownb3.dominic.tarifziffer.io.output.FileExporter;

public class TarifTypCleaner {

   public static void main(String[] args) {
      String path = FileSystemUtil.getHomeDir() + "\\alle_tarmed_tarifziffern.txt";
      File file = new File(path);
      List<String> allTarmedTarifziffern = FileImporter.INTANCE.importFile(file);

      List<String> sortedAndCleandTarmedTarifziffern = allTarmedTarifziffern.stream()
            .distinct()
            .sorted()
            .map(String::trim)
            .map(tarifType -> tarifType + "\n")
            .collect(Collectors.toList());

      FileExporter.INTANCE.export(sortedAndCleandTarmedTarifziffern, "alle_tarmed_tarifziffern.txt", ".txt");
   }
}
