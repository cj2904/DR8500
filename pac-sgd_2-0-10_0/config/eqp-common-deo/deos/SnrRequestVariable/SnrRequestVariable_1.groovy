package SnrRequestVariable

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import sg.znt.pac.machine.CEquipment
import groovy.transform.TypeChecked

@Deo(description='''
Scenario dispatcher
''')
class SnrRequestVariable_1 {


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
		List<String> listOfVariable = new ArrayList<String>()
		listOfVariable.add("TestParam1")
		listOfVariable.add("TestParam2")
		
		equipment.getModelScenario().eqpRequestVariableByList(listOfVariable)
    }
}