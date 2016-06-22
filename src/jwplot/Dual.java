
package jwplot;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Plots two data lines on two axes
 * See main() for command-line arguments
 * */
public class Dual
{
  static Properties properties;

  static String title = null;
  static String xlabel = "x";
  static String ylabel1 = "y1";
  static String ylabel2 = "y2";

  public static boolean bw = false;

  static int width = 400;
  static int height = 400;

  // null indicates the value was not set by the user
  static Double xmin = null;
  static Double xmax = null;
  static Double ymin1 = null;
  static Double ymax1 = null;
  static Double ymin2 = null;
  static Double ymax2 = null;

  static String axis_x_type = "normal";
  static String axis_y1_type = "normal";
  static String axis_y2_type = "normal";

  static boolean withLegend = true;

  /**
       Args: Lines <properties> <output file> <data file>*
       Reads settings from properties: see scanProperties()
       Produces EPS output file
       Data files are two-columns of numbers each -
       see LineReader.read() and LineReader.array()
   * @throws UserInputException
   */
  public void plotter(List<String> files)
  throws UserInputException
  {

    String propFile = files.get(0);
    String output = files.get(1);
    List<String> names = new ArrayList<String>();
    for (int i = 2; i < files.size(); i++)
      names.add(files.get(i));

    String title = null;
    String xlabel = null;
    String ylabel1 = null;
    String ylabel2 = null;
    List<double[][]> data = new ArrayList<double[][]>();
    List<String> labels = new ArrayList<String>();

    properties = new Properties();
    load(propFile);
    title = properties.getProperty("title");
    xlabel = properties.getProperty("xlabel");
    ylabel1 = properties.getProperty("ylabel1");
    ylabel2 = properties.getProperty("ylabel2");

    scanProperties();

    for (String name : names)
    {
      File file = new File(name);
      Util.verbose("open: " + file);
      List<String> lines = null;
      try
      {
        lines = LineReader.read(file);
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
      Util.verbose("array:\n" + toString(array));
    }

    XYSeriesCollection[] collections =
        collections(data, labels, names);

    plot(collections, title, xlabel, ylabel1, ylabel2, output);
  }

  /**
       Generate simple plot
       @param collection The x,y data
       @param title Plot title
       @param xlabel X label text
       @param ylabel1 Y label text
       @param ylabel2 Y label text
       @param output EPS filename
       @return true/false depending if the method completed without error or not
   */
  public static boolean plot(XYSeriesCollection[] collections,
                             String title, String xlabel,
                             String ylabel1, String ylabel2,
                             String output)
  {
    EPSDocumentGraphics2D g2d = null;
    Rectangle2D.Double rectangle = null;
    OutputStream out = null;

    try
    {
      out = new FileOutputStream(output);
      out = new BufferedOutputStream(out);

      g2d = new EPSDocumentGraphics2D(false);
      g2d.setGraphicContext
      (new org.apache.xmlgraphics.java2d.GraphicContext());

      rectangle = new Rectangle2D.Double(0, 0, width, height);

      g2d.setGraphicContext
      (new org.apache.xmlgraphics.java2d.GraphicContext());
      g2d.setupDocument(out, width, height);
    }
    catch (IOException e)
    {
      System.err.println("Problem with file: " + output);
      return false;
    }

    JFreeChart chart =
        ChartFactory.createXYLineChart
        (title, xlabel, ylabel1, collections[0],
         PlotOrientation.VERTICAL, withLegend, false, false);

    setupPlot(chart, collections, ylabel1, ylabel2);
    chart.draw(g2d, rectangle);

    try
    {
      g2d.finish();
    }
    catch (Exception e)
    {
      System.err.println("Err!" + e);
    }

    IOUtils.closeQuietly(out);
    System.out.println("PLOTTED: " + output);

    return true;
  }

  private static void setupPlot(JFreeChart chart,
                                XYSeriesCollection[] collections,
                                String ylabel1, String ylabel2)
  {
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
    XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();

    int count = plot.getSeriesCount();
    if (bw)
    {
      for (int i = 0; i < count; i++)
      {
        renderer1.setSeriesPaint(i, Color.BLACK);
        renderer2.setSeriesPaint(i, Color.BLACK);
      }
    }

    Series series1 = collections[0].getSeries(0);
    if (! showShape(series1.getDescription()))
      renderer1.setSeriesShapesVisible(0, false);
    Series series2 = collections[1].getSeries(0);
    if (! showShape(series2.getDescription()))
      renderer2.setSeriesShapesVisible(1, false);

    Plots.setupLegend(chart, properties);
    setAxes(plot, collections[1]);
    plot.setRenderer(0, renderer1);
    plot.setRenderer(1, renderer2);
    plot.setBackgroundPaint(Color.WHITE);
  }

  static void setAxes(XYPlot plot,
                      XYSeriesCollection collection)
  {
    setAxisTypes(plot);
    plot.setDataset(1, collection);

    // Actual values: modify if necessary
    double axmin, axmax, aymin1, aymax1, aymin2, aymax2;

    if (xmin != null || xmax != null)
    {
      NumberAxis axis = new NumberAxis();
      plot.setDomainAxis(axis);
      Range range = axis.getRange();
      axmin = range.getLowerBound();
      axmax = range.getUpperBound();
      if (xmin != null) axmin = xmin;
      if (xmax != null) axmax = xmax;
      axis.setRange(axmin, axmax);
    }

    // Left Y axis
    if (ymin1 != null || ymax1 != null)
    {
      NumberAxis axis = (NumberAxis) plot.getRangeAxis(0);
      Range range = axis.getRange();
      aymin1 = range.getLowerBound();
      aymax1 = range.getUpperBound();
      if (ymin1 != null) aymin1 = ymin1;
      if (ymax1 != null) aymax1 = ymax1;
      axis.setRange(aymin1, aymax1);
    }

    // Right Y axis setup
    // Font font = plot.getRangeAxis().getLabelFont();
    // rightAxis.setLabelFont(font);

    // Right Y axis
    if (ymin2 != null || ymax2 != null)
    {
      NumberAxis axis = (NumberAxis) plot.getRangeAxis(1);
      Range range = axis.getRange();
      aymin2 = range.getLowerBound();
      aymax2 = range.getUpperBound();
      if (ymin2 != null) aymin2 = ymin2;
      if (ymax2 != null) aymax2 = ymax2;
      axis.setRange(aymin2, aymax2);
    }

    plot.mapDatasetToRangeAxis(0, 0);
    plot.mapDatasetToRangeAxis(1, 1);
    plot.configureRangeAxes();
  }

  static void setAxisTypes(XYPlot plot)
  {
    NumberAxis axis_x;
    if (axis_x_type.equals("normal"))
      axis_x = new NumberAxis(xlabel);
    else if (axis_x_type.equals("logarithmic"))
      axis_x = new LogarithmicAxis(xlabel);
    else
      throw new RuntimeException
      ("Invalid axis.x type: " + axis_x_type);

    NumberAxis axis_y1;
    if (axis_y1_type.equals("normal"))
      axis_y1 = new NumberAxis(ylabel1);
    else if (axis_y1_type.equals("logarithmic"))
      axis_y1 = new LogarithmicAxis(ylabel1);
    else
      throw new RuntimeException
      ("unknown axis.y1: " + axis_y1_type);

    NumberAxis axis_y2;
    if (axis_y2_type.equals("normal"))
      axis_y2 = new NumberAxis(ylabel2);
    else if (axis_y2_type.equals("logarithmic"))
      axis_y2 = new LogarithmicAxis(ylabel2);
    else
      throw new RuntimeException
      ("unknown axis.y2: " + axis_y2_type);

    plot.setDomainAxis(axis_x);
    plot.setRangeAxis(0, axis_y1);
    plot.setRangeAxis(1, axis_y2);
  }

  static XYSeriesCollection[] collections(List<double[][]> data,
                                          List<String> labels,
                                          List<String> names)
  {
    final XYSeriesCollection[] result = new XYSeriesCollection[2];
    result[0] = new XYSeriesCollection();
    result[1] = new XYSeriesCollection();

    int count = 0;
    for (int i = 0; i < 2; i++)
    {
      double[][] d = data.get(i);
      String label = "data: " + count;
      try
      {
        String s = labels.get(count);
        if( !(s.equals("")) )
          label = s;
      }
      catch (IndexOutOfBoundsException e)
      {}

      Util.verbose( "label: "+label );
      XYSeries series = new XYSeries(label);
      for (int j = 0; j < d.length; j++)
      {
        series.add(d[j][0], d[j][1]);
      }

      series.setDescription(names.get(count));
      result[i].addSeries(series);
      count++;
    }
    return result;
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

  /**
     Various plot properties.  All are currently optional

     Example.
     Assume you want to plot a two-column table in file.data.
     The first column is the x values and the second column
     is the y values.  See LineReader for details.

     Your properties may include:
     title = title string
     xlabel,ylabel1,ylabel2 = title string
     label.file.data = legend text
     width (output image width)
     height (output image height)
     xmin, xmax, ymin1, ymax1, ymin2, ymax2
     (auto-selected if not given)
     bw (Black and white, true/false, default false)
     legend.enabled (true/false, default true)
     axis.x,axis.y1,axis.y2 (normal/logarithmic, default normal)
   */
  static void scanProperties()
  {
    String tmp;
    title = properties.getProperty("title");
    xlabel = properties.getProperty("xlabel");
    ylabel1 = properties.getProperty("ylabel1");
    ylabel2 = properties.getProperty("ylabel2");
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
    tmp = properties.getProperty("ymin1");
    if (tmp != null)
      ymin1 = Double.parseDouble(tmp);
    tmp = properties.getProperty("ymax1");
    if (tmp != null)
      ymax1 = Double.parseDouble(tmp);
    tmp = properties.getProperty("ymin2");
    if (tmp != null)
      ymin2 = Double.parseDouble(tmp);
    tmp = properties.getProperty("ymax2");
    if (tmp != null)
      ymax2 = Double.parseDouble(tmp);
    tmp = properties.getProperty("axis.x");
    if (tmp != null)
      axis_x_type = tmp;
    tmp = properties.getProperty("axis.y1");
    if (tmp != null)
      axis_y1_type = tmp;
    tmp = properties.getProperty("axis.y2");
    if (tmp != null)
      axis_y2_type = tmp;
    tmp = properties.getProperty("bw");
    if (tmp != null)
      bw = Boolean.parseBoolean(tmp);
    tmp = properties.getProperty("legend.enabled");
    if (tmp != null)
      withLegend = Boolean.parseBoolean(tmp);
  }

  /**
       Arrays.copyOfRange is a Java 1.6 feature.
       This has the same signature.
   */
  /*
      static String[] select(String[] s, int p, int q)
      {
      String[] result = new String[q-p];
      int j = 0;
      for (int i = p; i < q; i++)
      result[j++] = s[i];
      return result;
      }
   */

  static void addLabel(String name,
                       List<String> labels)
  {
    String label = properties.getProperty("label."+name);
    if (label == null)
      label = "";
    labels.add(label);
  }

  static boolean showShape(String name)
  {
    // System.out.println(name);
    String mode = properties.getProperty("shape."+name);
    // System.out.println(mode);
    if ("none".equals(mode))
      return false;
    return true;
  }

  static String toString(double[][] array)
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
}
