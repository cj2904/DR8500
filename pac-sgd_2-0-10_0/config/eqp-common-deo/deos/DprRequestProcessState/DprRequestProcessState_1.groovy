package DprRequestProcessState

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked;
import sg.znt.pac.domainobject.MachineDomainObjectManager
import sg.znt.pac.machine.CEquipment

@Deo(description='''
Dispatch to equipment
''')
class DprRequestProcessState_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="MachineDomainObjectManager")
	private MachineDomainObjectManager manager
	
	@DeoBinding(id="CEquipment")
    private CEquipment mainEquipment

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
            def externalService = cEquipment.getExternalService()
            if (externalService != null)
            {
                cEquipment.getModelScenario().eqpRequestProcessState()
            }
            else
            {
                logger.info("Skip requets process state: " + cEquipment.getName())                
            }
        }
    }
}