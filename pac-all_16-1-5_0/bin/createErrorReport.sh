#!/bin/bash

SCRIPT_DIR=$(dirname $0)                # get directory containing this script
cd $SCRIPT_DIR/..
APP_DIR=$(pwd)                          # get root directory for the pac server
cd - > /dev/null
TARGET_DIR=$1
MAIN_CLASS=de.znt.pac.ProcessAutomationController       # The main class of the pac server

get_pid()
{
  pgrep -u $USER -o -f "\-Dapp.home=$APP_DIR $MAIN_CLASS"
}

###########################################################################
# Create Thread dump
###########################################################################
APPPID=$(get_pid)
if [ -n "$APPPID" ] ; then
  jstack $APPPID  >> $TARGET_DIR/StackTrace.txt
else
  echo "Pac process is not running currently" > $TARGET_DIR/StackTrace.txt    	
fi

###########################################################################
# list all available files
###########################################################################
ls -Rall > $TARGET_DIR/Files.txt

###########################################################################
# copy log entries: take only pac and secs log files from the last 3 days
###########################################################################
cp -rp $APP_DIR/log  $TARGET_DIR/
find $TARGET_DIR/log/zsecs* -mtime +3 -exec rm {} \;
find $TARGET_DIR/log/pac.log* -mtime +3 -exec rm {} \;
cp -rp $APP_DIR/dat $TARGET_DIR/
find $TARGET_DIR/dat/db* -type d -exec rm -r {} \;
if [ -f "$APP_DIR/dumps" ] ; then
  cp -rp $APP_DIR/dumps $TARGET_DIR
fi


###########################################################################
# copy all configuration files
###########################################################################
cp -rp $APP_DIR/config $TARGET_DIR

echo "###########################################################################" > $TARGET_DIR/SystemInfo.txt
echo "# Physical Memory:" >> $TARGET_DIR/SystemInfo.txt
free -m >> $TARGET_DIR/SystemInfo.txt

echo "###########################################################################" >> $TARGET_DIR/SystemInfo.txt
echo "# Disk:" >> $TARGET_DIR/SystemInfo.txt
df -h >> $TARGET_DIR/SystemInfo.txt

echo "###########################################################################" >> $TARGET_DIR/SystemInfo.txt
echo "# Network:" >> $TARGET_DIR/SystemInfo.txt
/sbin/ifconfig >> $TARGET_DIR/SystemInfo.txt

echo "###########################################################################" >> $TARGET_DIR/SystemInfo.txt
echo "# Processes:" >> $TARGET_DIR/SystemInfo.txt
ps -ef >> $TARGET_DIR/SystemInfo.txt
