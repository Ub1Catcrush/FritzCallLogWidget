package net.dankito.fritzbox.utils;

/**
 * Created by ganymed on 02/11/16.
 */

public class StringUtils {

  public static boolean isNullOrEmpty(String string) {
    return string == null || string.length() == 0;
  }

  public static boolean isNotNullOrEmpty(String string) {
    return !isNullOrEmpty(string);
  }

}
