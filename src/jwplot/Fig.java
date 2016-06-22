
package jwplot;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;

public class Fig
{
  public static void main(String[] args)
  {
    if (args.length != 3)
    {
      usage();
      System.exit(1);
    }

    String propfile = args[0];
    String script   = args[1];
    String output   = args[2];

    Fig fig = new Fig(propfile, script, output);

    try
    {
      fig.go();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }

  static void usage()
  {
    System.out.println("usage: <properties> <script> <output.eps>");
  }

  String propfile, script, output;
  EPSDocumentGraphics2D g2d;
  Properties properties = new Properties();
  int width = 100, height = 100;
  Color background = Color.WHITE;

  Fig(String propfile, String script, String output)
  {
    this.propfile = propfile;
    this.script = script;
    this.output = output;
  }

  private void loadProperties()
  {
    try
    {
      InputStream stream =
          new BufferedInputStream(new FileInputStream(propfile));
      properties.load(stream);
    }
    catch (IOException e)
    {
      System.out.println("Could not read properties from: " +
                         propfile);
      System.exit(1);
    }
  }

  private void scanProperties()
  throws UserInputException
  {
    String tmp;
    tmp = properties.getProperty("width");
    if (tmp != null)
      width = Integer.parseInt(tmp.trim());
    tmp = properties.getProperty("height");
    if (tmp != null)
      height = Integer.parseInt(tmp.trim());
    tmp = properties.getProperty("background");
    if (tmp != null)
      background = Plots.string2color(tmp);
  }

  void go()
  throws UserInputException
  {
    loadProperties();
    scanProperties();

    g2d = new EPSDocumentGraphics2D(false);
    g2d.setGraphicContext(new GraphicContext());

    List<String> commands = null;
    try
    {
      commands = LineReader.read(new BufferedReader(new FileReader(script)));
    }
    catch (FileNotFoundException e)
    {
      System.err.println("Could not read: " + script);
      System.exit(1);
    }

    OutputStream out = null;
    try
    {
      out = new BufferedOutputStream(new FileOutputStream(output));
      g2d.setupDocument(out, width, height);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.err.println("Could not write to: " + output);
      System.exit(1);
    }

    g2d.setColor(background);
    g2d.fill(new Rectangle2D.Double(0, 0, width, height));

    runCommands(commands);

    try
    {
      g2d.finish();
    }
    catch (Exception e)
    {
      System.err.println("Error!" + e);
    }

    IOUtils.closeQuietly(out);
  }

  void runCommands(List<String> commands)
  {
    int count = 0;
    for (String command : commands)
    {
      count++;
      try
      {
        // System.out.println(command);
        String[] tokens = LineReader.tokenize(command);
        if ("rectangle".equals(tokens[0]))
          rectangle(tokens);
        else
          throw new UserInputException("Unknown command!");
      }
      catch (UserInputException e)
      {
        System.err.println("Error in command: "+ count);
        System.err.println("Command was: " + command);
        System.err.println(e);
        System.exit(1);
      }
    }
  }

  void rectangle(String[] tokens)
  throws UserInputException
  {
    Color  c  = Plots.string2color(tokens[1]);
    double x0 = Integer.parseInt(tokens[2]);
    double y0 = Integer.parseInt(tokens[3]);
    double x1 = Integer.parseInt(tokens[4]);
    double y1 = Integer.parseInt(tokens[5]);
    g2d.setColor(c);
    g2d.fill(new Rectangle2D.Double(x0, y0, x1, y1));
  }
}
