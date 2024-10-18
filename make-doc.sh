#!/bin/bash

# 2024-10-18: Asciidoc/Python may produce Mac end-of-line characters.
#             Ignore this.

asciidoc -a stylesheet=$PWD/index.css \
         -a max-width=750px \
         -a textwidth=80 \
         index.txt
