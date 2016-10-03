
# Source this to set up classpath for plotter runs
# Works on Linux and Cygwin
# classpath stored in ${CP}

if [[ $( uname ) == CYGWIN* ]]
then
  typeset -T CP cp ";"
  CYGWIN_NAME="cygwin"
  CYGPWD=$( cygpath --mixed ${JWPLOT_HOME} )
  if [[ ${CYGPWD} =~ c:/cygwin64* ]]
  then
    CYGWIN_NAME="cygwin64"
  fi
  cp+=c:/${CYGWIN_NAME}${JWPLOT_HOME}/src
  for jar in ${JWPLOT_HOME}/lib/*.jar
  do
    cp+=c:/${CYGWIN_NAME}${jar}
  done
else # Normal Linux/Unix
  typeset -T CP cp
  CP=${CLASSPATH}:${JWPLOT_HOME}/src
  cp+=( ${JWPLOT_HOME}/lib/*.jar )
fi
