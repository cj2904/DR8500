# Define the script parameters
Param(
  [Parameter(Mandatory=$True,Position=1)]
   [string]$targetDirectory
)

# Get directory containing this script
$SCRIPT_DIR = Split-Path -parent $MyInvocation.MyCommand.Definition

# Get application root directory relative to script directory
$APP_DIR = (get-item $SCRIPT_DIR).Parent.FullName

Write-Host "JAVA_HOME="$env:JAVA_HOME

###########################################################################
# Create Thread dump
###########################################################################
$processes = Get-WmiObject Win32_Process -Filter "Name like '%java%'" | select name, CommandLine, ProcessID 
foreach ($process in $processes)
{
    $commandline = $process.CommandLine
    $processId = $process.ProcessID
    $jstackExecutable = $env:JAVA_HOME+"/bin/jstack"
    if ($commandline)
    {
        if ($commandline.Contains("$APP_DIR"))
        {
            if ($commandline.Contains("de.znt.pac.ProcessAutomationController"))
            {
                echo "ProcessID: $processId" > $targetDirectory\StackTrace.txt
                echo "Command: $commandline" >> $targetDirectory\StackTrace.txt
                echo "###########################################################################" >> $targetDirectory\StackTrace.txt                
                & $jstackExecutable $processId 2>&1 >> $targetDirectory\StackTrace.txt
            }
        }
    }
}

###########################################################################
# list all available files
###########################################################################
Get-ChildItem -Path $APP_DIR -Recurse > $targetDirectory\Files.txt

###########################################################################
# copy log entries: take only pac and secs log files from the last 3 days
###########################################################################
robocopy $APP_DIR\log $targetDirectory\log /s /maxage:3
robocopy $APP_DIR\log $targetDirectory\log /s /xf paclog* zsecs*
robocopy $APP_DIR\dat $targetDirectory\dat /s /xd db*
robocopy $APP_DIR\dumps $targetDirectory\dumps

###########################################################################
# copy all configuration files
###########################################################################
robocopy $APP_DIR\config $targetDirectory\config /s

echo "###########################################################################" > $targetDirectory\SystemInfo.txt
echo "# Physical Memory:" >> $targetDirectory\SystemInfo.txt
Get-WmiObject -query "Select * from CIM_PhysicalMemory" >> $targetDirectory\SystemInfo.txt

echo "###########################################################################" >> $targetDirectory\SystemInfo.txt
echo "# Disk:" >> $targetDirectory\SystemInfo.txt
Get-WmiObject Win32_logicaldisk -ComputerName LocalHost | Format-Table DeviceID, MediaType, @{Name="Size(GB)";Expression={[decimal]("{0:N0}" -f($_.size/1gb))}}, @{Name="Free Space(GB)";Expression={[decimal]("{0:N0}" -f($_.freespace/1gb))}}, @{Name="Free (%)";Expression={"{0,6:P0}" -f(($_.freespace/1gb) / ($_.size/1gb))}} -AutoSize >> $targetDirectory\SystemInfo.txt

echo "###########################################################################" >> $targetDirectory\SystemInfo.txt
echo "# Network:" >> $targetDirectory\SystemInfo.txt
Get-WmiObject Win32_NetworkAdapterConfiguration | Format-Table IPAddress, DHCPEnabled, DefaultIPGateway, Description -autosize >> $targetDirectory\SystemInfo.txt

echo "###########################################################################" >> $targetDirectory\SystemInfo.txt
echo "# Processes:" >> $targetDirectory\SystemInfo.txt
Get-Process >> $targetDirectory\SystemInfo.txt

exit 0