package EqpSetProcessStateRunning_GUN

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EqpSetProcessStateRunning_GUN_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())
	
	@DeoBinding(id="Equipment")
	private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		cEquipment.getProcessState().setState(3)
		logger.info("S6F11 :: Set CEquipment process state to 'Running'")
    }
}