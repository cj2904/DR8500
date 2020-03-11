package EqpStartMachine_GUN

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.TscConfig
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EqpStartMachine_GUN_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())
	
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
	
    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService
	
    /**
     *
     */
    @DeoExecute
    public void execute()
    {

		def startCommand = TscConfig.getStringProperty("Secs.RemoteCommand.Start.Name", "START")
		
		logger.info("Sending $startCommand to equipment")

		def request =  new S2F41HostCommandSend(new SecsAsciiItem(startCommand))
		def lotId = getLotId()		
		def clot = cMaterialManager.getCLot(lotId)
		def recipe = setValue(clot.getRecipe())		
		def batchId = setValue(lotId)
		
		request.addParameter(new SecsAsciiItem("PPID") , new SecsAsciiItem(recipe))
		request.addParameter(new SecsAsciiItem("BatchID") , new SecsAsciiItem(batchId))
		
		def reply = secsGemService.sendS2F41HostCommandSend(request)
		logger.info("$startCommand command reply: " + reply.getHCAckMessage())
		
		if (!reply.isCommandAccepted())
		{
			throw new Exception("Fail to start equipment with error: " + reply.getHCAckMessage())
		}
    }
	
	private String setValue(String value)
	{
		def retvalue = ""
		if (value != null)
		{
			retvalue = value
		}
		return retvalue
	}
	
	private String getLotId()
	{
		def lotId = ""
		def request1 = new W02TrackInLotRequest(inputXmlDocument)
		def outboundLot = request1.getContainerName()

		def lotList = cMaterialManager.getCLotList(new LotFilterAll())
		if(lotList.size() > 0)
		{
			for(lot in lotList)
			{
				def lotId2 = lot.getId()
				if(lotId2.equalsIgnoreCase(outboundLot))
				{
					lotId = lotId2
				}
			}
		}
		return lotId
	}
}