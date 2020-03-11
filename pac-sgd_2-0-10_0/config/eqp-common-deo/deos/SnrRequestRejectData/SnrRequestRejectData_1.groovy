package SnrRequestRejectData

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment
import groovy.transform.TypeChecked

@Deo(description='''
Scenario dispatcher
''')
class SnrRequestRejectData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment equipment

    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
    	equipment.getModelScenario().eqpRequestRejectData()
    }
}