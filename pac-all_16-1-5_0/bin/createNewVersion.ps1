# Define the script parameters
Param(
  [Parameter(Mandatory=$True,Position=1)]
   [string]$targetDirectory
)

# Get directory containing this script
$SCRIPT_DIR = Split-Path -parent $MyInvocation.MyCommand.Definition

# Get application root directory relative to script directory
$APP_DIR = (get-item $SCRIPT_DIR).Parent.FullName

###########################################################################
# copy zam archive to target directory
###########################################################################
robocopy $APP_DIR\newAppVersionTemp\ $targetDirectory\ *.zar

exit 0