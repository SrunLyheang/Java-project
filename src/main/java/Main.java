import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

import java.nio.file.Path;
import java.util.Scanner;

public class Main{
  public static void main(String[] args) throws java.io.IOException {
    Scanner scanner = new Scanner(System.in);
    System.out.print("Enter Your name: ");
    String name = scanner.nextLine();
    System.out.print("Enter a Date: ");
    String date = scanner.nextLine();
    scanner.close();

    // This is a function that will return a valid HTML
    String html = ConvertHTML.convertToHTML(name, date);

    Path outputPath = Path.of(System.getProperty("user.home"), "Desktop", "certificate.pdf");

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

