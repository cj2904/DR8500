package DprCompleteLot

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked;
import sg.znt.pac.domainobject.MachineDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager;
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.zwin.ZWinApiException

@Deo(description='''
Dispatch to equipment
''')
class DprCompleteLot_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment mainEquipment
	
	@DeoBinding(id="MachineDomainObjectManager")
	private MachineDomainObjectManager manager
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
    
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
        def lotList = materialManager.getCLotList(new LotFilterAll())
        if(lotList.size() == 0)
        {
            def exception = new ZWinApiException("-4", "Lot not found")
            throw exception
        }
        def lot = lotList.get(0)
        
        def machineSet = manager.getMachineSet(mainEquipment.getName())
        def list = machineSet.getAllMachineDomainObject()
        for (mo in list) {
            def cEquipment = mo.getCEquipment()
            logger.info("Calling scenario: " + cEquipment.getModelScenario().getModelType())
            def hm = new HashMap()
            hm.put("LotId", lot.getId())
            cEquipment.getModelScenario().eqpCompleteLot(hm)
        }
    }
}