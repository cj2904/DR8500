package EqpUpdateControlState

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.EqpUtil
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent

@CompileStatic
@Deo(description='''
Update control state from event
''')
class EqpUpdateControlState_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {        
        def controlState = cEquipment.getControlState()
        def secsU4State = EqpUtil.getIntReportValue(secsEvent, controlState.getStatusVariableName())
        controlState.setState(secsU4State);
        logger.info("[" + cEquipment.getName() + "] updateControlState: $secsU4State, " + controlState.getStateName())
    
    }
}