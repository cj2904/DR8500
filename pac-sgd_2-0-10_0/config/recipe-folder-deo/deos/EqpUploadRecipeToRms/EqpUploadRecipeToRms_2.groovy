package EqpUploadRecipeToRms

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F5ProcessProgramRequest
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary
import groovy.transform.TypeChecked
import sg.znt.services.rms.RmsService

@Deo(description='''
Upload recipe to RMS
''')
class EqpUploadRecipeToRms_2 {


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
		S7F5ProcessProgramRequest request = new S7F5ProcessProgramRequest()
		request.setPPID(new SecsAsciiItem(recipeId))
		
		def reply = secsGemService.sendS7F5ProcessProgramRequest(request)
		SecsBinary ppbody = (SecsBinary) reply.getData().getPPBODY()
		
		if(isSpecificFolder)
		{
			rmsService.getEquipmentService().saveRecipe(newFileName, ppbody.getBytes())
		}
		else
		{
			rmsService.getEquipmentModelService().saveRecipe(newFileName, ppbody.getBytes());
		}
		
		logger.info("Upload Recipe Successful")
    }
}