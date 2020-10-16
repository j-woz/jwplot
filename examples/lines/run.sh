#!/bin/zsh
set -e

# Make a simple plot with JWPlot

push ../.. ; ant ; pop

../../bin/jwplot -v out.eps plot.cfg set1.data
