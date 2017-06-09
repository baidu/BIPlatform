#! /bin/sh
############################################
#一键启动脚本
############################################
echo "being start demo project, please wait a moment ... ..."
if [ ! -n "$JAVA_HOME" ]; then
	echo "[ERROR]************please make sure you have already install java 8 and set JAVA_HOME in your profile"
	exit 1
fi

if [ ! -x "$JAVA_HOME/bin/java" ]; then
	echo "[ERROR]************please make sure you can invoke the command $JAVA_HOME/bin/java"
	exit 1
fi
jdk_version=`$JAVA_HOME/bin/java -version 2>&1`
echo "[INFO]current jdk version check value is : $jdk_version"

version_num=`echo $jdk_version | awk -F ' ' '{print $3}' | awk -F '_' '{print substr($1, 2, 3)}' | awk -F '.' '{print $2}'`
if [ $version_num -lt 8 ]; then
	echo "[ERROR]************please make sure current version number over than 1.8"
	exit 1
fi

parentPath=$(dirname $(pwd))
current_product_version=`cat ./version.txt>&1`
demo_location=$parentPath/demo_home 
echo "[INFO]************current product version is : $current_product_version"
$JAVA_HOME/bin/java -cp  $demo_location/db/h2-1.3.175.jar org.h2.tools.Server -tcp -tcpAllowOthers -tcpPort 9999 >$demo_location/log/db.log &
echo "[INFO]************db server start successfully"
echo "[INFO]************init DB need a long time, please wait a moment please ... ... "
sleep 10
echo "[INFO]************successfully init db"
echo "[INFO]************begin start fileserver"
sleep 2
$JAVA_HOME/bin/java -jar  $parentPath/lib/fileServer-$current_product_version.jar 9090 $demo_location > $demo_location/log/fileserver.log &
sleep 2
echo "[INFO]************fileserver start successfully"
echo "[INFO]************begin start tesseract"
$JAVA_HOME/bin/java -jar  $parentPath/lib/tesseract-$current_product_version.jar -Dserver.port=9191 > $demo_location/log/ter.log &
sleep 5
echo "[INFO]************tesseract started successfully"
echo "[INFO]************begin start silkroad"
$JAVA_HOME/bin/java -jar  $parentPath/lib/designer-$current_product_version.jar  > $demo_location/log/designer.log &
sleep 60
echo "[INFO]************silkroad started successfully"
echo "[INFO]************Congratulation! you can using BI-Platform  with URL :[http://localhost:8090/silkroad/home.html ] and user [demo/demo] through Chrome Browser "
exit 0
