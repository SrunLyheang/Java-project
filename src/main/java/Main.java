import com.microsoft.playwright.BrowserType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    List<EvaluateTOML.RecordInputs> recordInputs = EvaluateTOML.evaluate(TOMLPath, HTMLPath);

    // If it has null
    // We know it has failed the test, so don't run the code
    if (recordInputs.contains(null)) return;

    Scanner getInput = new Scanner(System.in);

    List<String> variables = new ArrayList<>();
    List<String> results = new ArrayList<>();
    for (EvaluateTOML.RecordInputs record : recordInputs) {
      while (true) {
        String v = record.input();

        // These are type checks
        // if the type is date then we'll do more stuff
        String[] st = record.type().split(" ");
        String t = st[0];
        String f = null;
        if (st.length > 1) f = st[1];

        if (t.equals("date")) {
          String fc = f;
          fc = fc.replaceAll("u", "Y");
          fc = fc.replaceAll("d", "D");

          System.out.print("Enter " + v + " (" + fc + "): ");
        } else {
          System.out.print("Enter " + v + ": ");
        }
        String var = getInput.nextLine().trim();

        // If it is not optional
        // Don't permit empty even if they declared that way
        if (!record.isOptional()) {
          if (var.isEmpty()) {
            System.out.println(v + " cannot be empty!");
            continue;
          }
        }

        // Check if it actually matches the regex
        if (var.matches(record.regex())) {
          if (t.equals("date")) {
            Boolean hasCorrectFormat = EvaluateTOML.isValidDateFormat(f, var);

            if (!hasCorrectFormat) {
              System.out.println("Invalid date in " + v + "!");
              continue;
            }
          }
          variables.add(v);
          results.add(var);
          break;
        }

        System.out.println("Invalid " + v + "! Please try again.");
      }
    }

    getInput.close();

    String html = ConvertHTML.convertToHTML(variables, results, HTMLPath);

    /*
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
    */

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

