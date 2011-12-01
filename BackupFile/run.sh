#!/bin/sh

echo
echo "Swing Example"
echo "-------------------"
echo

if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  exit 1
fi

if [ `uname | grep -n CYGWIN` ]; then
  PS=";"
elif [ `uname | grep -n Windows` ]; then
  PS=";"
else
  PS=":"
fi

LOCALCLASSPATH=${JAVA_HOME}/lib/tools.jar${PS}${JAVA_HOME}/lib/dev.jar${PS}./target/classes
for i in ./lib/*; do
  LOCALCLASSPATH=$LOCALCLASSPATH${PS}$i
done

echo Running with classpath $LOCALCLASSPATH
echo Starting...
echo

"$JAVA_HOME/bin/java" -classpath "$LOCALCLASSPATH" example.MainPanel $*
