package EqpSendTerminalMessage

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import groovy.transform.TypeChecked
import sg.znt.pac.machine.CEquipment

@Deo(description='''
Send terminal message to equipment when validation fail
''')
class EqpSendTerminalMessage_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment equipment

	@DeoBinding(id="Message")
	private String message
    
    @DeoBinding(id="Type")
    private Byte type
    
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		SecsGemService secsService = (SecsGemService) equipment.getExternalService()
		secsService.sendTerminalMessage((byte)type, message)
    }
}