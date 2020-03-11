package EapArchiveLotInfoFileServiceByTiTime

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConfig
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.RequestWipDataRequest
import sg.znt.services.jcifs.ArchiveFilePath
import sg.znt.services.jcifs.ArchiveFileService
import sg.znt.services.jcifs.ArchiveUserAccount
import de.znt.pac.deo.annotations.*

@Deo(description='''
Archive machine file to shared folder
''')
class EapArchiveLotInfoFileServiceByTiTime_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="ArchiveFileService")
	private ArchiveFileService archiveFileService
	
	@DeoBinding(id="TscEquipment")
	private TscEquipment equipment
	
	@DeoBinding(id="ArchiveJobNameRegEx")
	private String archiveJobNameRegEx
    
    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
        RequestWipDataRequest outboundRequest = new RequestWipDataRequest(inputXmlDocument)
        def serviceName = outboundRequest.getWipDataServiceName()
        if (serviceName.equalsIgnoreCase("TrackOutLot"))
        {            
            def lotId = outboundRequest.getContainerName()
            def lot = cMaterialManager.getCLot(lotId)
            
            def machineUserAccount = getUserAccount("Local")
            def serverUserAccount = getUserAccount("ActiveDirectory")
            
            def filePathList = archiveFileService.getFilePathList()
            for (fp in filePathList) {
                if (fp.getArchiveJobName().matches(archiveJobNameRegEx))
                {
                    logger.info("Executing job file '"  +  fp.getArchiveJobName() + "'...")
                    archiveLotInfoFile(fp, machineUserAccount, serverUserAccount, lot)
                }
            }
        }
        else
        {
            logger.info("Do not execute for type '" + serviceName + "'")
        }
        
    }
	
    private String createTesterPath(String serverPath, CLot lot)
    {
        String tServerPath = serverPath
        if (!Character.toString(tServerPath.charAt(tServerPath.length()-1)).matches("[/|\\\\]"))
        {
            tServerPath = tServerPath + "/";
        }
        String lotPathKey = TscConfig.getArchiveLotInfoAttributeName(equipment.getName());
        String dataPathKey = TscConfig.getArchiveLotInfoRootFolderName(equipment.getName());
        String pathValue = lot.getPropertyContainer().getString(lotPathKey, "")
        if (pathValue.length()>0)
        {
            logger.info(dataPathKey + " already exists with value '" + pathValue)
            return pathValue
        }        
        return tServerPath + dataPathKey + "/" + getYear() + "/" + getMonth() + "/" + lot.getProductFamily() + "/" + lot.getId() + "/" 
    }
    
    private void archiveLotInfoFile(ArchiveFilePath filePathByJob, ArchiveUserAccount machineUserAccount, ArchiveUserAccount serverUserAccount, CLot lot)
    {
        if(filePathByJob != null)
        {
            def machinePath = filePathByJob.getMachinePath()
            def serverPath = filePathByJob.getServerPath()
            if(machinePath != null && machinePath.length() > 0 && serverPath != null && serverPath.length() > 0)
            {
                serverPath = createTesterPath(serverPath, lot)
                def destFilenamePrefix = TscConfig.getArchiveFilenamePrefix(equipment.getName()) + filePathByJob.getSystemId() + "_"
                def minuteOver = filePathByJob.getMinuteOverArchive()
				logger.info("Archive machine path $machinePath to $serverPath, minute over is $minuteOver")
                def result = archiveFileService.archiveMachineFileToServer(machinePath, machineUserAccount, serverPath, serverUserAccount, destFilenamePrefix, "", true, lot.getCreationTime(), 0)
                if (result)
                {
                    def dataPath = PacUtils.smbToUncPath(serverPath)
                    lot.setPropertyKey(TscConfig.getArchiveLotInfoAttributeName(equipment.getName()), dataPath)
                }
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
    
	private String getYear()
	{
		return PacUtils.getFormattedCurrentYear()
	}
	
	private String getMonth()
	{
		return PacUtils.getFormattedCurrentMonth()
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