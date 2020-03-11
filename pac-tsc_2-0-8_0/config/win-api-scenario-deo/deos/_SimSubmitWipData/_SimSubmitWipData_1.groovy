package _SimSubmitWipData


import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.services.zwin.TscZWinApiScenario
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher

@CompileStatic
@Deo(description='''
Simulate submit wip data from win api gateway
''')
class _SimSubmitWipData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="DeoEventDispatcher")
    private DeoEventDispatcher deoEventDispatcher

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="LotId")
    private String lotId
    
    @DeoBinding(id="WaferId")
    private String waferId
    
    @DeoBinding(id="ServiceType")
    private String serviceType
    
    @DeoBinding(id="WipDataValue")
    private String wipDataValue
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def runtimeParameters = new HashMap<String, Object>()
        def parameters = new HashMap<String, Object>()
        parameters.put("LotId", lotId)
        parameters.put("WaferId", waferId)
        parameters.put("ServiceType", serviceType)
        parameters.put("WipDataValue", wipDataValue)
        
        runtimeParameters.put("Equipment", cEquipment)
        runtimeParameters.put("SecsGemService", cEquipment.getExternalService())
        runtimeParameters.put("Parameter", parameters)
        runtimeParameters.put("EventName", TscZWinApiScenario.EVENT_SUBMIT_WIP_DATA)
        deoEventDispatcher.notifyEvent(TscZWinApiScenario.PROVIDER, TscZWinApiScenario.DEO_CATEGORY_EVENT, TscZWinApiScenario.EVENT_SUBMIT_WIP_DATA, runtimeParameters)
    }
}