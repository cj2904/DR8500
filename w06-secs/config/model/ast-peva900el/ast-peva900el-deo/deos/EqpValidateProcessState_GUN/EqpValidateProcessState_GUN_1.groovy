package EqpValidateProcessState_GUN

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
class EqpValidateProcessState_GUN_1 {


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
		if (!cEquipment.getProcessState().isReady2Start())
		{
			def eqpName = cEquipment.getSystemId()
			def eqpState = cEquipment.getProcessState().getStateName()
			throw new Exception("Equipment '$eqpName' is not at a ready to start state, current process state='$eqpState'")
		}
    }
}