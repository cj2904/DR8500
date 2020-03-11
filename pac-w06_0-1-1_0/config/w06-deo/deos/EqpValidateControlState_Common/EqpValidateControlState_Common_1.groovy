package EqpValidateControlState_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Validate equipment control state before lot start</b>
''')
class EqpValidateControlState_Common_1 {


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
		cEquipment.updateControlState()
		if (!cEquipment.getControlState().isRemote())
		{
			throw new Exception("Equipment '" + cEquipment.getSystemId() + "' is not in remote state, current control state='" + cEquipment.getControlState().getStateName() + "'")
		}
    }
}