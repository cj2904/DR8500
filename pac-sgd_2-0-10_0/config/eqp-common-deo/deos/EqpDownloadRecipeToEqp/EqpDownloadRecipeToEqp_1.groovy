package EqpDownloadRecipeToEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.error.DeoExecutionException
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F1ProcessProgramLoadInquire
import de.znt.services.secs.dto.S7F3ProcessProgramSend
import de.znt.services.secs.dto.S7F1ProcessProgramLoadInquireDto.Data
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsNumberItem
import de.znt.zsecs.composite.SecsU4Item
import de.znt.zsecs.composite.SecsDataItem.ItemName
import de.znt.zsecs.sml.SecsItemType
import groovy.transform.TypeChecked

@Deo(description='''
Download recipe to equipment
''')
class EqpDownloadRecipeToEqp_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="RecipeId")
	private String recipeId
	
	@DeoBinding(id="RecipeBody")
	private byte[] recipeBody

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		if(recipeBody != null)
		{
			S7F1ProcessProgramLoadInquire loadRequest = new S7F1ProcessProgramLoadInquire()
			SecsAsciiItem secsPpId = new SecsAsciiItem(recipeId)
			loadRequest.getData().setPPID(secsPpId)
			def defaultDataType = SecsDataItem.getDataType(ItemName.VID, SecsItemType.U4)
			def dataType = SecsDataItem.getDataType("S7F1", defaultDataType)
			SecsNumberItem dataItem = (SecsNumberItem) SecsDataItem.createDataItem(dataType, recipeBody.length + "")
			loadRequest.getData().setLENGTH(dataItem)

			def loadReply = secsGemService.sendS7F1ProcessProgramLoadInquire(loadRequest)

			if (loadReply.getPPGNT() != 0)
			{
				throw new DeoExecutionException("Failed to send S7F1")
			}
			
			S7F3ProcessProgramSend request = new S7F3ProcessProgramSend()
			request.getData().setPPID(new SecsAsciiItem(recipeId))
			request.getData().setPPBODY(new SecsAsciiItem(new String(recipeBody)))
			
			def reply = secsGemService.sendS7F3ProcessProgramSend(request)
			
			if(reply.isAccepted())
			{
				logger.info("Download Recipe Successful")
			}
			else
			{
				throw new DeoExecutionException("Download Recipe Fail")
			}
		}
    }
}