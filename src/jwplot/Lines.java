
package jwplot;

import static jwplot.Util.matches;
import static jwplot.Util.verbose;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.regex.Pattern;

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
import org.jfree.util.ShapeUtilities;

/**
 * See main() for command-line arguments
 * */
public class Lines
{
  static CheckedProperties properties;

  static String title = null;
  static String xlabel = "x";
  static String ylabel = "y";

  /** If true, use only black and white */
  public static boolean bw = false;

  static String axis_type_x = "normal";
  static String axis_type_y = "normal";

  /** Font size for axes labels.  If 0, use default */
  static int axis_label_font_size = 0;
  /** Font size for axes tick labels.  If 0, use default */
  static int axis_tick_font_size = 0;

  static int width = 400;
  static int height = 400;

  // null indicates the value was not set by the user
  static Double xmin = null;
  static Double xmax = null;
  static Double ymin = null;
  static Double ymax = null;

  static boolean legendEnabled = true;
  static String legendPosition = "bottom";

  static List<XYTextAnnotation> notes =
      new ArrayList<XYTextAnnotation>();

  /**
       @param files: output_file properties_file data_file*
       Reads settings from properties: see scanProperties()
       Produces EPS output file
       Data files are two-columns of numbers each -
       see LineReader.read() and LineReader.array()
   */
  public void plotter(Map<String,String> cmdProps, List<String> files)
  throws UserInputException
  {
    if (files.size() < 2)
      throw new UserInputException
        ("Insufficient command line arguments!");
    String output   = files.get(0);
    String propFile = files.get(1);
    List<String> names = new ArrayList<String>();
    for (int i = 2; i < files.size(); i++)
      names.add(files.get(i));

    List<double[][]> data = new ArrayList<double[][]>();
    List<String> labels = new ArrayList<String>();

    properties = new CheckedProperties();
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
      // System.out.println("name: " + name);
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
      Color color = lineColor(description);
      if (color != null)
        renderer.setSeriesPaint(i, color);
      renderer.setSeriesShape(0, ShapeUtilities.createRegularCross(2,2));
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
    if (xmin != null || xmax != null || axis_type_x.equals("integer"))
    {
      NumberAxis axis = (NumberAxis) plot.getDomainAxis();
      Range range = axis.getRange();
      axmin = range.getLowerBound();
      axmax = range.getUpperBound();
      if (xmin != null) axmin = xmin;
      if (xmax != null) axmax = xmax;
      axis.setRange(axmin, axmax);
      if (axis_type_x.equals("integer"))
        axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    }

    if (ymin != null || ymax != null || axis_type_y.equals("integer"))
    {
      ValueAxis axis = plot.getRangeAxis();
      Range range = axis.getRange();
      aymin = range.getLowerBound();
      aymax = range.getUpperBound();
      if (ymin != null) aymin = ymin;
      if (ymax != null) aymax = ymax;
      axis.setRange(aymin, aymax);
      if (axis_type_y.equals("integer"))
        axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    }
    //  NumberAxis axis = (NumberAxis) plot.getRangeAxis();
    // axis.setTickUnit(new NumberTickUnit(1, new DecimalFormat("0"), 1));

    if (axis_label_font_size != 0)
    {
      Font font = new Font("Dialog",  Font.PLAIN, axis_label_font_size);
      ValueAxis axis_x = plot.getDomainAxis();
      ValueAxis axis_y = plot.getRangeAxis();
      axis_x.setTickLabelFont(font);
      axis_y.setTickLabelFont(font);
    }
    if (axis_tick_font_size != 0)
    {
      Font font = new Font("Dialog",  Font.BOLD, axis_tick_font_size);
      ValueAxis axis_x = plot.getDomainAxis();
      ValueAxis axis_y = plot.getRangeAxis();
      axis_x.setLabelFont(font);
      axis_y.setLabelFont(font);
    }
    /*
    Font fontPlain = new Font("Dialog", Font.PLAIN, 28);
    Font fontBold = new Font("Dialog",  Font.BOLD,  24);
    ValueAxis axis_y = (ValueAxis) plot.getRangeAxis();
    axis_y.setTickLabelFont(fontPlain);
    axis_y.setLabelFont(fontBold);
    ValueAxis axis_x = (ValueAxis) plot.getDomainAxis();
    axis_x.setTickLabelFont(fontPlain);
    axis_x.setLabelFont(fontBold);
     */
  }

  private static void setAxisTypes(XYPlot plot)
  throws UserInputException
  {
    if (axis_type_x.equals("logarithmic"))
    {
      ValueAxis domainAxis = new LogarithmicAxis(xlabel);
      plot.setDomainAxis(domainAxis);
    }
    else if (axis_type_x.equals("log"))
    {
      ValueAxis domainAxis = new LogAxis(xlabel);
      plot.setDomainAxis(domainAxis);
    }
    else if (axis_type_x.equals("date"))
    {
      DateAxis domainAxis = new DateAxis();
      plot.setDomainAxis(domainAxis);
    }
    else if (axis_type_x.equals("normal") ||
             axis_type_x.equals("integer"))
    { /* OK */
    }
    else
      throw new UserInputException
      ("Invalid axis.x type: " + axis_type_x);

    if (axis_type_y.equals("logarithmic"))
    {
      //
      ValueAxis rangeAxis = new LogarithmicAxis(ylabel);
      plot.setRangeAxis(rangeAxis);
    }
    else if (axis_type_y.equals("log"))
    {
      ValueAxis rangeAxis = new LogAxis(ylabel);
      plot.setRangeAxis(rangeAxis);
    }
    else if (axis_type_y.equals("normal") ||
             axis_type_y.equals("integer"))
    { /* OK */ }
    else
      throw new UserInputException
      ("Invalid axis.y type: " + axis_type_y);
  }

  static void placeLegend(JFreeChart chart)
  throws UserInputException
  {
    if (legendPosition == null) return;

    XYPlot plot = chart.getXYPlot();
    // http://stackoverflow.com/questions/11320360/embed-the-legend-into-the-plot-area-of-jfreechart
    LegendTitle legend = new LegendTitle(plot);
    // LegendTitle legend = chart.getLegend();


    // legend.setItemFont(new Font("Dialog", Font.PLAIN, 24));

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
      XYTitleAnnotation ta =
        new XYTitleAnnotation(0.10, 0.00001, legend, RectangleAnchor.BOTTOM_RIGHT);
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
       notes: see loadNotes()
   */
  static void scanProperties()
  throws UserInputException
  {
    title  = properties.assign("title",  title);
    xlabel = properties.assign("xlabel", xlabel);
    ylabel = properties.assign("ylabel", ylabel);
    width  = properties.assign("width",  width);
    height = properties.assign("height", height);
    xmin   = properties.assign("xmin",   xmin);
    xmax   = properties.assign("xmax",   xmax);
    ymin   = properties.assign("ymin",   ymin);
    ymax   = properties.assign("ymax",   ymax);

    bw = properties.assign("bw", bw);
    legendEnabled  = properties.assign("legend.enabled",  legendEnabled);
    legendPosition = properties.assign("legend.position", legendPosition);

    axis_type_x = properties.assign("axis.type.x",    axis_type_x);
    axis_type_y = properties.assign("axis.type.y",    axis_type_y);
    axis_label_font_size = properties.assign("axis.label.font.size",
                                             axis_label_font_size);
    axis_tick_font_size  = properties.assign("axis.tick.font.size",
                                             axis_tick_font_size);

    if (properties.getProperty("notes") != null)
      loadNotes();
  }

  static void loadNotes()
  throws UserInputException
  {
    String s = properties.getProperty("notes");
    int count = Integer.parseInt(s);
    for (int i = 0; i < count; i++)
    {
      String p = "note."+i;
      String d = properties.getProperty(p);
      if (d == null)
        throw new UserInputException
          ("Could not find note." + i + " (notes=" + count + ")");
      String[] tokens = d.split("\\s+");
      double x = Double.parseDouble(tokens[0]);
      double y = Double.parseDouble(tokens[1]);
      String text = Util.concat(tokens, 2);
      // System.out.println("note: " + x + " " + y + " '" + text + "'");
      XYTextAnnotation note = new XYTextAnnotation(text, x, y);
      Font font1 = note.getFont();
      Font font2 = font1.deriveFont(12.0f);
      note.setFont(font2);
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

  static Color lineColor(String name)
  throws UserInputException
  {
    String s;
    s = properties.getProperty("color."+name);
    if (s != null)
      return mapToColor(s);
    // System.out.println(s);
    s = properties.getProperty("color.all");
    if (s != null)
      return mapToColor(s);
    return null;
  }

  static BasicStroke dottedStroke =
    new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                    1.0f, new float[] {6.0f, 6.0f}, 0.0f);

  static void formatLine(String name,
                         XYLineAndShapeRenderer renderer, int index)
  throws UserInputException
  {
    String lineStyle = properties.getProperty("line."+name);
    if ("dotted".equals(lineStyle))
      renderer.setSeriesStroke(index, dottedStroke);
    String colorName = propertiesMatch("color."+name);
    try
    {
      if (colorName != null)
        renderer.setSeriesPaint(index, mapToColor(colorName));
    }
    catch (UserInputException e)
    {
      throw new UserInputException(e.toString() +
                                   " for '" + name + "'");
    }
  }

  static String propertiesMatch(String name)
  {
    for (Object k : Collections.list(properties.keys()))
    {
      String key = (String) k;
      String pattern = createRegexFromGlob(key);
      // System.out.println(pattern);
      if (Pattern.matches(pattern, name))
      {
        // System.out.println("\t match");
        return (String) properties.get(key);
      }
    }
    return null;
  }

  // https://stackoverflow.com/questions/1247772/is-there-an-equivalent-of-java-util-regex-for-glob-type-patterns
  public static String createRegexFromGlob(String glob)
  {
    StringBuilder result = new StringBuilder();
    for(int i = 0; i < glob.length(); i++)
    {
      final char c = glob.charAt(i);
      switch(c)
      {
        case '*':  result.append(".*"); break;
        case '?':  result.append('.');   break;
        case '.':  result.append("\\."); break;
        case '\\': result.append("\\\\"); break;
        default: result.append(c);
      }
    }
    return result.toString();
  }

  static Color mapToColor(String colorName)
  throws UserInputException
  {
    Color color;
    switch (colorName.toLowerCase())
    {
      case "black":
        color = Color.BLACK;
        break;
      case "blue":
        color = Color.BLUE;
        break;
      case "cyan":
        color = Color.CYAN;
        break;
      case "darkgray":
        color = Color.DARK_GRAY;
        break;
      case "gray":
        color = Color.GRAY;
        break;
      case "green":
        color = Color.GREEN;
        break;
      case "yellow":
        color = Color.YELLOW;
        break;
      case "lightgray":
        color = Color.LIGHT_GRAY;
        break;
      case "magenta":
        color = Color.MAGENTA;
        break;
      case "orange":
        color = Color.ORANGE;
        break;
      case "pink":
        color = Color.PINK;
        break;
      case "red":
        color = Color.RED;
        break;
      case "white":
        color = Color.WHITE;
        break;
      default:
        throw new UserInputException("unknown color: " + colorName);
    }
    return color;
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
