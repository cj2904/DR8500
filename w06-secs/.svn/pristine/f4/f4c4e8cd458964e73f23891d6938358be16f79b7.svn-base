package EqpValidateControlState_GUN

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
class EqpValidateControlState_GUN_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())
	
	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {		
		if (!cEquipment.getControlState().isRemote())
		{
			def eqpName = cEquipment.getSystemId()
			def eqpState = cEquipment.getControlState().getStateName()
			throw new Exception("Equipment '$eqpName' is not in remote state, current control state='$eqpState'")
		}
    }
}