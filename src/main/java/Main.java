import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

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
    System.out.println("Date: " + Date);

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

    // Render the HTML in headless Chromium and print it to PDF
    try (Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch()) {
      Page page = browser.newPage(); //new tab
      page.setContent(html);
      page.waitForLoadState(LoadState.NETWORKIDLE); // wait for Google Fonts
      page.evaluate("document.fonts.ready"); //wait for fonts to be applied
      Page.PdfOptions options = new Page.PdfOptions();
      options.setPath(outputPath);
      options.setPrintBackground(true); //include background colours and images
      options.setPreferCSSPageSize(true); // uses @page size from  HTML
      page.pdf(options);
    }

    System.out.println("Saved to " + outputPath);
  }
}
