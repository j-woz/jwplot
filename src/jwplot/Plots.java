
package jwplot;

import java.awt.Color;
import java.awt.Font;

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
                          CheckedProperties properties)
  throws UserInputException
  {
    LegendTitle legend = chart.getLegend();
    if (legend == null) return;
    Font font = getLegendFont(properties);
    if (font == null) return;
    legend.setItemFont(font);
  }

  static Font getLegendFont(CheckedProperties properties)
  throws UserInputException
  {
    int i = properties.assign("legend.font.size", 0);
    if (i == 0) return null;
    return new Font("Dialog", Font.PLAIN, i);
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
