package DprRequestRejectData

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked;
import sg.znt.pac.domainobject.MachineDomainObjectManager
import sg.znt.pac.machine.CEquipment

@Deo(description='''
Dispatch to equipment
''')
class DprRequestRejectData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="MachineDomainObjectManager")
    private MachineDomainObjectManager manager

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
            cEquipment.getModelScenario().eqpRequestRejectData()
        }
    }
}