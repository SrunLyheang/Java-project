package com.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Main{
  public static void main(String[] args) throw java.io.IOException{
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter Your name: ");
    String name = scanner.nextLine();
    Path htmlFile = Path.of("src/main/resource/index.html");
    String html = Files.readString(htmlFile);
    html = html.replace("{{name}}", name);
    System.out.println(html);
    scanner.close();
  }
}
