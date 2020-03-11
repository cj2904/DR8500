package SnrCheckControlState

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
Scenario dispatcher
''')
class SnrCheckControlState_1 {


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
    	cEquipment.getModelScenario().eqpRequestControlState()
    }
}