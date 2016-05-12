package com.thefind.sisyphus;

import java.util.*;
import java.io.*;

/**
 * @author Eric Gaudet
 */
public class Shell
{
  public static void main(String[] args)
  {
    try {
      jline.ConsoleReader reader = new jline.ConsoleReader();
      reader.setBellEnabled(false);
      reader.getHistory().setHistoryFile(new File(".sisyphus.history"));
      String line = null;
      System.err.println("=== Enter Command ===");
      while ((line = reader.readLine("cmd> ")) != null) {
        if (!line.trim().isEmpty()) {
          for (String k : line.split("\\s+")) {
            System.out.print(k);
            System.out.print('\t');
            if (bind.contains(k)) {
              System.out.print(bind.get(k));
            }
            else {
              System.out.print("false");
            }
            System.out.println();
          }
        }
      }
    }
    catch(IOException ioex) {
      ioex.printStackTrace();
    }
  }
}
