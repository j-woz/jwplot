
= JWPlot

:toc:

by Justin M Wozniak

== Overview

This documents a plotter for simple data sets.  It produces EPS output
from two-column data sets using JFreeChart.  The user interface is
+bin/jwplot+

The overall idea is to produce high-quality, easily customizable plots
by using the rich feature set in
http://www.jfree.org/jfreechart[JFreeChart] and providing a simple,
scriptable user interface.

It should be easy to extend the plotter to use additional JFreeChart features.

== Installation

Just check out from SVN and build with Ant:

----
svn co svn+ssh://jmwozniak@svn.code.sf.net/p/jwplot/code/trunk
cd plotter
ant
----

== Usage

The plotter accepts a configuration file, a target output file name
for the EPS, and a variable number of data files:

----
jwplot my_output.eps my_config.cfg my_data.data
----

(The output file is specified first for easier use with multiple data files in a Makefile.)

== Configuration

The output is controlled by the configuration (CFG) file which is
formatted as a Java properties file.  Examples may be found
in the +examples/+ directory.

Java properties files have a +key=value+ format where comments are
denoted with *#*.

For example, +plot.cfg+ contains:
----
# Set the x label to "Input"
xlabel = Input
# Set the y label to "Output"
ylabel = Output
# For the data series in file "set2.data", use no shapes
shape.set2.data = none
# For the data series in file "set1.data", use legend label "Set 1"
label.set1.data = Set 1
----

=== Configuration properties

* Numbers may be formatted in obvious ways.
  Commas are automatically removed from numbers!
  (Cf. Java +Double.parseDouble()+.)
* True/false may be +true+ or +false+.
  (Cf. Java +Boolean.parseBoolean()+.)
* Some properties are set on a per-data-file basis.  In this case, part of
  the key is the file name.

+title+:: Set a plot title.
+xlabel,ylabel+:: Set labels for the X and Y axes.
+width,height+:: Set the width, height of the output EPS in pixels.
Defaults to 400 by 400.
+xmin,xmax,ymin,ymax+:: Set the visible region of the plot.
Defaults to an auto-selection made by JFreeChart.
+bw+:: If true, use black and white only.  Default: +false+.
+legend.enabled+:: If true, show a legend.  Default: +true+.
+legend.font.size+:: If set, the font size for the legend.  Default: +JWPlot selection+

+axis.x,axis.y+::
May be +normal+, +logarithmic+ (tends to use scientific notation in labels), +log+, or +date+.
For +date+, provide date data as decimal milliseconds since the Unix Epoch.  (Milliseconds are required by JFreeChart.)
Default: +normal+.

+axis.tick.font.size+:: If set, the font size for the axis tick numbers.  Default: +JWPlot selection+
+axis.label.font.size+:: If set, the font size for the axis labels.  Default: +JWPlot selection+
+label._filename_+:: Set the legend label for the data series from
file +_filename_+.
+shape._filename_+:: If +none+, use no shape for the data series from
file +_filename_+.
+notes+:: The number of notes in the file, numbered starting from 0.
+note._i_+:: The _x_, _y_, and text for note _i_.   The text is
centered on _x_, _y_ with respect to the plot.

////
+legend.position+:: Where to put the legend.  May be top, bottom, right, left, or SW.  Default: bottom.
////

Example:
----
width = 6000
height = 1000
xmin = 2
xmax = 3
ymin = 4
ymax = 5
bw = true
# Keep X axis normal but use logarithic Y axis
axis.y = logarithmic
# A text note
notes = 1
note.0 2.5 4.5 This is my text label.
----

== Output

EPS files were chosen because of the ease of using them in LaTeX
documents and the relative ease of producing other high-quality
images from them via ImageMagick:

----
convert my_output.eps my_output.png
convert my_output.eps my_output.pdf
----

== Data

The data input is two columns of plain text numbers:

----
1.965 1.5

8 0
# This is a comment
1 7,000,000.1 # This is also a comment
----

As shown, empty lines and +#+ comments are accepted.
Commas are also automatically stripped.

== Plotter options

The plotter accepts the following options on the command line.

+-s+:: Use a single Y axis.
+-d+:: Use a double Y axis (dual mode).
+-v+:: Turn on verbose output.

== Dual mode

Dual mode allows the user to plot exactly two data series on the
same plot, with separate X axes.

. Use +-d+ above.
. Specify two data files.
. Instead of +ymin+, +ymax+, specify +ymin1+, +ymax1+,
  +ymin2+, +ymax2+.
. The first data series will be plotted with respect to a left-hand
  Y axis and the second data series with respect to a right-hand
  Y axis.

== Issues, suggestions, and feature requests

Please use the JWPlot issue tracker: https://sourceforge.net/p/jwplot/tickets

////
Local Variables:
mode: doc
End:
////
