import com.microsoft.playwright.BrowserType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Scanner;

public class Main{
  public static boolean check(String name) {
    return name.matches("^[A-Z][a-z]+(\\s[A-Z][a-z]+)*$");
  }

  public static boolean datecheck(String dateInput) {
    String normalizedDate = dateInput.trim();
    if (!normalizedDate.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}")) {
      return false;
    }

    String[] date = normalizedDate.split("/");
    try {
      int day = Integer.parseInt(date[0]);
      int month = Integer.parseInt(date[1]);
      int year = Integer.parseInt(date[2]);
      if (year == 0) {
        return false;
      }
      LocalDate.of(year, month, day);
      return true;
    } catch (DateTimeException | NumberFormatException e) {
      return false;
    }
  }

  public static void main(String[] args) throws java.io.IOException {

    // Check for arguments
    if (args.length < 2) {
      System.err.println("How to use: mvn exec:java -Dexec.args='[HTML file path] [TOML file path] [JSON file path](Optional)'");
      return;
    }

    Path HTMLPath = Path.of(args[0]);
    Path TOMLPath = Path.of(args[1]);

    if (!Files.isRegularFile(HTMLPath)) {
      System.err.println("HTML file path does not exist!");
      return;
    }

    if (!Files.isRegularFile(TOMLPath)) {
      System.err.println("TOML file path does not exist!");
      return;
    }

    System.out.println("Success!");

    // TODO: loop through this one and create the input varyingly
    EvaluateTOML.evaluate(TOMLPath, HTMLPath);

    Scanner input = new Scanner(System.in);

    String Name;
    while (true) {
      System.out.print("Enter Name: ");
      Name = input.nextLine().trim();
      if (check(Name)) {
        break;
      }
      System.out.println("Invalid Name!! Please try again.");
    }

    String Date;
    while (true) {
      System.out.print("Enter Date: (DD/MM/YYYY): ");
      Date = input.nextLine().trim();
      if (datecheck(Date)) {
        break;
      }
      System.out.println("Invalid Date!! Please try again.");
    }

    input.close();

    // This is a function that will return a valid HTML
    String html = ConvertHTML.convertToHTML(Name, Date);

    Path outputDir = Path.of(System.getProperty("user.home"), "Desktop");
    Files.createDirectories(outputDir); //make sure desktop exist before sending the file

    Path outputPath = outputDir.resolve("certificate.pdf");
    int n = 1;
    while (Files.exists(outputPath)) { // don't overwrite an existing certificate, add a number instead
      n++;
      outputPath = outputDir.resolve("certificate-" + n + ".pdf");
    }

    // Use the system PATH if the execution fail
    // MUST have chrome or chromium installed!
    // the PATH name MUST be CHROMIUM_PATH that points to chromium!
    String chromePath = System.getenv("CHROMIUM_PATH");
    BrowserType.LaunchOptions opts = new BrowserType.LaunchOptions();

    if (chromePath != null && !chromePath.isBlank()) {
      opts.setExecutablePath(Path.of(chromePath));
    };

    // Render the HTML in headless Chromium and print it to PDF
    PDFGenerator.generate(html, outputPath, opts);
  }
}

