
package jwplot;

public class CheckedProperties
extends java.util.Properties
{
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

  public static String[] trues = new String[]
      { "true", "yes", "y", "1" };
  public static String[] falses = new String[]
      { "false", "no", "n", "0" };

  public static boolean parseBoolean(String propName, String text)
  throws UserInputException
  {
    String t = text.trim();
    for (String v : trues)
    {
      if (v.equals(t))
        return true;
    }
    for (String v : falses)
    {
      if (v.equals(t))
        return false;
    }
    throw new UserInputException
    ("Could not parse property as boolean: " +
        "\""+ propName +"\"=\"" +text+"\"");
  }

  public static boolean parsePropertyBool(String propName, String text)
  throws UserInputException
  {
    try
    {
      return parseBoolean(propName, text);
    }
    catch (NumberFormatException e)
    {
      throw new UserInputException
      ("Could not parse property as double: " +
       "\""+ propName +"\"=\"" +text+"\"");
    }
  }

  public String assign(String propName, String defaultValue)
  {
    String text = getProperty(propName);
    if (text == null) return defaultValue;
    return text.trim();
  }

  public int assign(String propName, int defaultValue)
  throws UserInputException
  {
    String text = getProperty(propName);
    if (text == null) return defaultValue;
    return parsePropertyInt(propName, text);
  }

  public double assign(String propName, double defaultValue)
  throws UserInputException
  {
    String text = getProperty(propName);
    if (text == null) return defaultValue;
    return parsePropertyDbl(propName, text);
  }

  public Double assign(String propName, Double defaultValue)
  throws UserInputException
  {
    String text = getProperty(propName);
    if (text == null) return defaultValue;
    return parsePropertyDbl(propName, text);
  }

  public boolean assign(String propName, boolean defaultValue)
  throws UserInputException
  {
    String text = getProperty(propName);
    if (text == null) return defaultValue;
    return parsePropertyBool(propName, text);
  }

  private static final long serialVersionUID = 1L;
}
