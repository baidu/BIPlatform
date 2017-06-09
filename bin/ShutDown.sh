#! /bin/sh

echo "being start demo project, please wait a moment ... ..."
if [ ! -n "$JAVA_HOME" ]; then
	echo "[ERROR]************please make sure you have already install java 8 and set JAVA_HOME in your profile"
	exit 1
fi

if [ ! -x "$JAVA_HOME/bin/java" ]; then
	echo "[ERROR]************please make sure you can invoke the command $JAVA_HOME/bin/java"
	exit 1
fi

file_server_id=`$JAVA_HOME/bin/jps | grep fileServer | awk -F ' ' '{print $1}'`
echo $file_server_id
kill -9 $file_server_id

silkroad_id=`$JAVA_HOME/bin/jps | grep designer | awk -F ' ' '{print $1}'`
echo $silkroad_id
kill -9 $silkroad_id

tesseract_id=`$JAVA_HOME/bin/jps | grep tesseract | awk -F ' ' '{print $1}'`
echo $tesseract_id
kill -9 $tesseract_id

parentPath=$(dirname $(pwd))
$JAVA_HOME/bin/java -cp $parentPath/demo_home/db/h*.jar org.h2.tools.Server  -tcpShutdown tcp://localhost:9999