
package jwplot;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to read a file into a List of String lines.
 *
 * Omits comments with #
 * Omits blank lines
 * Wraps lines that end on backslash
 * */
public class LineReader
{
  public static List<String> read(File file)
  throws FileNotFoundException
  {
    BufferedReader reader =
        new BufferedReader(new FileReader(file));
    return read(reader);
  }

  public static List<String> read(String s)
  {
    BufferedReader reader =
        new BufferedReader(new StringReader(s));
    return read(reader);
  }

  public static List<String> read(BufferedReader reader)
  {
    List<String> result = new ArrayList<String>();
    try
    {
      String prevline = "";
      String line = "";
      while ((line = reader.readLine()) != null)
      {
        int hash = line.indexOf("#");
        if (hash >= 0)
          line = line.substring(0, hash);
        line = spaces(line);
        line = (prevline + " " + line).trim();
        if (line.endsWith("\\"))
        {
          line = line.substring(0, line.length()-2);
          prevline = line;
          continue;
        }
        else
        {
          prevline = "";
          line = line.trim();
          if (line.length() > 0)
            result.add(line);
        }
      }
      reader.close();
    }
    catch (IOException e)
    {
      System.err.println("LineReader: I/O problem.");
      return null;
    }
    return result;
  }

  public static List<String[]> tokens(List<String> lines)
  {
    List<String[]> result = new ArrayList<String[]>(lines.size());
    for (String line : lines)
    {
      String[] tokens = tokenize(line);
      result.add(tokens);
    }
    return result;
  }

  public static int maxTokens(List<String[]> tokens)
  {
    int result = 0;
    for (String[] t : tokens)
      if (t.length > result)
        result = t.length;
    return result;
  }

  /**
       Take line-oriented data and produce an array of doubles.
   * @throws LineReaderException

   */
  public static double[][] array(List<String> lines)
  throws LineReaderException
  {
    List<String[]> tokens = tokens(lines);
    int columns = maxTokens(tokens);
    int rows = lines.size();
    double[][] result = new double[rows][columns];
    for (int i = 0; i < rows; i++)
    {
      String[] row = tokens.get(i);
      if (row.length < columns)
        throw new LineReaderException("Short row: " + i);
      for (int j = 0; j < columns; j++)
      {
        try
        {
          String v = tokens.get(i)[j];
          v = v.replaceAll(",", "");
          v = v.replaceAll("%", "");
          double d = Double.parseDouble(v);
          result[i][j] = d;
        }
        catch (NumberFormatException e)
        {
          throw new LineReaderException("Bad number in row: " + i);
        }
      }
    }
    return result;
  }

  public static String spaces(String line)
  {
    String result = "";
    for (int i = 0; i < line.length(); i++)
    {
      if (line.charAt(i)   == '=' &&
          line.charAt(i+1) != '=' &&
          line.charAt(i-1) != '=')
      {
        result += " = ";
      }
      else
        result += line.substring(i,i+1);
    }
    return result;
  }

  public static String[] tokenize(String line)
  {
    if (line == null)
      return null;
    List<String> words = new ArrayList<String>();
    String[] ws = line.split("\\s");
    for (int i = 0; i < ws.length; i++)
      if (ws[i].length() > 0)
        words.add(ws[i]);
    String[] result = new String[words.size()];
    for (int i = 0; i < words.size(); i++)
      result[i] = words.get(i);
    return result;
  }

  /*
      public static List<String> list(String line)
      {
      List<String> result = new ArrayList<String>();
      String[] tokens = tokenize(line);
      for (int i = 0; i < tokens.length; i++)
      result.add(tokens[i]);
      return result;
      }
   */
}
