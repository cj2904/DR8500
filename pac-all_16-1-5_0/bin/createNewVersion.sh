#!/bin/bash

SCRIPT_DIR=$(dirname $0)                # get directory containing this script
cd $SCRIPT_DIR/..
APP_DIR=$(pwd)                          # get root directory for the pac server
cd - > /dev/null
TARGET_DIR=$1

###########################################################################
# copy zam archive to target directory
###########################################################################
echo "Copy zam archive to target directory..."
cp -p $APP_DIR/newAppVersionTemp/*.zar  $TARGET_DIR/
COPY_RESULT=$?
if [ $COPY_RESULT -ne 0 ]; then
    echo "Failed to copy zam archive. Exit code $COPY_RESULT"
else
    echo "Copy of zam archive successful!"
fi
exit $COPY_RESULT
