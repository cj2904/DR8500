package EqpDownloadFormattedRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.error.DeoExecutionException
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S7F1ProcessProgramLoadInquire
import de.znt.services.secs.dto.S7F23FormattedProcessProgramSend;
import de.znt.services.secs.dto.S7F23FormattedProcessProgramSendDto.Data
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsComponent;
import de.znt.zsecs.composite.SecsDataItem;
import de.znt.zsecs.composite.SecsDataItem.ItemName;
import de.znt.zsecs.sml.SecsItemType
import groovy.transform.TypeChecked;

import java.lang.String

@Deo(description='''
Download formatted recipe parameter to equipment
''')
class EqpDownloadFormattedRecipe_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="RecipeId")
    private String recipeId
	
	@DeoBinding(id="RecipeBody")
	private byte[] recipeBody;
	
	@DeoBinding(id="FormattedParameter")
	private Map<String, String> formattedParameter

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		S7F1ProcessProgramLoadInquire loadRequest = new S7F1ProcessProgramLoadInquire()
		SecsAsciiItem secsPpId = new SecsAsciiItem(recipeId)
		loadRequest.getData().setPPID(secsPpId)
		def dataType = SecsDataItem.getDataType("S7F1", SecsDataItem.getDataType(ItemName.VID, SecsItemType.U4))
		//loadRequest.getData().setLENGTH(SecsDataItem.createDataItem(dataType, recipeBody.length))

		def loadReply = secsGemService.sendS7F1ProcessProgramLoadInquire(loadRequest)
		
		if (loadReply.getPPGNT() != 0)
		{
			throw new DeoExecutionException("Failed to send S7F1")
		}
		
		Data data = new Data()
		data.setPpId(new SecsAsciiItem(recipeId))
//		data.setMdln("")
//		data.setSoftRev("")
		
		for (entry in formattedParameter.entrySet()) 
		{
			def param = data.getProcessCommands().addProcessCommand()
			param.setCCode(new SecsAsciiItem(entry.getKey()))
			param.getParameters().addPParm(new SecsAsciiItem(entry.getValue()))
		}
		
		S7F23FormattedProcessProgramSend request = new S7F23FormattedProcessProgramSend(data)
		def response = secsGemService.sendS7F23FormattedProcessProgramSend(request)
		
		SecsComponent<?> ackc7Component = response.getAckC7Component()
		if (ackc7Component == null || ackc7Component.getSize() == 0)
		{
			throw new DeoExecutionException("Download Formatted Recipe Fail")
		}
		else
		{
			logger.info("Download Formatted Recipe Successful")
		}
    }
}