package EqpValidateProcessState_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Validate equipment process state before lot start</b>
''')
class EqpValidateProcessState_Common_1 {


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
		cEquipment.updateProcessState()
    	if (!cEquipment.getProcessState().isReady2Start())
    	{
			throw new Exception("Equipment '" + cEquipment.getSystemId() + "' is not at a ready to start state, current process state='" + cEquipment.getProcessState().getStateName() + "'")
		}
    }
}