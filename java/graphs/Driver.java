package graphs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import jre.NFAToDot;
import jre.Regex;

public class Driver {
  public static void main(String[] args) throws IOException {
    FileOutputStream outputStream = new FileOutputStream(new File(args[0]));
    String regex = "a((b|c)*fd?)+e";
    regex = "ba?b";
    String contents = NFAToDot.convert(new Regex(regex).toNFA());
    outputStream.write(contents.getBytes());
  }
}
