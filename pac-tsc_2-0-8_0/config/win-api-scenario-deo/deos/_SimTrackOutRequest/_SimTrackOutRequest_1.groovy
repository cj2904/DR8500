package _SimTrackOutRequest


import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.EquipmentIdentifyService
import sg.znt.pac.machine.CEquipment
import sg.znt.services.zwin.TscZWinApiScenario
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher

@CompileStatic
@Deo(description='''
Simulate track in request event from win api gateway
''')
class _SimTrackOutRequest_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="DeoEventDispatcher")
    private DeoEventDispatcher deoEventDispatcher

    @DeoBinding(id="EquipmentId")
    private String equipmentId

    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="WaferId")
    private String waferId
    
    @DeoBinding(id="EquipmentIdentifyService")
    private EquipmentIdentifyService equipmentIdentifyService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        CEquipment cEquipment = equipmentIdentifyService.getEquipmentBySystemId(equipmentId)
        
        def runtimeParameters = new HashMap<String, Object>()
        def parameters = new HashMap<String, Object>()
        parameters.put("LotId", lotId)
        parameters.put("WaferId", waferId)
        
        runtimeParameters.put("Equipment", cEquipment)
        runtimeParameters.put("SecsGemService", cEquipment.getExternalService())
        runtimeParameters.put("Parameter", parameters)
        runtimeParameters.put("EventName", TscZWinApiScenario.EVENT_TRACK_OUT)
        deoEventDispatcher.notifyEvent(TscZWinApiScenario.PROVIDER, TscZWinApiScenario.DEO_CATEGORY_EVENT, TscZWinApiScenario.EVENT_TRACK_OUT, runtimeParameters)
    }
}