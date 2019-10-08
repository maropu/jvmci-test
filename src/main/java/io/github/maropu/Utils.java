package io.github.maropu;

import java.io.IOException;
import java.io.InputStream;

public class Utils {

  public static void debugPrint(String msg) {
    System.err.println(msg);
  }

  public static byte[] byteArrayFromResource(String path) throws IOException {
    InputStream inputStream =
      Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    return inputStream.readAllBytes();
  }
}
