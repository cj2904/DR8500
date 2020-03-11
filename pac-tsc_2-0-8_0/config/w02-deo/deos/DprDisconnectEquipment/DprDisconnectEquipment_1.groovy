package DprDisconnectEquipment

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.MachineDomainObjectManager
import sg.znt.pac.machine.TscEquipment
import de.znt.pac.deo.annotations.*

@Deo(description='''
Dispatch to equipment
''')
class DprDisconnectEquipment_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="MachineDomainObjectManager")
    private MachineDomainObjectManager manager

    @DeoBinding(id="TscEquipment")
    private TscEquipment mainEquipment

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
            TscEquipment cEquipment = (TscEquipment) mo.getCEquipment()
            logger.info("Calling scenario: " + cEquipment.getModelScenario().getModelType())
            cEquipment.getModelScenario().eqpDisconnect()
        }
    }
}