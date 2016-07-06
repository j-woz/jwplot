
package jwplot;

import static jwplot.Util.verbose;

import java.util.*;

import gnu.getopt.Getopt;

public class Main
{
  public static void main(String[] argv)
  {
    Bits.init();
    Util.verbose(false);

    Map<String,String> cmdProps = new HashMap<>();
    List<String> files = new ArrayList<>();
    char mode = processArgv(argv, cmdProps, files);
    try
    {
      switch (mode)
      {
        case 's':
        {
          Lines l = new Lines();
          l.plotter(cmdProps, files);
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
  static char processArgv(String[] argv,
                          Map<String,String> cmdProps,
                          List<String> files)
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
    {
      String arg = argv[i];
      if (arg.contains("=")) mapAdd(cmdProps, arg);
      else                   files.add(arg);
    }
    return mode;
  }

  static void mapAdd(Map<String,String> cmdProps, String arg)
  {
    String[] tokens = arg.split("=");
    verbose("command line property: " + tokens[0] + "=" + tokens[1]);
    cmdProps.put(tokens[0], tokens[1]);
  }

  public static void usage()
  {
    System.err.println( "usage: [-s,-d] [<OPTIONS>] <CFG> <OUTPUT> <DATA>*" );
    System.exit(2);
  }
}
