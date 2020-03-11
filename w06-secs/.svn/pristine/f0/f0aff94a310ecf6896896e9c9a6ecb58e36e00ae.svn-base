package EqpSetControlStateRemote_GUN

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
class EqpSetControlStateRemote_GUN_1 {


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
		cEquipment.getControlState().setRemote()
		logger.info("S6F11 :: Set CEquipment control state to 'Remote'")
    }
}