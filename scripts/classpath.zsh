
# Source this to set up classpath for plotter runs
# Works on Linux and Cygwin
# classpath stored in ${CP}

if [[ $( uname ) == CYGWIN* ]]
then
    typeset -T CP cp ";"
    cp+=c:/cygwin64${JWPLOT_HOME}/src
    for jar in ${JWPLOT_HOME}/lib/*.jar
    do
      cp+=c:/cygwin64${jar}
    done
else
    typeset -T CP cp
    CP=${CLASSPATH}:${JWPLOT_HOME}/src
    cp+=( ${JWPLOT_HOME}/lib/*.jar )
fi
