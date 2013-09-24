package com.thefind.sisyphus;

import java.io.*;
import java.util.*;

import com.thefind.util.FileRegexFilter;

/**
 * @author Eric Gaudet
 * @author Seinjuti Chakraborty
 */
public class FileGroup
{
  public final static String PREFIX_REGEX = "regex:";
  public final static String PREFIX_GLOB  = "glob:";

  public final static Comparator FILENAME_COMPARATOR = new Comparator() {
    public int compare(Object o1, Object o2) {
      try {
        File f1 = (File) o1;
        File f2 = (File) o2;

        if (f1.isDirectory() && !f2.isDirectory()) {
          return -1;
        } else if (!f1.isDirectory() && f2.isDirectory()) {
          return 1;
        } else {
          return f1.getName().compareTo(f2.getName());
        }
      } catch (ClassCastException ex) {}

      return 0;
    }
  };

  public final String dirname_;
  public final File[] files_;
  public final String str_;

  public FileGroup(String path)
  { this(null, path); }

  public FileGroup(String dirname, String filter)
  {
    if (dirname==null) {
      int cut = filter.lastIndexOf('/');
      if (cut==-1) {
        dirname = "";
      }
      else {
        ++ cut;
        dirname = filter.substring(0,cut);
        filter = filter.substring(cut);
      }
    }

    int cut = filter.lastIndexOf('/');
    if (cut==-1) {
      cut = 0;
      if (dirname.endsWith("/")) {
        dirname = dirname.substring(0, dirname.length()-1);
      }
    }
    else if (!dirname.isEmpty() && !dirname.endsWith("/")) {
      dirname += '/';
    }
    FileRegexFilter frf;
    if (filter.startsWith(PREFIX_REGEX)) {
      if (cut>0) {
        dirname += filter.substring(PREFIX_REGEX.length(), cut);
        filter = filter.substring(cut+1);
      }
      else {
        filter = filter.substring(PREFIX_REGEX.length());
      }
      frf = new FileRegexFilter(filter);
    }
    else if (filter.startsWith(PREFIX_GLOB)) {
      if (cut>0) {
        dirname += filter.substring(PREFIX_GLOB.length(), cut);
        filter = filter.substring(cut+1);
      }
      else {
        filter = filter.substring(PREFIX_GLOB.length());
      }
      frf = FileRegexFilter.fromGlob(filter);
    }
    else { // assume glob
      if (cut>0) {
        dirname += filter.substring(0, cut);
        filter = filter.substring(cut+1);
      }
      frf = FileRegexFilter.fromGlob(filter);
    }
    dirname_ = dirname;
    File dir = new File(dirname_);
    if (dir.isDirectory()) {
      files_ = dir.listFiles(frf);
      Arrays.sort(files_, FILENAME_COMPARATOR);
    }
    else {
      throw new RuntimeException("Not a directory: "+dir.getAbsolutePath());
    }
    str_ = frf.toString();
  }

  public boolean sameAs(Action act)
  {
    for (File f : files_) {
      if (act.sameAs(f.getName().hashCode())) {
        throw new ConcurrentModificationException("OutputFile prints into InputFile: " + f.getPath()+" -> "+act);
      }
    }
    return false;
  }

  public static int deleteFiles(String dirname, String filter)
  {
    FileGroup fg = new FileGroup(dirname, filter);
    for (File f : fg.files_) {
      if (f.delete()) {
        System.err.println("[FileGroup] Deleted file: "+f.getAbsolutePath());
      }
      else {
        System.err.println("[FileGroup] WARNING - Failed to delete file: "+f.getAbsolutePath());
      }
    }
    return fg.files_.length;
  }

  @Override
  public String toString()
  { return "FileGroup{\""+dirname_+"/\" + "+str_+" ("+files_.length+" files)}"; }
}

