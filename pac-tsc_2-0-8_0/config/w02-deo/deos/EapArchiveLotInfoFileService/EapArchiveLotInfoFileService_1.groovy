package EapArchiveLotInfoFileService

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.TscConfig
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.TrackInLotRequest
import sg.znt.services.jcifs.ArchiveFilePath
import sg.znt.services.jcifs.ArchiveFileService
import sg.znt.services.jcifs.ArchiveUserAccount

@Deo(description='''
Archive machine file to shared folder
''')
class EapArchiveLotInfoFileService_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="ArchiveFileService")
	private ArchiveFileService archiveFileService
	
	@DeoBinding(id="MainEquipment")
	private TscEquipment mainEquipment
	
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
        TrackInLotRequest outboundRequest = new TrackInLotRequest(inputXmlDocument)
        def lotId = outboundRequest.getContainerName()
        def lot = cMaterialManager.getCLot(lotId)
        
        def machineUserAccount = getUserAccount("Local")
        def serverUserAccount = getUserAccount("ActiveDirectory")
        
        def filePathList = archiveFileService.getFilePathList()
        for (fp in filePathList) {
            if (fp.getArchiveJobName().matches(archiveJobNameRegEx))
            {
                logger.debug("Executing job file '"  +  fp.getArchiveJobName() + "'...")
                archiveLotInfoFile(fp, machineUserAccount, serverUserAccount, lot)
            }
        }
        
    }
	
    private String createTesterPath(String serverPath, CLot lot)
    {
        String tServerPath = serverPath
        if (!Character.toString(tServerPath.charAt(tServerPath.length()-1)).matches("[/|\\\\]"))
        {
            tServerPath = tServerPath + "/";
        }
        String lotPathKey = TscConfig.getArchiveLotInfoAttributeName(mainEquipment.getName());
        String dataPathKey = TscConfig.getArchiveLotInfoRootFolderName(mainEquipment.getName());
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
                def destFilenamePrefix = TscConfig.getArchiveFilenamePrefix(mainEquipment.getName()) + filePathByJob.getSystemId() + "_"

                def minuteOver = filePathByJob.getMinuteOverArchive()
                        
                def result = archiveFileService.archiveMachineFileToServer(machinePath, machineUserAccount, serverPath, serverUserAccount, destFilenamePrefix, "", true, minuteOver, filePathByJob.getLotPattern().getRegularExp(lot))
                if (result)
                {
                    def testerDataPath = PacUtils.smbToUncPath(serverPath)
                    lot.setPropertyKey(TscConfig.getArchiveLotInfoAttributeName(mainEquipment.getName()), testerDataPath)
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