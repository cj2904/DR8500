package DprShowTerminalMessage

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import sg.znt.pac.domainobject.MachineDomainObjectManager
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
Dispatch to equipment
''')
class DprShowTerminalMessage_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	
	@DeoBinding(id="MachineDomainObjectManager")
	private MachineDomainObjectManager manager
	
	@DeoBinding(id="CEquipment")
	private CEquipment mainEquipment
	
	@DeoBinding(id="Parameters")
	private Map<String, Object> parameters
	/**
	 *
	 */
	@DeoExecute
	@TypeChecked
	public void execute()
	{
		def machineSet = manager.getMachineSet(mainEquipment.getName())
		def list = machineSet.getAllMachineDomainObject()
		for (mo in list) {
			def cEquipment = mo.getCEquipment()
			logger.info("Calling scenario: " + cEquipment.getModelScenario().getModelType())
			cEquipment.getModelScenario().eqpShowTerminalMessage(parameters)
		}
	}
}