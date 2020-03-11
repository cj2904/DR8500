package EqpUploadRecipeToRms

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F5ProcessProgramRequest
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary
import groovy.transform.TypeChecked
import sg.znt.pac.SgdConfig
import sg.znt.services.rms.RmsService

@Deo(description='''
Upload recipe to RMS
''')
class EqpUploadRecipeToRms_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="RecipeId")
    private String recipeId
	
	@DeoBinding(id="NewFileName")
	private String newFileName

	@DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService
	
	@DeoBinding(id="RmsService")
	private RmsService rmsService
	
	@DeoBinding(id="IsSpecificFolder")
	private boolean isSpecificFolder
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
        def recipeName = recipeId
        def newFileNameWithSubfolder = newFileName
        def subfolderKey = SgdConfig.getStringProperty("File.SubFolder.Seperator", "\\Q\\\\E|/")
        recipeName = recipeName.replaceAll(subfolderKey, SgdConfig.getRecipeFolderSeperator())
        newFileNameWithSubfolder = newFileName.replaceAll(subfolderKey, SgdConfig.getRecipeFolderSeperator())
        
		S7F5ProcessProgramRequest request = new S7F5ProcessProgramRequest()
		request.setPPID(new SecsAsciiItem(recipeId))
		
		def reply = secsGemService.sendS7F5ProcessProgramRequest(request)
        def bodyReply = reply.getData().getPPBODY()
        byte[] ppbodyInByte = null
        if (bodyReply instanceof SecsBinary)
        {
            SecsBinary ppbody = (SecsBinary) reply.getData().getPPBODY()
            ppbodyInByte = ppbody.getBytes()
        }
        else if (bodyReply instanceof SecsAsciiItem)
        {
            SecsAsciiItem ppbody = (SecsAsciiItem) reply.getData().getPPBODY()
            ppbodyInByte = ppbody.getString().getBytes()
        }
		
		if(isSpecificFolder)
		{
			rmsService.getEquipmentService().saveRecipe(newFileNameWithSubfolder, ppbodyInByte)
		}
		else
		{
			rmsService.getEquipmentModelService().saveRecipe(newFileNameWithSubfolder, ppbodyInByte);
		}
		
		logger.info("Upload Recipe Successful")
    }
}