# jwplot

Plotter based on JFreeChart for simple, general-purpose plotting from
the shell

* [User guide](https://jmwozniak.github.io/jwplot)

m4_dnl Example from file:
m4_define(`fexample', `*$1*
md_code()m4_include($1)md_code()')

m4_define(`md_code', m4_changequote([,
])[m4_changequote([,])```m4_changequote(`,')]m4_changequote(`,'))

m4_changequote()

## Example

Consider this JWPlot configuration file (Java properties format):

fexample(examples/lines/jw.cfg)

And these data files:

fexample(examples/lines/j.data)

fexample(examples/lines/w.data)

Run these command lines:

md_code()
$ jwplot jw.cfg jw.eps j.data w.data
$ convert jw.eps jw.png`'md_code()

Get this output:

![jw.png](examples/lines/jw.png)

m4_dnl Local Variables:
m4_dnl mode: Fundamental
m4_dnl End:
