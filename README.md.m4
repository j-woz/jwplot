# jwplot

Plotter based on JFreeChart for simple, general-purpose plotting from
the shell

* [User guide](https://jmwozniak.github.io/jwplot)


m4_define(`md_code', m4_changequote([,
])[m4_changequote([,])```m4_changequote(`,')]m4_changequote(`,'))

## Example

1. Consider this JWPlot configuration file (Java properties format):

md_code() m4_dnl
m4_include(examples/lines/jw.cfg)
md_code() m4_dnl

2. And these data files:

m4_include(examples/lines/j.data)

m4_include(examples/lines/w.data)

3. Run this command line:

4. Get this output:

![jw.png](examples/lines/jw.png)

m4_dnl Local Variables:
m4_dnl mode: Fundamental
m4_dnl End:
