package LiJePCVDPPSelect

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*
import java.lang.String
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsU1Item

@CompileStatic
@Deo(description='''
LiJe PCVD PP-Select
''')
class LiJePCVDPPSelect_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="ImpXml")
    private String impXml

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	def request= new S2F41HostCommandSend()
		def commandItem=new SecsAsciiItem("PP-SELECT")
		
		def mesxml=new W02TrackInLotRequest(impXml)
		def lotid=mesxml.getContainerName()
		def recipename=mesxml.getRecipeName()
		
		def paramlist=mesxml.getRecipeParamList()
		def paramvalue
		for (param in paramlist)
		{
			if (param.getParamName().equals("EqpRecipe"))
			{
				paramvalue=param.getParamValue()
			}
		}
		request.getData().setRemoteCommand(commandItem)
		def param1=new SecsAsciiItem("PPID")
		//def paramValue=new SecsAsciiItem(ppId)
		request.addParameter(param1, new SecsAsciiItem(paramvalue))
		def param2=new SecsAsciiItem("BATCHID")
		request.addParameter(param2,  new SecsAsciiItem(lotid))
		def param3=new SecsAsciiItem("PORTID")
		request.addParameter(param3, new SecsU1Item((short)1))
		def reply=secsGemService.sendS2F41HostCommandSend(request)
		if (reply.isCommandAccepted())
		{
			logger.info("PP-Select Success")
		}
		else
		{
			throw new Exception(reply.getHCAckMessage())
		}
    }
}