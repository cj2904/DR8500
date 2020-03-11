package LjCvdStart

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsU1Item

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class LjCvdStart_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
      def request = new S2F41HostCommandSend()
	  def start= new SecsAsciiItem("START")
	  request.getData().setRemoteCommand(start)
	  def portName=new SecsAsciiItem("PortID")
	  def portValue=new SecsU1Item((Short)1)
	  request.addParameter(portName, portValue)
	  def reply = secsGemService.sendS2F41HostCommandSend(request) 
	  if(!reply.isCommandAccepted())
	  {
		 throw new Exception("Start fail!!") 
		 logger.error("Start fail!!") 
	  }
	  else
	  {
		  logger.info("Start success!!")
	  }
    }
}