#!/bin/bash

asciidoc -a stylesheet=$PWD/index.css \
         -a max-width=750px \
         -a textwidth=80 index.txt
