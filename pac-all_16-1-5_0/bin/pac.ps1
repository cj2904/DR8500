# Check if -action parameter is given
param([string]$action=$(throw "Parameter missing: -action <start | stop | kill | status | backup | restore | deleteZPers>"))
# IMPORTANT: Do not add any additional parameters (See PACALL-164)

# Check if -action parameter has valid value
$possible_actions = "start", "stop", "kill", "status", "backup", "restore", "deleteZPers"
if (-Not ($possible_actions -contains $action))
{
    throw "Specify either start, stop, kill, status, backup, restore or deleteZPers for argument -action."
}

# Get directory containing this script
$SCRIPT_DIR = Split-Path -parent $MyInvocation.MyCommand.Definition

# Get application root directory relative to script directory
$APP_DIR = (get-item $SCRIPT_DIR).Parent.FullName

# Define restore and backup directory
$RESTORE_DIR = $APP_DIR + "\restore"
$BACKUP_DIR = $APP_DIR + "\backup"

# Application type (used in log messages of this script)
$APP_TYPE = "pac"

# Main class of managed application
$MAIN_CLASS = "de.znt.pac.ProcessAutomationController"

# Set application specific classpath
$CP =
$CP = "$CP;$APP_DIR\config\i18n"
$CP = "$CP;$APP_DIR\ext\*"
$CP = "$CP;$APP_DIR\esa\*"
$CP = "$CP;$APP_DIR\lib\*"

# Application specific properties in Instance.properties
$heapSizePropertyName = "JVM.MaxHeapSize"
$socketServerPortPropertyName = "SocketServer.Port"

# Default values of instance properties
$maxHeapSize = "256M"
$socket_server_port

# Paths configuration of hprof files
$DUMP_DIR = $APP_DIR + "\dumps"
$DUMP_DIR_TMP = $DUMP_DIR + "\temp"
$DUMP_DIR_ARCHIVE = $DUMP_DIR + "\archive"

$DLL_DIR = $APP_DIR + "\lib"

function Get-TimeStamp {

    return "$(Get-Date -Format o)"

}

# Application type specific start function
function doStartApp
{
    cd $APP_DIR

    echo "$(Get-TimeStamp) Start JVM with max. heap size '$heapSizePropertyName=$maxHeapSize'"

    $env:Path += ";"+$DLL_DIR

    Write-Host "$(Get-TimeStamp) JAVA_HOME="$env:JAVA_HOME

    # Compress and delete original hprof file
    compressHProfFiles
    cleanupHProfArchiveFiles
    cleanupHProfTempFolder
    # Create folder for gc logging in case that it does not exist
    testAndCreateFolder $APP_DIR\log

    $startinfo = New-Object "System.Diagnostics.ProcessStartInfo"
    $startinfo.FileName = $env:JAVA_HOME+"/bin/java"
    $startinfo.Arguments = "-cp $CP -Dde.znt.util.PropertyHelper.fileName=$APP_DIR\config\properties\application.properties -Duser.home=$APP_DIR -DAppDir=$APP_DIR -Djapps.home=$APP_DIR -Dapp.home=$APP_DIR -Djava.library.path=$DLL_DIR -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=$DUMP_DIR -Xloggc:$APP_DIR\log\jvm-gc.log -Xmx$maxHeapSize $MAIN_CLASS"
    $startinfo.UseShellExecute = $false
    $startinfo.RedirectStandardOutput = $false
    $startinfo.RedirectStandardError = $false

    $process = New-Object "System.Diagnostics.Process"
    $process.StartInfo = $startinfo
    $process.start() | Out-Null
    return $process
}

# Restore backup
function doRestore
{
    if ((Test-Path -Path $RESTORE_DIR))
    {
        Write-Host "$(Get-TimeStamp) Restoring from $RESTORE_DIR"
        Robocopy $RESTORE_DIR $APP_DIR /MOVE /E /IS
        Write-Host "$(Get-TimeStamp) Restoring finished!"
    }
    else
    {
        Write-Host "$(Get-TimeStamp) Nothing to restore!"
    }
}

# Application type specific stop function
function doStopApp
{
    cd $APP_DIR

    Write-Host "$(Get-TimeStamp) JAVA_HOME="$env:JAVA_HOME
    echo "$(Get-TimeStamp) Stop application at port '$socketServerPortPropertyName=$socket_server_port'"

    # Shutdown application via ShutdownRequestSender
    $startinfo = New-Object "System.Diagnostics.ProcessStartInfo"
    $startinfo.FileName = $env:JAVA_HOME+"/bin/java"
    $startinfo.Arguments = "-cp $CP de.znt.remote.shutdown.ShutdownRequestSender -port $socket_server_port -timeout 10"
    $startinfo.UseShellExecute = $false
    $startinfo.RedirectStandardOutput = $false
    $startinfo.RedirectStandardError = $false

    $process = New-Object "System.Diagnostics.Process"
    $process.StartInfo = $startinfo
    $process.start() | Out-Null
}

# Application type specific function to get process id of running application
function getProcessID
{
    $APP_FILTER = $APP_DIR.Replace('\', '\\')
    $processes = Get-WmiObject Win32_Process -Filter "Name like '%java%' and CommandLine like '%$APP_FILTER%' and CommandLine like '%$MAIN_CLASS%'" | select ProcessID

    foreach ($process in $processes)
    {
        return $process.ProcessID
    }
    return $null
}

# Start application if it is not already running
# Exit value 0 means that application is already running or has been started
# Exit value 1 means that application could not be started
function startApp
{
    Write-Host "$(Get-TimeStamp) Start $APP_TYPE if not running"

    $processID = getProcessID
    if ($processID)
    {
        Write-Host "$(Get-TimeStamp) $APP_TYPE is already running"
        exit 0
    }
    else
    {
        Write-Host "$(Get-TimeStamp) $APP_TYPE is not running"
    }

    doRestore

    Write-Host "$(Get-TimeStamp) Try to start $APP_TYPE"

    $process = doStartApp

    Start-Sleep -s 5

    if ($process.HasExited)
    {
        Write-Host "$(Get-TimeStamp) $APP_TYPE start failed with exit code $process.ExitCode"
        exit 1
    }
    Write-Host "$(Get-TimeStamp) Started $APP_TYPE"
}

# Stop application if it is running
# Exit value 0 means that application has been stopped
# Exit value 1 means that application was not running
function stopApp
{
    Write-Host "$(Get-TimeStamp) Stop $APP_TYPE if running"

    $processID = getProcessID
    if ($processID)
    {
        Write-Host "$(Get-TimeStamp) Try to stop $APP_TYPE"
        doStopApp

        while ($true)
        {
            $processID = getProcessID
            if ($processID)
            {
                Write-Host "."
                Start-Sleep -s 1
            }
            else
            {
                Write-Host "$(Get-TimeStamp) Stopped $APP_TYPE"
                break
            }
        }
    }
    else
    {
        Write-Host "$(Get-TimeStamp) $APP_TYPE is not running"
        exit 1
    }
}

# Kill application if it is running
# Exit value 0 means that application has been killed
# Exit value 1 means that application was not running
function killApp
{
    Write-Host "$(Get-TimeStamp) Kill $APP_TYPE if running"

    $processID = getProcessID
    if ($processID)
    {
        Stop-Process -Force -Id $processID
        Write-Host "$(Get-TimeStamp) Killed $APP_TYPE"
    }
    else
    {
        Write-Host "$(Get-TimeStamp) $APP_TYPE is not running"
        exit 1
    }
}

# Get running state of application
# Exit value 0 means that application is running
# Exit value 1 means that application is not running
function getStatus
{
    Write-Host "$(Get-TimeStamp) Check if $APP_TYPE is running"

    $processID = getProcessID
    if ($processID)
    {
        Write-Host "$(Get-TimeStamp) $APP_TYPE is running with ProcessID $processID"
    }
    else
    {
        Write-Host "$(Get-TimeStamp) $APP_TYPE is not running"
        exit 1
    }
}

# Delete zpers cached files
function deleteZPers
{
    $processID = getProcessID
    if ($processID)
    {
        Write-Host "$(Get-TimeStamp) $APP_TYPE is running, delete zpers files not allowed!"
        exit 1
    }
    else
    {
        Write-Host "$(Get-TimeStamp) $APP_TYPE is not running, delete zpers files ..."
        Robocopy $APP_DIR\dat\db $APP_DIR\dat\db.bak /MOVE /E /IS
    }
}

# Creates a backup
function createBackup
{
    # verify if backup directory already exist
    if (Test-Path -Path $BACKUP_DIR)
    {
        echo "$(Get-TimeStamp) ERROR: Backup directory '$BACKUP_DIR' already exists"
        exit -1
    }

    # verify if backup tmp directory already exist
    $TMP_BACKUP_DIR = $BACKUP_DIR + ".tmp"
    if (Test-Path -Path $TMP_BACKUP_DIR)
    {
        echo "$(Get-TimeStamp) Backup temporary directory '$TMP_BACKUP_DIR' already exists - delete it"
        Remove-Item $TMP_BACKUP_DIR -Recurse
    }

    echo "$(Get-TimeStamp) Create temporary backup directory '$TMP_BACKUP_DIR'"
    mkdir $TMP_BACKUP_DIR

    $processID = getProcessID
    if ($processID)
    {
        echo "$(Get-TimeStamp) Copy \dat without zpers to '$TMP_BACKUP_DIR'"
        robocopy $APP_DIR\dat $TMP_BACKUP_DIR\dat /s /XD $APP_DIR\dat\db $APP_DIR\dat\deoClasses > output.log
    }
    else
    {
        echo "$(Get-TimeStamp) Copy \dat with zpers to '$TMP_BACKUP_DIR'"
        robocopy $APP_DIR\dat $TMP_BACKUP_DIR\dat /s /XD $APP_DIR\dat\deoClasses > output.log
    }
    del output.log

    echo "$(Get-TimeStamp) Move '$TMP_BACKUP_DIR' to '$BACKUP_DIR'"
    mv $TMP_BACKUP_DIR $BACKUP_DIR
    echo "$(Get-TimeStamp) Backup finished!"
    exit 0
}

# Initialize properties
function initializeInstanceProperties
{
    $InstancePath = $APP_DIR + "\INSTANCE.properties"

    # Read property file content
    $FileExists = Test-Path $InstancePath
    if ($FileExists -eq $True)
    {
        $PropertyFileContent = Get-Content $InstancePath
        $PropertyFileContent = $PropertyFileContent -join [Environment]::NewLine
        $instance_properties = ConvertFrom-StringData($PropertyFileContent)
        # Property max heap size
        $maxHeapProperty = $instance_properties.$heapSizePropertyName
        # Socket server port
        Set-Variable -Name socket_server_port -Value $instance_properties.$socketServerPortPropertyName -Scope 1
        echo "$(Get-TimeStamp) Use configured property '$socketServerPortPropertyName = $socket_server_port'"
        if ($maxHeapProperty.length -gt 0)
        {
            Set-Variable -Name maxHeapSize -Value $maxHeapProperty -Scope 1
            echo "$(Get-TimeStamp) Use configured property '$heapSizePropertyName = $maxHeapSize'"
        }
    }
}

function compressHProfFiles
{
    Write-Host "$(Get-TimeStamp) Compress HProf files ..."

    testAndCreateFolder $DUMP_DIR
    testAndCreateFolder $DUMP_DIR_TMP
    testAndCreateFolder $DUMP_DIR_ARCHIVE

    $fileCount = getFileCount $DUMP_DIR

    if( $fileCount -gt 0)
    {
        # Move hprof files
        robocopy $DUMP_DIR $DUMP_DIR_TMP /MOVE > dumps.log
        # Create ZIP file with current hprof file content
        $jarExecutable = $env:JAVA_HOME+"/bin/jar"
        & $jarExecutable  -cvf $DUMP_DIR_ARCHIVE\hprof_$(get-date -f yyyyMMdd_HHmmss).zip $DUMP_DIR_TMP > dumps.log
        del dumps.log
    }
    else
    {
        Write-Host "$(Get-TimeStamp) No HProf files found"
    }
}

# Keep only on archive file in folder. Delete others.
function cleanupHProfArchiveFiles
{
    Write-Host "$(Get-TimeStamp) Cleanup HProf archive files ..."

    testAndCreateFolder $DUMP_DIR_ARCHIVE

    $fileCount = (Get-ChildItem $DUMP_DIR_ARCHIVE *.zip).Count

    if ($fileCount -gt 1)
    {
        $fileList = Get-ChildItem "$DUMP_DIR_ARCHIVE" *.zip | Sort-Object -property @{Expression={$_.CreationTime}; Ascending=$true} | Select-Object -first ($fileCount - 1)
        foreach ($item in $fileList)
        {
            Remove-Item $item.fullname
        }
    }
    else
    {
        Write-Host "$(Get-TimeStamp) No HProf archive files found"
    }
}

# Delete all files in temporary folder
function cleanupHProfTempFolder
{
    Write-Host "$(Get-TimeStamp) Cleanup HProf temp files ..."

    $fileCount = getFileCount $DUMP_DIR_TMP

    if( $fileCount -gt 0)
    {
        # Delete all files in folder $DUMP_DIR_TMP
        Remove-Item $DUMP_DIR_TMP\*
    }
    else
    {
        Write-Host "$(Get-TimeStamp) No HProf archive files found"
    }
}

# Test if folder exists. Create it, if not existing.
function testAndCreateFolder
{
    param ($directory)

    if ((Test-Path -Path $directory) -eq $false)
    {
        Write-Host "$(Get-TimeStamp) Create folder $directory"
        mkdir -Path $directory
    }
}

function getFileCount
{
    param ($folder)

    $fileCount = 0
    $fileList = Get-ChildItem $folder
    foreach ($item in $fileList)
    {
        # if the item is NOT a directory, then process it.
        if ($item.Attributes -ne "Directory")
        {
            $fileCount = $fileCount + 1
        }
    }
    return $fileCount
}

initializeInstanceProperties

if($action -eq "start")
{
    startApp
}
elseif($action -eq "stop")
{
    stopApp
}
elseif($action -eq "kill")
{
    killApp
}
elseif($action -eq "status")
{
    getStatus
}
elseif($action -eq "backup")
{
    createBackup
}
elseif($action -eq "restore")
{
    doRestore
}
elseif($action -eq "deleteZPers")
{
    deleteZPers
}
exit 0