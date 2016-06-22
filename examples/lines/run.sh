#!/bin/zsh
set -e

# Make a simple plot with JWPlot

push ../.. ; ant ; pop

../../bin/jwplot -v plot.cfg out.eps set1.data
