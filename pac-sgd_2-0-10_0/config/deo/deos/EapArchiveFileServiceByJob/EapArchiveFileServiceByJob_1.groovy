package EapArchiveFileServiceByJob

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.SgdConfig
import sg.znt.pac.machine.CEquipment
import sg.znt.services.jcifs.ArchiveFileService
import sg.znt.services.jcifs.ArchiveUserAccount

@Deo(description='''
Archive machine file to shared folder
''')
class EapArchiveFileServiceByJob_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="ArchiveFileService")
	private ArchiveFileService archiveFileService
	
	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment
	
	@DeoBinding(id="ArchiveJobName")
	private String archiveJobName
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		def machineUserAccount = getUserAccount("Local")
		def serverUserAccount = getUserAccount("ActiveDirectory")
		def filePathByJob = archiveFileService.getFilePathByArchiveJobName(archiveJobName)
		if(filePathByJob != null)
		{
			def machinePath = filePathByJob.getMachinePath()
			def serverPath = filePathByJob.getServerPath()
			if(machinePath != null && machinePath.length() > 0 && serverPath != null && serverPath.length() > 0)
			{
				serverPath = serverPath + "/@FILE_LAST_MODIFIED_YEAR@/" + cEquipment.getEquipmentFamily() + "/" + cEquipment.getSystemId() + "/@FILE_LAST_MODIFIED_MONTH@/"
				def destFilenamePrefix = SgdConfig.getArchiveFilenamePrefix(cEquipment.getName());
				def minuteOver = filePathByJob.getMinuteOverArchive()
				logger.info("Server path,minute over: $serverPath, $minuteOver")		
				archiveFileService.archiveMachineFileToServer(machinePath, machineUserAccount, serverPath, serverUserAccount, destFilenamePrefix, "", true, minuteOver)
			}
			else
			{
				if(machinePath == null || machinePath.length() == 0)
				{
					logger.info("Machine path for archive file service is not configured, skip archive")
				}
				
				if(serverPath == null || serverPath.length() == 0)
				{
					logger.info("Server path for archive file service is not configured, skip archive")
				}
			}
		}
    }
	
	private ArchiveUserAccount getUserAccount(String accountType)
	{
		def userAccounts = archiveFileService.getUserAccount()
		for (acc in userAccounts) 
		{
			if(acc.getAccountType().equals(accountType))
			{
				return acc
			}
		}
		
		return null
	}
}