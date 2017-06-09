#! /bin/bash

JAVA_HOME=$JAVA_HOME_1_8
export JAVA_HOME
PATH=$JAVA_HOME/bin:$PATH
export PATH

MAVEN_HOME=$MAVEN_2_2_1
PATH=$MAVEN_HOME:$PATH
export PATH

mvn -U clean deploy -DskipTests=true
mkdir output

