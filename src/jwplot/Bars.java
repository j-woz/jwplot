
package jwplot;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * See bars() for command-line arguments
 * */
public class Bars
{
  static CheckedProperties properties;

  static String title = null;
  static String xlabel = "x";
  static String ylabel = "y";

  /** If true, use only black and white */
  public static boolean bw = false;

  static String axis_x_type = "normal";
  static String axis_y_type = "normal";

  static int width = 400;
  static int height = 400;

  // null indicates the value was not set by the user
  static Double xmin = null;
  static Double xmax = null;
  static Double ymin = null;
  static Double ymax = null;

  static boolean withLegend = true;
  static boolean verbose = false;

  /**
       Args: Lines <properties> <output file> <data file>*
       Reads settings from properties: see scanProperties()
       Produces EPS output file
       Data files are two-columns of numbers each -
       see LineReader.read() and LineReader.array()
   * @throws UserInputException
   */
  public static void bars(List<String> files, boolean verbose)
  throws UserInputException
  {
    Bars.verbose = verbose;

    Util.verbose(verbose);

    String propFile = files.get(0);
    String output = files.get(1);

    List<String> names = new ArrayList<String>();
    for (int i = 2; i < files.size(); i++)
      names.add(files.get(i));

    List<double[][]> data = new ArrayList<double[][]>();
    List<String> labels = new ArrayList<String>();

    properties = new CheckedProperties();
    load(propFile);

    scanProperties();

    for (String name : names)
    {
      File file = new File(name);
      Util.verbose("open: " + file);
      List<String> lines = null;
      try
      {
        lines = LineReader.read(file);

        if (lines == null)
        {
          System.err.println("Problem when reading: "+file);
        }
      }
      catch (FileNotFoundException e)
      {
        System.err.println("not found: " + file);
        System.exit(1);
      }
      double[][] array;
      try
      {
        array = LineReader.array(lines);
      }
      catch (LineReaderException e)
      {
        throw new UserInputException(e.getMessage());
      }
      data.add(array);
      addLabel(name, labels);
      Util.verbose("array:\n" + Util.doublesToString(array));
    }

    CategoryDataset dataset = makeDataset(data);

    plot(dataset, title, xlabel, ylabel, output);
  }

  private static CategoryDataset makeDataset(List<double[][]> data)
  {
    DefaultCategoryDataset result = new DefaultCategoryDataset();
    //int j = 0;
    for (double[][] d : data)
      for (int i = 0; i < d.length; i++)
        result.addValue((Number) d[i][1], "", String.valueOf(d[i][0]));
    return result;
  }

  /**
       Generate simple plot.
       @param collection The x,y data.
       @param title Plot title.
       @param xlabel X label text.
       @param ylabel Y label text.
       @param output EPS filename.
       @return true/false depending if the method completed without error or not
   * @throws UserInputException
   */
  public static boolean plot(CategoryDataset dataset,
                             String title, String xlabel,
                             String ylabel, String output)
  throws UserInputException
  {
    EPSDocumentGraphics2D g2d = null;
    Rectangle2D.Double rectangle = null;
    OutputStream out = null;

    try
    {
      out = new BufferedOutputStream(new FileOutputStream(output));

      g2d = new EPSDocumentGraphics2D(false);
      g2d.setGraphicContext
      (new GraphicContext());

      rectangle = new Rectangle2D.Double(0, 0, width, height);

      g2d.setGraphicContext
      (new GraphicContext());
      g2d.setupDocument(out, width, height);
    }
    catch (IOException e)
    {
      System.err.println("Problem with file: " + output);
      return false;
    }

    JFreeChart chart =
      ChartFactory.createBarChart(title, xlabel, ylabel,
                                  dataset, PlotOrientation.VERTICAL,
                                  withLegend, true, false);

    setupPlot(chart, dataset);
    chart.draw(g2d, rectangle);

    try
    {
      g2d.finish();
    }
    catch (Exception e)
    {
      System.err.println("Error!" + e);
    }

    IOUtils.closeQuietly(out);
    System.out.println("PLOTTED: " + output);

    return true;
  }

  private static void setupPlot(JFreeChart chart,
                                CategoryDataset collection)
  throws UserInputException
  {
    CategoryPlot plot = chart.getCategoryPlot();
    Plots.setupLegend(chart, properties);
    plot.setBackgroundPaint(Color.WHITE);

    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setGradientPaintTransformer(null);
    renderer.setSeriesPaint(0, Color.red);

    //  BarRenderer.setDefaultShadowsVisible(false);
//    BarRenderer renderer = (BarRenderer) plot.getRenderer();
//    renderer.setGradientPaintTransformer(null);
//    renderer.setSeriesPaint(0, Color.red);
//    renderer.setSeriesPaint(1, Color.green);
//    plot.setRenderer(renderer);
//    // renderer.setShadowVisible(false);
  }

  /**
       Various plot properties.  All are currently optional

       Example.
       Assume you want to plot a two-column table in file.data.
       The first column is the x values and the second column
       is the y values.  See LineReader for details.

       Your properties may include:
       title = Plot
       xlabel = size
       ylabel = speed
       label.file.data = legend text
       shape.file.data = shape setting
       width (output image width)
       height (output image height)
       xmin, xmax, ymin, ymax (auto-selected if not given)
       bw (Black and white, true/false, default false)
       legend.enabled (true/false, default true)
   */
  static void scanProperties()
  {
    title = properties.getProperty("title");
    xlabel = properties.getProperty("xlabel");
    ylabel = properties.getProperty("ylabel");

    String tmp;
    tmp = properties.getProperty("width");
    if (tmp != null)
      width = Integer.parseInt(tmp.trim());
    tmp = properties.getProperty("height");
    if (tmp != null)
      height = Integer.parseInt(tmp.trim());
    tmp = properties.getProperty("xmin");
    if (tmp != null)
      xmin = Double.parseDouble(tmp);
    tmp = properties.getProperty("xmax");
    if (tmp != null)
      xmax = Double.parseDouble(tmp);
    tmp = properties.getProperty("ymin");
    if (tmp != null)
      ymin = Double.parseDouble(tmp);
    tmp = properties.getProperty("ymax");
    if (tmp != null)
      ymax = Double.parseDouble(tmp);
    tmp = properties.getProperty("bw");
    if (tmp != null)
      bw = Boolean.parseBoolean(tmp);
    tmp = properties.getProperty("legend.enabled");
    if (tmp != null)
      withLegend = Boolean.parseBoolean(tmp);
    tmp = properties.getProperty("axis.x");
    if (tmp != null)
      axis_x_type = tmp;
    tmp = properties.getProperty("axis.y");
    if (tmp != null)
      axis_y_type = tmp;
  }

  static void load(String propFile)
  {
    try
    {
      if (propFile.equals("-"))
        properties.load(System.in);
      else
        properties.load(new FileInputStream(propFile));
    }
    catch (FileNotFoundException e)
    {
      System.err.println(e);
      System.exit(1);
    }
    catch (IOException e)
    {
      e.printStackTrace();
      System.exit(1);
    }
  }

  static void addLabel(String name, List<String> labels)
  {
    String label = properties.getProperty("label."+name);
    if (label == null)
      label = "";
    labels.add(label);
  }
}
