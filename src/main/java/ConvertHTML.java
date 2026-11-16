/*
 *   This file will be mainly used for anything that
 *   is related with any sort of conversion to html
 *   pass the relevant field and you get a String html
 *   Input: fullName (required), date (required)
 *   Output: html
 *
 */
import java.util.List;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

public class ConvertHTML {
  public static String convertToHTML(List<String> Variables, List<String> Results, Path HTMLPath) throws IOException {
    String html = Files.readString(HTMLPath);

    for (int i=0; i<Variables.size(); i++) {
      html = html.replace("{{" + Variables.get(i) + "}}", Results.get(i));
    }

    return html;
  }

  public static String convertToHTML(String fullName, String date) throws IOException {
    Path templatePath = Path.of("src/resource/index.html");
    String html = Files.readString(templatePath);
    html = html.replace("{{fullName}}", fullName);
    html = html.replace("{{date}}", date);

    return html;
  }
}

