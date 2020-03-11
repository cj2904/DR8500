package EqpSendTerminalMessageOnError

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.util.error.ErrorManager;
import groovy.transform.TypeChecked
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.PacUtils;

@Deo(description='''
Send terminal message to equipment when validation fail
''')
class EqpSendTerminalMessageOnError_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment equipment

	@DeoBinding(id="Exception")
	private Throwable exception
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
        ErrorManager.handleError(exception, this)
        
		SecsGemService secsService = (SecsGemService) equipment.getExternalService()
		secsService.sendTerminalMessage((byte)2, PacUtils.getErrorMessage(exception))
    }
}