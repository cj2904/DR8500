package SputterEapSendLockDoorsCommand

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll

@CompileStatic
@Deo(description='''
Sputter specific function:<br/>
<b>pac to send LOCK_DOORS command to equipment</b>
''')
class SputterEapSendLockDoorsCommand_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def lotList = cMaterialManager.getCLotList(new LotFilterAll())
		if (lotList.size() > 0)
		{
			def message = "No lot found in pac Material Manager!"
			secsGemService.sendTerminalMessage((byte) 0, message)
			throw new Exception(message)
		}
		
        secsGemService = (SecsGemService) cEquipment.getExternalService()
    
        S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("LOCK_DOORS"))
        def reply = secsGemService.sendS2F41HostCommandSend(request)
        if (reply.isCommandAccepted())
        {
			logger.trace("Host command LOCK_DOORS accepted")
        }
        else
        {
            throw new Exception("Executing remote command LOCK_DOORS failed, reply message: " + reply.getHCAckMessage())
        }
    }
}