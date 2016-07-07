
package jwplot;

import java.util.ArrayList;
import java.util.List;

import gnu.getopt.Getopt;

public class Main
{
  public static void main(String[] argv)
  {
    Bits.init();
    Util.verbose(false);
    
    List<String> files = new ArrayList<String>();
    char mode = processArgv(argv, files);
    try
    {
      switch (mode)
      {
        case 's': 
        {
          Lines l = new Lines();
          l.plotter(files);
          break;
        }
        case 'd':
        {
          Dual d = new Dual();
          d.plotter(files);
          break;
        }
        case 'b':
        {
          Bars.bars(files, true);
          break;
        }
      }
    }
    catch (UserInputException e)
    {
      System.out.println("input error: ");
      System.out.println(e.getMessage());
      // e.printStackTrace();
      System.exit(1);
    }
  }
  
  /**  
     Returns mode character and fills in List files 
   * @return
   */
  static char processArgv(String[] argv, List<String> files)
  {
    char mode = 's';
    Getopt g = new Getopt("jwplot", argv, "bdsv");
    int c;
    while ((c = g.getopt()) != -1)
    {
      switch (c)
      {
        case 's':
        {
          mode = 's';
          break;
        }
        case 'd':
        {
          mode = 'd';
          break;
        }
        case 'b':
        {
          mode = 'b';
          break;
        }
        case 'v': 
        {
          Util.verbose(true);
          break;
        }
        default:
        {
          System.out.printf("g %c\n", c);
          usage();
        }
      }
    }
    for (int i = g.getOptind(); i < argv.length ; i++)
      files.add(argv[i]);
    return mode;
  }

  public static void usage()
  {
    System.err.println( "usage: [-s,-d] [<OPTIONS>] <CFG> <OUTPUT> <DATA>*" );
    System.exit(2);
  }
}
