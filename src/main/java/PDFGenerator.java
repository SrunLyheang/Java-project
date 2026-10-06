/*
 *  This file is to generate the PDF that will be written to disk
 *  Anything related to PDF options or change should be here
 *  Input: String html, Path outputPath, BrowserType.LaunchOptions opts
 *  Output: None
 */
import java.nio.file.Path;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;

public class PDFGenerator {
  static void generate(String html, Path outputPath, BrowserType.LaunchOptions opts) {
    try (
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(opts);
        ) {
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

