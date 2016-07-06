
package jwplot;

import static jwplot.Util.matches;
import static jwplot.Util.verbose;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.java2d.GraphicContext;
import org.apache.xmlgraphics.java2d.ps.EPSDocumentGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.annotations.XYTitleAnnotation;
import org.jfree.chart.axis.*;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.general.Series;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;

/**
 * See main() for command-line arguments
 * */
public class Lines
{
  static Properties properties;

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

  static boolean legendEnabled = true;
  static String legendPosition = null;

  static List<XYTextAnnotation> notes =
      new ArrayList<XYTextAnnotation>();

  /**
       @param files: properties_file output_file data_file*
       Reads settings from properties: see scanProperties()
       Produces EPS output file
       Data files are two-columns of numbers each -
       see LineReader.read() and LineReader.array()
   */
  public void plotter(Map<String,String> cmdProps, List<String> files)
  throws UserInputException
  {
    String propFile = files.get(0);
    String output = files.get(1);
    List<String> names = new ArrayList<String>();
    for (int i = 2; i < files.size(); i++)
      names.add(files.get(i));

    List<double[][]> data = new ArrayList<double[][]>();
    List<String> labels = new ArrayList<String>();

    properties = new Properties();
    load(propFile);
    properties.putAll(cmdProps);

    scanProperties();

    readDataFiles(names, data, labels);

    XYSeriesCollection collection =
        collection(data, labels, names);
    plot(collection, title, xlabel, ylabel, output);
  }

  static void readDataFiles(List<String> names,
                            List<double[][]> data,
                            List<String> labels)
  throws UserInputException
  {
    for (String name : names)
    {
      File file = new File(name);
      verbose("reading data: " + file);
      double[][] array;
      try
      {
        List<String> lines = LineReader.read(file);
        if (lines == null)
          System.err.println("Problem when reading: "+file);
        array = LineReader.array(lines);
      }
      catch (FileNotFoundException e)
      {
        throw new UserInputException("not found: " + file);
      }
      catch (LineReaderException e)
      {
        throw new UserInputException
        (e.getMessage() + "\n" + "In file: " + file);
      }

      checkRead(name, array);
      data.add(array);
      addLabel(name, labels);
    }
  }

  static void checkRead(String name, double[][] array)
  throws UserInputException
  {
    if (array.length == 0)
      throw new UserInputException
        ("file has no data: " + name);
    if (array[0].length != 2)
      throw new UserInputException
        ("file is not two-column: " + name);
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
  public static boolean plot(XYSeriesCollection collection,
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
      g2d.setGraphicContext(new GraphicContext());

      rectangle = new Rectangle2D.Double(0, 0, width, height);

      g2d.setGraphicContext(new GraphicContext());
      g2d.setupDocument(out, width, height);
    }
    catch (IOException e)
    {
      System.err.println("Problem with file: " + output);
      return false;
    }

    JFreeChart chart =
      ChartFactory.createXYLineChart
      (title, xlabel, ylabel, collection,
       PlotOrientation.VERTICAL, legendEnabled, false, false);

    setupPlot(chart, collection);

    placeLegend(chart);

    chart.draw(g2d, rectangle);

    drawGraphics(g2d);

    try
    {
      g2d.finish();
    }
    catch (Exception e)
    {
      System.err.println("Error!" + e);
    }

    IOUtils.closeQuietly(out);
    verbose("plotted: " + output);

    return true;
  }
  /**
       Stores the data in the given resulting Collection
       Stores the label as the Series key.
       Stores the filename as the Series description.
   */
  static XYSeriesCollection collection(List<double[][]> data,
                                       List<String> labels,
                                       List<String> names)
  {
    final XYSeriesCollection collection = new XYSeriesCollection();

    int count = 0;
    for (double[][] d : data)
    {
      String label = "data: " + count;
      try
      {
        String s = labels.get(count);
        if( !(s.equals("")) )
          label = s;
      }
      catch (IndexOutOfBoundsException e)
      {}

      verbose("label: \""+label+"\"");
      XYSeries series = new XYSeries(label);
      for (int i = 0; i < d.length; i++)
        series.add(d[i][0], d[i][1]);

      series.setDescription(names.get(count));
      collection.addSeries(series);
      count++;
    }
    return collection;
  }

  private static void setupPlot(JFreeChart chart,
                                XYSeriesCollection collection)
  throws UserInputException
  {
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
    if (bw)
      for (int i = 0; i < plot.getSeriesCount(); i++)
        renderer.setSeriesPaint(i, Color.BLACK);
    for (int i = 0; i < plot.getSeriesCount(); i++)
    {
      Series series = collection.getSeries(i);
      String description = series.getDescription();
      if (! showShape(description))
        renderer.setSeriesShapesVisible(i, false);
      if (! showLine(description))
        renderer.setSeriesLinesVisible(i, false);
      formatLine(description, renderer, i);
    }

    Plots.setupLegend(chart, properties);
    setAxes(plot);
    plot.setRenderer(renderer);
    plot.setBackgroundPaint(Color.WHITE);

    for (XYTextAnnotation note : notes)
      plot.addAnnotation(note);
  }

  static void setAxes(XYPlot plot)
  throws UserInputException
  {
    setAxisTypes(plot);

    // Actual values: modify if necessary
    double axmin, axmax, aymin, aymax;
    if (xmin != null || xmax != null)
    {
      NumberAxis axis = (NumberAxis) plot.getDomainAxis();
      Range range = axis.getRange();
      axmin = range.getLowerBound();
      axmax = range.getUpperBound();
      if (xmin != null) axmin = xmin;
      if (xmax != null) axmax = xmax;
      axis.setRange(axmin, axmax);
    }

    if (ymin != null || ymax != null)
    {
      ValueAxis axis = plot.getRangeAxis();
      Range range = axis.getRange();
      aymin = range.getLowerBound();
      aymax = range.getUpperBound();
      if (ymin != null) aymin = ymin;
      if (ymax != null) aymax = ymax;
      axis.setRange(aymin, aymax);
    }
    //  NumberAxis axis = (NumberAxis) plot.getRangeAxis();
    // axis.setTickUnit(new NumberTickUnit(1, new DecimalFormat("0"), 1));
  }


  private static void setAxisTypes(XYPlot plot)
  throws UserInputException
  {
    if (axis_x_type.equals("logarithmic"))
    {
      ValueAxis domainAxis = new LogarithmicAxis(xlabel);
      plot.setDomainAxis(domainAxis);
    }
    else if (axis_x_type.equals("log"))
    {
      ValueAxis domainAxis = new LogAxis(xlabel);
      plot.setDomainAxis(domainAxis);
    }
    else if (axis_x_type.equals("date"))
    {
      DateAxis domainAxis = new DateAxis();
      plot.setDomainAxis(domainAxis);
    }
    else if (!axis_x_type.equals("normal"))
      throw new UserInputException
      ("Invalid axis.x type: " + axis_x_type);

    if (axis_y_type.equals("logarithmic"))
    {
      //
      ValueAxis rangeAxis = new LogarithmicAxis(ylabel);
      plot.setRangeAxis(rangeAxis);
    }
    else if (axis_y_type.equals("log"))
    {
      ValueAxis rangeAxis = new LogAxis(ylabel);
      plot.setRangeAxis(rangeAxis);
    }
    else if (!axis_y_type.equals("normal"))
      throw new UserInputException
      ("Invalid axis.y type: " + axis_y_type);
  }

  static void placeLegend(JFreeChart chart)
  throws UserInputException
  {
    if (legendPosition == null) return;

    XYPlot plot = chart.getXYPlot();
    // http://stackoverflow.com/questions/11320360/embed-the-legend-into-the-plot-area-of-jfreechart
    LegendTitle legend = new LegendTitle(plot);
    // LegendTitle legend = chart.getLegend();

    if (legendPosition.equals("right"))
      legend.setPosition(RectangleEdge.RIGHT);
    else if (legendPosition.equals("left"))
      legend.setPosition(RectangleEdge.LEFT);
    else if (legendPosition.equals("top"))
      legend.setPosition(RectangleEdge.TOP);
    else if (legendPosition.equals("bottom"))
      legend.setPosition(RectangleEdge.BOTTOM);
    else if (legendPosition.equals("SW"))
    {
      legend.setItemFont(new Font("Dialog", Font.PLAIN, 9));
      legend.setBackgroundPaint(Color.WHITE);
      legend.setFrame(new BlockBorder(Color.BLACK));
      legend.setPosition(RectangleEdge.BOTTOM);
      XYTitleAnnotation ta = new XYTitleAnnotation(0.10, 0.00001, legend, RectangleAnchor.BOTTOM_RIGHT);
      ta.setMaxWidth(0.48);
      plot.addAnnotation(ta);
      legendEnabled = false;
    }
    else
      throw new UserInputException
        ("Invalid legend position: " + legendPosition);
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
       axis.x, axis.y (normal/logarithmic, default normal)
   */
  static void scanProperties()
  {
    String tmp;
    title = properties.getProperty("title");
    xlabel = properties.getProperty("xlabel");
    ylabel = properties.getProperty("ylabel");
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
      legendEnabled = Boolean.parseBoolean(tmp);
    tmp = properties.getProperty("legend.position");
    if (tmp != null)
      legendPosition = tmp;
    else
      legendPosition = "bottom";
    tmp = properties.getProperty("axis.x");
    if (tmp != null)
      axis_x_type = tmp;
    tmp = properties.getProperty("axis.y");
    if (tmp != null)
      axis_y_type = tmp;
    tmp = properties.getProperty("notes");
    if (tmp != null)
      loadNotes();
  }

  static void loadNotes()
  {
    String s = properties.getProperty("notes");
    int count = Integer.parseInt(s);
    for (int i = 0; i < count; i++)
    {
      String p = "note."+i;
      String d = properties.getProperty(p);
      String[] tokens = d.split("\\s");
      double x = Double.parseDouble(tokens[0]);
      double y = Double.parseDouble(tokens[1]);
      String text = Util.concat(tokens, 2);
      XYTextAnnotation note = new XYTextAnnotation(text, x, y);
      notes.add(note);
    }
  }

  static void load(String propFile)
  {
    verbose("reading properties from: " + propFile);
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

  static boolean showShape(String name)
  {
    String mode = properties.getProperty("shape."+name);
    // System.out.println(mode);
    if ("none".equals(mode))
      return false;
    mode = properties.getProperty("shape.all");
    if ("none".equals(mode))
      return false;
    return true;
  }

  static boolean showLine(String name)
  {
    String mode = properties.getProperty("line."+name);
    // System.out.println(mode);
    if ("none".equals(mode))
      return false;
    mode = properties.getProperty("line.all");
    if ("none".equals(mode))
      return false;
    return true;
  }

  static BasicStroke dottedStroke =
    new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[] {6.0f, 6.0f}, 0.0f);

  static void formatLine(String name, XYLineAndShapeRenderer renderer, int i)
  {
    String lineStyle = properties.getProperty("line."+name);
    if ("dotted".equals(lineStyle))
      renderer.setSeriesStroke(i, dottedStroke);
  }

  static void drawGraphics(Graphics2D g2d)
  throws UserInputException
  {
    String s = properties.getProperty("graphics");
    if (s == null) return;
    int count = Integer.parseInt(s);
    for (int i = 0; i < count; i++)
    {
      String p = "graphic."+i;
      String d = properties.getProperty(p);
      if (d == null)
        throw new UserInputException("no such property: " + p);
      String[] tokens = d.split("\\s");
      if (tokens.length == 0)
        throw new UserInputException("property too short: " + p);
      String shape = tokens[0];
      if (matches(shape, "rectangle"))
        drawRectangle(g2d, tokens);
      else
        throw new UserInputException("unknown shape: " + shape);
    }
  }

  static void drawRectangle(Graphics2D g2d, String[] tokens)
  throws UserInputException
  {
    Color color = Plots.string2color(tokens[1]);
    double x0 = Integer.parseInt(tokens[2]);
    double y0 = Integer.parseInt(tokens[3]);
    double x1 = Integer.parseInt(tokens[4]);
    double y1 = Integer.parseInt(tokens[5]);
    g2d.setColor(color);
    g2d.fill(new Rectangle2D.Double(x0, y0, x1, y1));
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
