package SnrCompleteLot

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager;
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.zwin.ZWinApiException

@Deo(description='''
Scenario dispatcher
''')
class SnrCompleteLot_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment equipment

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
        
        def hm = new HashMap()
        hm.put("LotId", lot.getId())
        
    	equipment.getModelScenario().eqpCompleteLot(hm)
    }
}