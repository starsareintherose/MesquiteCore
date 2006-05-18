#!/bin/bash

#Script to identify java VM and modify classpath on cygwin
#Written by Travis Wheeler (www.traviswheeler.com)

#If you find bugs or modify the script to improve functionality, 
# please let us know so we can improve the script for everyone


# Thanks to William Cook (http://www.cs.utexas.edu/users/wcook/) for the bit that
# identifies the path containing this script, and uses it to set the classpath. 
# (the exact site of the work I based my code on is lost to antiquity)


#figure out where java lives 
if [ $MESQUITE_JAVA_HOME ]
then
  java=$MESQUITE_JAVA_HOME/bin/java
elif [ $JAVA_HOME ]
then
  java=$JAVA_HOME/bin/java
else
  tmp=`java -version 2>&1`
  if echo "$tmp" | grep -q "command not found"  # no "java", so try "jre"
  then
    tmp=`jre -version 2>&1`
    if echo "$tmp" | grep -q "command not found"
    then
       echo "Can't find java. Try setting either the JAVA_HOME environment variable"
       exit
    else
       java="jre"
    fi
  else
   java="java" 
  fi
fi


# figure out where I live, then run java w/ my containing dir as classpath  
dir=`dirname $0`
os=`uname`
if test ${os:0:6} = "CYGWIN"
then
  if test ${dir:1:8} = "cygdrive"
  then
    dir="${dir:10:1}:/${dir:12}"
  fi
  chmod -R u+w  $dir/com/
  cp -r  $dir/../Resources/com/* $dir/com/
  $java -cp "$dir" mesquite.Mesquite $*
else
  chmod -R u+w  $dir/com/
  cp -r  $dir/../Resources/com/* $dir/com/
  echo "$java -cp $dir mesquite.Mesquite $*"
  $java -cp "$dir" mesquite.Mesquite $*
fi