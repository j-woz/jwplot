
package jwplot;

import java.util.Properties;
import java.util.List;

/**
 * Plot data helpers.
 * */
public class Util
{
  /** Verbose? */
  static boolean v = false;

  public static String doublesToString(double[][] array)
  {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < array.length; i++)
    {
      double[] row = array[i];
      for (int j = 0; j < row.length; j++)
      {
        sb.append(array[i][j]);
        if (j < row.length-1)
          sb.append(" ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }

  public static String concat(List<String> tokens)
  {
    String[] array = new String[tokens.size()];
    tokens.toArray(array);
    return concat(array, 0, " ");
  }

  public static String concat(String... strings)
  {
    return concat(' ', strings);
  }

  public static String concat(String[] tokens, int start)
  {
    return concat(tokens, start, " ");
  }

  public static String concat(String[] tokens, int start,
                              String separator)
  {
    if (tokens == null)
      return "null";
    StringBuilder sb = new StringBuilder();
    for (int i = start; i < tokens.length; i++)
    {
      sb.append(tokens[i]);
      if (i < tokens.length - 1)
        sb.append(separator);
    }
    return sb.toString();
  }

  public static String concat(char c, String... strings)
  {
    if (strings == null)
      return "null";
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < strings.length; i++)
    {
      sb.append(strings[i]);
      if (i < strings.length - 1)
        sb.append(c);
    }
    return sb.toString();
  }

  public static boolean matches(String s1, String s2)
  {
    assert(s1 != null);
    assert(s2 != null);
    if (s1.compareToIgnoreCase(s2) == 0)
      return true;
    return false;
  }

  public static void fatal(String s)
  {
    System.err.println(s);
    System.exit(2);
  }

  public static void verbose(String s)
  {
    if (v)
      System.out.println("jwplot: " + s);
  }

  public static void verbose(boolean status)
  {
    v = status;
  }
  
  public static int parsePropertyInt(String propName, String text)
  throws UserInputException
  {
    try 
    {
      return Integer.parseInt(text);
    }
    catch (NumberFormatException e)
    {
      throw new UserInputException
      ("Could not parse property as integer: " +
       "\""+ propName +"\"=\"" +text+"\"");
    }
  }
  
  public static double parsePropertyDbl(String propName, String text)
  throws UserInputException
  {
    try 
    {
      return Double.parseDouble(text);
    }
    catch (NumberFormatException e)
    {
      throw new UserInputException
      ("Could not parse property as double: " +
       "\""+ propName +"\"=\"" +text+"\"");
    }
  }
  

  
  public static double assignProperty(Properties properties, 
                                      String propName, 
                                      double defaultValue)
  throws UserInputException
  {
    String text = properties.getProperty(propName).trim();
    if (text == null) return defaultValue;
    return parsePropertyDbl(propName, text);
  }
}
