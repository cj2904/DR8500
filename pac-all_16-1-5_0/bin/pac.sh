#!/bin/bash
# ****************************************************************************
# *                                                                          *
# *  Program:  "@(#)PAC 1.00"                              Shell: bash       *
# *                                                                          *
# *  Descrpt:  Control-Script for PAC-Server                                 *
# *                                                                          *
# ****************************************************************************
# IMPORTANT: Do not add any additional parameters (See PACALL-164)

PRG=$(basename $0)                      # get scriptname (for logging)
SCRIPT_DIR=$(dirname $0)                # get directory containing this script
cd $SCRIPT_DIR/..
APP_DIR=$(pwd)                          # get root directory for the pac server
# Define restore and backup directory
RESTORE_DIR=$APP_DIR/restore
BACKUP_DIR=$APP_DIR/backup
cd - > /dev/null

PROPERTY_FILE=$APP_DIR/config/properties/application.properties      # Property file for PropertyHelper
INSTANCE_PROPERTIES=$APP_DIR/INSTANCE.properties

STST_LOG="$APP_DIR/log/startstop.log"   # StartStop-Logfile

MAIN_CLASS=de.znt.pac.ProcessAutomationController       # The main class of the pac server

APP_NAME=pac                     # Application name for log messages

DATABASE_NAME=pac               # Database name for dbcheck, zpers_export and zpers_import

# Set application specific classpath

CP=$APP_DIR/config/i18n
CP=$CP:$APP_DIR/lib/ext/*
CP=$CP:$APP_DIR/lib/esa/*
CP=$CP:$APP_DIR/lib/*

echo "$(date --rfc-3339=ns) $CP"

# Default values of instance properties
maxHeapSize=256M

print_usage_and_exit()
{
  echo "$(date --rfc-3339=ns) Usage: $PRG command" 1>&2
  echo "Available commands are:" 1>&2
  echo "   start" 1>&2
  echo "         starts the PAC service" 1>&2
  echo "   stop" 1>&2
  echo "         stops the PAC service" 1>&2
  echo "   kill" 1>&2
  echo "         kills the PAC service" 1>&2
  echo "   restart" 1>&2
  echo "         restarts the PAC service" 1>&2
  echo "   stat[us]" 1>&2
  echo "         prints the current state of the PAC service" 1>&2
  echo "   dump" 1>&2
  echo "         prints a full thread dump to the log" 1>&2
  echo "   log" 1>&2
  echo "         makes a tail -f on the log file" 1>&2
  echo "   backup" 1>&2
  echo "         creates a backup of the configuration files into the directory 'backup'" 1>&2
  echo "   dbcheck" 1>&2
  echo "         checks the consistency of the zpers db" 1>&2
  echo "   zpers_export" 1>&2
  echo "         exports the contents of the database in the current working directory to an image file" 1>&2
  echo "   zpers_import" 1>&2
  echo "         imports an image file into an database in the current working directory" 1>&2
  echo "   deleteZPers" 1>&2
  echo "         deletes database files" 1>&2
  echo "   copy_config" 1>&2
  echo "         copies all config files from etc to the given destination directory" 1>&2
  exit 1
}

read_instance_properties()
{
    if [ -f "$INSTANCE_PROPERTIES" ]
    then
        echo "$(date --rfc-3339=ns) $INSTANCE_PROPERTIES found."
        while IFS='=' read -r key value
        do
            key=$(echo $key | tr '.' '_')
            eval "${key}='${value}'"
        done < "$INSTANCE_PROPERTIES"

        if [ -n "${JVM_MaxHeapSize}" ];
            then maxHeapSize=${JVM_MaxHeapSize};
            echo "maxHeapSize="${JVM_MaxHeapSize}
        fi
        if [ -n "${SocketServer_Port}" ];
            then socket_server_port=${SocketServer_Port};
            echo "SocketServer.Port="${socket_server_port}
        fi
    else
        echo "$(date --rfc-3339=ns) $INSTANCE_PROPERTIES not found."
    fi
}

start_app()
{
  cd $APP_DIR
  read_instance_properties
  restore_from_backup
  if [ ! -d $APP_DIR\log ] ; then    
    mkdir $APP_DIR\log        
  fi
  
  $JAVA_HOME/bin/java $JVM_OPTS -cp $CP -Dde.znt.util.PropertyHelper.fileName=$PROPERTY_FILE -Duser.home=$APP_DIR -DAppDir=$APP_DIR -Djapps.home=$APP_DIR -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$DUMP_DIR -Xloggc:$APP_DIR\log\jvm-gc.log -Xmx$maxHeapSize -Dapp.home=$APP_DIR $MAIN_CLASS 2>&1 &
  cd - > /dev/null
}

restore_from_backup()
{
  cd $APP_DIR

  # Check if restore directory exists
  if [ -d restore ] ; then
    echo "$(date --rfc-3339=ns) Restoring from restore"
    cp -r restore/* ./
    echo "$(date --rfc-3339=ns) Restoring finished"
  else
  	echo "$(date --rfc-3339=ns) Nothing to restore"
  fi

  cd - > /dev/null
}

get_pid()
{
  pgrep -u $USER -o -f "\-Dapp.home=$APP_DIR $MAIN_CLASS"
}

# -----------------------------------------------------------------
# some checks if all needed Parameters are set?

if [ $(id -u) = "0" ] ; then		# test: if USER = root?
  echo "$(date --rfc-3339=ns) $PRG: you can't run this under root directly" 1>&2
  exit 1
fi

case "$1" in
start)
  APPPID=$(get_pid)
  if [ -n "$APPPID" ] ; then
    echo "$(date --rfc-3339=ns) $PRG: $APP_NAME is already running" 1>&2
    exit 0
  fi

  echo -n "$(date --rfc-3339=ns) $PRG: Starting $APP_NAME ..." 1>&2
  start_app

  sleep 5

  APPPID=$(get_pid)
  if [ -z "$APPPID" ] ; then
    echo "$(date --rfc-3339=ns) $PRG: $APP_NAME not yet started - wait and check again"
    sleep 3

    APPPID=$(get_pid)
    if [ -z "$APPPID" ] ; then
      MSG="$(date --rfc-3339=ns) $PRG: $APP_NAME not started - giving up!"
      echo "\n$MSG" 1>&2
      echo "$MSG" >>$STST_LOG
      exit $?
    fi
  fi

  echo " ok" 1>&2

  MSG="$(date --rfc-3339=ns) $PRG: Started $APP_NAME"
  echo "\n$MSG" 1>&2
  echo "$MSG" >>$STST_LOG
  ;;

stop)
  APPPID=$(get_pid)
  if [ -n "$APPPID" ] ; then
    MSG="$(date --rfc-3339=ns) $PRG: send signal 3 to $APP_NAME ($APPPID)"
    kill -3 $APPPID         # force Thread-Dump
    echo $MSG 1>&2
    echo $MSG >>$STST_LOG

    kill $APPPID
    echo -n "$(date --rfc-3339=ns) $PRG: Try to stop $APP_NAME " 1>&2

    while :					# wait until Shutdown finished
    do
      APPPID=$(get_pid)
      if [ -n "$APPPID" ] ; then
        echo -n "." 1>&2
        sleep 1
      else
        echo " ok" 1>&2
        MSG="$(date --rfc-3339=ns) $PRG: Stopped $APP_NAME"
        echo "\n$MSG" 1>&2
        echo "$MSG" >>$STST_LOG
        break
      fi
    done
  else
    echo "$(date --rfc-3339=ns) $PRG: $APP_NAME is not running" 1>&2
    exit 0
  fi
  ;;

kill)
  APPPID=$(get_pid)
  if [ -n "$APPPID" ] ; then
    MSG="$(date --rfc-3339=ns) $PRG: send signal 3 to $APP_NAME ($APPPID)"
    kill -3 $APPPID                      # force Thread-Dump
    echo $MSG 1>&2
    echo $MSG >>$STST_LOG

    sleep 5

    kill -9 "$APPPID" 2>&1 >/dev/null
    MSG="$(date --rfc-3339=ns) $PRG: Killed $APP_NAME"
    echo "\n$MSG" 1>&2
    echo "$MSG" >>$STST_LOG
  else
    echo "$(date --rfc-3339=ns) $PRG: $APP_NAME is not running" 1>&2
    exit 1
  fi
  ;;

restart)
  APPPID=$(get_pid)
  if [ -n "$APPPID" ] ; then
    MSG="$(date --rfc-3339=ns) $PRG: send signal 3 to $APP_NAME ($APPPID)"
    kill -3 $APPPID         # force Thread-Dump
    echo $MSG 1>&2
    echo $MSG >>$STST_LOG

    sleep 1

    echo -n "$(date --rfc-3339=ns) $PRG: Try to stop $APP_NAME " 1>&2
    kill $APPPID

    while :                 # wait until Shutdown finished
    do
      APPPID=$(get_pid)
      if [ -n "$APPPID" ] ; then
        echo -n "." 1>&2
        sleep 1
      else
        echo " ok" 1>&2
        MSG="$(date --rfc-3339=ns) $PRG: Stopped $APP_NAME"
        echo "\n$MSG" 1>&2
        echo "$MSG" >>$STST_LOG
        break
      fi
    done
  else
    echo "$(date --rfc-3339=ns) $PRG: $APP_NAME is not running" 1>&2
  fi

  echo -n "$(date --rfc-3339=ns) $PRG: Starting $APP_NAME ..." 1>&2
  start_app

  sleep 5

  APPPID=$(get_pid)
  if [ -z "$APPPID" ] ; then
    sleep 3

    APPPID=$(get_pid)
    if [ -z "$APPPID" ] ; then
      MSG="$(date --rfc-3339=ns) $PRG: $APP_NAME not started - giving up!"
      echo "\n$MSG" 1>&2
      echo "$MSG" >>$STST_LOG
      exit 1
    fi
  fi

  echo " ok" 1>&2

  MSG="$(date --rfc-3339=ns) $PRG: $APP_NAME started"
  echo "\n$MSG" 1>&2
  echo "$MSG" >>$STST_LOG
  ;;

stat|status)
  APPPID=$(get_pid)
  if [ -n "$APPPID" ] ; then
    echo "$(date --rfc-3339=ns) $PRG: $APP_NAME is running" 1>&2
    exit 0
  else
    echo "$(date --rfc-3339=ns) $PRG: $APP_NAME is not running" 1>&2
    exit 1
  fi
  ;;

dump)
  APPPID=$(get_pid)
  if [ -n "$APPPID" ] ; then
#    MSG="$(date --rfc-3339=ns) $PRG: send signal 3 to $APP_NAME ($APPPID)"
#    kill -3 $APPPID
#    echo $MSG 1>&2
#    echo $MSG >>$STST_LOG
    MSG="$(date --rfc-3339=ns) $PRG: trigger thread dump with jstack for $APP_NAME ($APPPID)"
    jstack $APPPID
    jstack $APPPID > $2/StackTrace.txt
  else
    echo "$(date --rfc-3339=ns) $PRG: $APP_NAME is not running" 1>&2
    exit 1
  fi
  ;;

log)					# shows newest log
  tail -f $APP_DIR/log/pac.log
  ;;

backup)					# creates a backup of the configuration files into the directory 'backup'
  if [ -d $BACKUP_DIR ] ; then
    echo "$(date --rfc-3339=ns) $PRG: ERROR: Backup directory '$BACKUP_DIR' already exists"
    exit -1
  fi

  # verify if backup tmp directory already exist
  TMP_BACKUP_DIR=$BACKUP_DIR.tmp
  if [ -d $TMP_BACKUP_DIR ] ; then
    echo "$(date --rfc-3339=ns) $PRG: Backup temporary directory 'backup.tmp' already exists - delete it"
    rm -r $TMP_BACKUP_DIR
  fi
  echo "$(date --rfc-3339=ns) $PRG: Create temporary backup directory '$TMP_BACKUP_DIR'"
  mkdir $TMP_BACKUP_DIR

  APPPID=$(get_pid)
  if [ -n "$APPPID" ] ; then
 	 echo "$(date --rfc-3339=ns) $PRG: Copy \dat without zpers to  '$TMP_BACKUP_DIR'"
  	 cp -rp $APP_DIR/dat $TMP_BACKUP_DIR > output.log
  	 rm -r $TMP_BACKUP_DIR/dat/db  > output.log
  	 rm -r $TMP_BACKUP_DIR/dat/deoClasses  > output.log
  else
     echo "$(date --rfc-3339=ns) $PRG: Copy \dat with zpers to  '$TMP_BACKUP_DIR'"
  	 cp -rp $APP_DIR/dat $TMP_BACKUP_DIR > output.log
  	 rm -r $TMP_BACKUP_DIR/dat/deoClasses  > output.log
  fi

  echo "$(date --rfc-3339=ns) Move '$TMP_BACKUP_DIR' to '$BACKUP_DIR'"
  mv $TMP_BACKUP_DIR $BACKUP_DIR
  echo "$(date --rfc-3339=ns) Backup finished!"
  ;;

dbcheck)                    # starts database checker
  echo -n "$(date --rfc-3339=ns) $PRG: Starting database checker ..." 1>&2

  $JAVA_HOME/bin/java $JVM_OPTS -cp $APP_DIR/app/lib/\* -Djava.awt.headless=true -Dde.znt.util.PropertyHelper.fileName=$PROPERTY_FILE de.znt.zpers.client.ZPersDbChecker $APP_DIR/dat/db/$DATABASE_NAME $2 $3 1>&2
  ;;

zpers_export)
  if [ !-f $DATABASE_NAME.db ] ; then
    echo "$PRG: $DATABASE_NAME.db not found! Move to a directory containing $DATABASE_NAME.db before executing this command!" 1>&2
    exit 1
  fi

  echo "$(date --rfc-3339=ns) Starting ZPersDbCompress1 ..."

  $JAVA_HOME/bin/java $JVM_OPTS -cp $APP_DIR/app/lib/\* -Djava.awt.headless=true -Dde.znt.util.PropertyHelper.fileName=$PROPERTY_FILE de.znt.zpers.client.ZPersDbCompress $DATABASE_NAME step1

  echo "$(date --rfc-3339=ns) Finished ZPersDbCompress1 !"
  ;;

zpers_import)
  if [ -f $DATABASE_NAME.db ] ; then
    echo "$PRG: $DATABASE_NAME.db already exists! Move it away before executing this command!" 1>&2
    exit 1
  fi

  echo "$(date --rfc-3339=ns) Starting ZPersDbCompress2 ..."

  $JAVA_HOME/bin/java $JVM_OPTS -cp $APP_DIR/app/lib/\* -Djava.awt.headless=true -Dde.znt.util.PropertyHelper.fileName=$PROPERTY_FILE de.znt.zpers.client.ZPersDbCompress $DATABASE_NAME step2

  echo "$(date --rfc-3339=ns) Finished ZPersDbCompress2 !"
  ;;

copy_config)
  destination_dir=$2
  if [ ! -d $destination_dir ] ; then
    echo "$PRG: Destination directory '$destination_dir' does not exist!" 1>&2
    exit 1
  fi

  echo "$(date --rfc-3339=ns) Copy config files to $destination_dir ..."

  cp $APP_DIR/etc/* $destination_dir

  echo "$(date --rfc-3339=ns) Finished copying config files to $destination_dir"
  ;;

deleteZPers)
  APPPID=$(get_pid)
  if [ -n "$APPPID" ] ; then
 	echo "$PRG: Application with id $APPPID is running, delete zpers files not allowed!"
  	exit 1
  else
    echo "$(date --rfc-3339=ns) $PRG: Application is not running, delete zpers files ..."
  	mv $APP_DIR/dat/db $APP_DIR/dat/db.bak
  fi
  ;;

*)
  print_usage_and_exit
  ;;
esac

exit 0
