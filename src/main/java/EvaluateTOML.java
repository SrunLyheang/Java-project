import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;
import java.util.stream.IntStream;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class EvaluateTOML {

  private static final LocalDate SAMPLE_DATE = LocalDate.of(2026, 11, 15);

  private static Boolean isValidRegex(String regex) {
    try {
      Pattern.compile(regex);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static Boolean isValidDateFormat(String pattern) {
      try {
          // 1. Build the formatter (catches unknown letters like 'UUUU' or 'j')
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);

          // 2. Generate dummy data from the sample date (e.g., "Nov/15, 2026" or "11/15, 2026")
          String dummyDateStr = SAMPLE_DATE.format(formatter);

          // 3. Parse it back strictly (catches 'yyyy', 'YYYY', 'DD', 'mm', or missing year/month/day)
          LocalDate parsed = LocalDate.parse(dummyDateStr, formatter);

          // 4. Ensure the parsed date actually matches our sample date
          return SAMPLE_DATE.equals(parsed);

      } catch (Exception e) {
          // Catches IllegalArgumentException, DateTimeParseException, and DateTimeException
          return false;
      }
  }

  private static Boolean isValidDateFormat(String pattern, String dateStr) {
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withResolverStyle(ResolverStyle.STRICT);

        LocalDate.parse(dateStr, formatter);

        return true;

    } catch (Exception e) {
        return false;
    }
  }

  public record RecordInputs(String input, String regex, String type, Boolean isOptional) {}

  public static List<RecordInputs> evaluate(Path TOMLPath, Path HTMLPath) {
    Path tomlPath = TOMLPath;
    Path htmlPath = HTMLPath;
    List<RecordInputs> result = new ArrayList<>();

    try {
      // try to parse the toml
      TomlParseResult toml = Toml.parse(tomlPath);

      // read the html contents
      String htmlContents = Files.readString(htmlPath);

      // attempts to read the inputs
      toml.errors().forEach(
          err -> {
            throw new AssertionError(err.toString());
          }
        );

      TomlArray input = toml.getArray("input");
      if (input == null) throw new AssertionError("Atleast 1 input must exist!");

      for (int i = 0; i<input.size(); i++) {
        TomlTable items = input.getTable(i);

        String variable = items.getString("variable");
        String variableRegex = "^\\{\\{(\\w+)\\}\\}$";
        if (variable == null) throw new AssertionError("Variable must exist!");
        Boolean hasPlaceholder = variable.matches(variableRegex);
        if (!hasPlaceholder) throw new AssertionError("Variables should be in {{name}} only! Error at input " + i);
        if (!htmlContents.contains(variable)) throw new AssertionError("Variable doesn't exist in HTML! Error at input " + i);

        // This would take the word inside the {{}}
        Pattern pattern = Pattern.compile(variableRegex);
        Matcher matcher = pattern.matcher(variable);
        if (matcher.matches()) variable = matcher.group(1);

        String regex = items.getString("regex");
        if (!isValidRegex(regex)) throw new AssertionError("Invalid Regex! Error at input " + i);

        String type = items.getString("type", () -> "string");
        String[] typeArray = type.split(" ");

        // support only string and date {format}
        if (typeArray.length == 1 && !type.equals("string")) throw new AssertionError("Invalid type! Error at input " + i);

        if (typeArray.length > 1) {
          if (!typeArray[0].equals("date")) throw new AssertionError("Invalid type! Perhaps you accidently capitalize d? Error at input " + i);

          Boolean validateFormat = isValidDateFormat(typeArray[1]);

          if (!validateFormat) throw new AssertionError("Format type for date is invalid! Error at input " + i);

          // Everything is valid, construct it back
          type = typeArray[0] + " " + typeArray[1];
        }

        Boolean optional = items.getBoolean("optional", () -> false);

        // this one is optional too
        // but the program shall test it for the end user if they added it
        TomlArray test = items.getArray("test");
        if (test != null) {
          TomlTable t = test.getTable(0);
          TomlArray cases = t.getArray("cases");
          TomlArray res = t.getArray("result");

          if (cases == null) throw new AssertionError("variable cases must exist in test! Error at input " + i);
          if (res == null) throw new AssertionError("variable result must exist in test! Error at input " + i);

          List<String> c = cases.toList().stream().map(Object::toString).toList();
          List<Boolean> r = IntStream.range(0, res.size()).mapToObj(res::getBoolean).toList();
          if (cases.size() != r.size()) throw new AssertionError("cases size and result size should be equal! Error at input " + i);

          for (int j = 0; j < c.size(); j++) {
            // evaluate this if the first one is true
            if (type.contains("date")) {
              String[] a = type.split(" ");

              Boolean checkFormat = isValidDateFormat(a[1], c.get(j));

              if (checkFormat != r.get(j)) throw new AssertionError("Test failed! Expect " + r.get(j) + " but got " + checkFormat + " at " + i + " of test " + j);
            } else {
              Boolean isCasesMatchRegex = c.get(j).matches(regex);

              if (isCasesMatchRegex != r.get(j)) throw new AssertionError("Test failed! Expect " + r.get(j) + " but got " + isCasesMatchRegex + " at " + i + " of test " + j);
            }
          }
        }

        // If it reached this point, then the TOML is valid
        result.add(new RecordInputs(variable, regex, type, optional));
      }
    }
    catch (AssertionError | Exception e) {
      System.err.println(e.getMessage());

      // fail once, fail the whole thing
      return List.of(new RecordInputs(null, null, null, null));
    }

    return result;
  }
}

