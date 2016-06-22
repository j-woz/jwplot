
package jwplot;

import java.awt.Color;
import java.awt.Font;
import java.util.Properties;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.title.LegendTitle;

/**
 * Reusable JFreeChart-specific functions
 * @author wozniak
 *
 */
public class Plots
{
  static void setupLegend(JFreeChart chart,
                          Properties properties)
  {
    LegendTitle legend = chart.getLegend();
    Font font = getLegendFont(properties);
    if (font != null)
      legend.setItemFont(font);
  }

  static Font getLegendFont(Properties properties)
  {
    Font result = null;
    String name = properties.getProperty("legend.font");
    if (name == null)
      return null;
    result = new Font(name, Font.PLAIN, 12);
    return result;
  }

  static Color string2color(String s)
  throws UserInputException
  {
    Color result = null;

    if (s.compareToIgnoreCase("black") == 0)
      result = Color.BLACK;
    if (s.compareToIgnoreCase("white") == 0)
      result = Color.WHITE;
    if (s.compareToIgnoreCase("red") == 0)
      result = Color.RED;
    if (s.compareToIgnoreCase("blue") == 0)
      result = Color.BLUE;
    if (s.compareToIgnoreCase("green") == 0)
      result = Color.GREEN;

    if (result == null)
      throw new UserInputException("unknown color: " + s);

    return result;
  }
}
