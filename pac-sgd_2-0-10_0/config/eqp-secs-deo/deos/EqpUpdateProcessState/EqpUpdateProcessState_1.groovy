package EqpUpdateProcessState

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.EqpUtil
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent

@CompileStatic
@Deo(description='''
Update the process state from equipment
''')
class EqpUpdateProcessState_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

    @DeoBinding(id="CEquipment")
    private CEquipment equipment
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def processState = equipment.getProcessState();
        def secsU4State = EqpUtil.getIntReportValue(secsEvent, processState.getStatusVariableName())
        processState.setState(secsU4State);
        logger.info("[" + equipment.getName() + "] updateProcessState: $secsU4State, " + processState.getStateName())
    }
}