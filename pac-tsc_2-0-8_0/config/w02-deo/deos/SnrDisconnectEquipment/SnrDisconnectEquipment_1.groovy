package SnrDisconnectEquipment

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.TscEquipment
import de.znt.pac.deo.annotations.*

@Deo(description='''
Scenario dispatcher
''')
class SnrDisconnectEquipment_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private TscEquipment equipment

    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
    	equipment.getModelScenario().eqpDisconnect()
    }
}