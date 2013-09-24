package com.thefind.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * For use with java.io.File: File[] listFiles(FileFilter filter)
 *
 * @author Eric Gaudet
 */
public class FileRegexFilter
implements FileFilter
{
  protected Pattern regexPattern_;

  public FileRegexFilter(String regexFilter)
  {
    regexPattern_ = Pattern.compile(regexFilter);
  }

  public boolean accept(File pathname)
  {
    Matcher m = regexPattern_.matcher(pathname.getName());
    return m.matches();
  }

  @Override
  public String toString()
  { return "FileRegexFilter{"+regexPattern_.toString()+"}"; }

  public static FileRegexFilter fromGlob(String glob)
  {
    int strLen = glob.length();
    StringBuilder sb = new StringBuilder();
    glob = glob.trim();

    sb.append('^');
    if (glob.charAt(0)=='*') {
      sb.append("[^.]");
    }

    boolean escaping = false;
    int inCurlies = 0;
    for (int i=0 ; i<strLen ; i++) {
      char currentChar = glob.charAt(i);
      switch (currentChar) {
      case '*':
        if (escaping) {
          sb.append("\\*");
        }
        else {
          sb.append(".*");
        }
        escaping = false;
        break;
      case '?':
        if (escaping) {
          sb.append("\\?");
        }
        else {
          sb.append('.');
        }
        escaping = false;
        break;
      case '.':
      case '(':
      case ')':
      case '+':
      case '|':
      case '^':
      case '$':
      case '@':
      case '%':
        sb.append('\\');
        sb.append(currentChar);
        escaping = false;
        break;
      case '\\':
        if (escaping) {
          sb.append("\\\\");
          escaping = false;
        }
        else {
          escaping = true;
        }
        break;
      case '{':
        if (escaping) {
          sb.append("\\{");
        }
        else {
          sb.append('(');
          inCurlies++;
        }
        escaping = false;
        break;
      case '}':
        if (inCurlies > 0 && !escaping) {
          sb.append(')');
          inCurlies--;
        }
        else if (escaping) {
          sb.append("\\}");
        }
        else {
          sb.append("}");
        }
        escaping = false;
        break;
      case ',':
        if (inCurlies > 0 && !escaping) {
          sb.append('|');
        }
        else if (escaping) {
          sb.append("\\,");
        }
        else {
          sb.append(",");
        }
        break;
      default:
        escaping = false;
        sb.append(currentChar);
      }
    }

    sb.append('$');

    return new FileRegexFilter(sb.toString());
  }
}

