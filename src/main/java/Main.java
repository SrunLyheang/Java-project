import java.util.Scanner;

public class Main{
  public static void main(String[] args) throws java.io.IOException {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter Your name: ");
    String name = scanner.nextLine();
    System.out.print("Enter a Date: ");
    String date = scanner.nextLine();

    // This is a function that will return a valid HTML
    // Check ConvertHTML.java for more details
    String html = ConvertHTML.convertToHTML(name, date);

    System.out.println(html);
    scanner.close();
  }
}

