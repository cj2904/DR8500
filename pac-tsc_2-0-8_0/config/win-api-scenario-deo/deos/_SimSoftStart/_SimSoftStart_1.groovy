package _SimSoftStart

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.services.zwin.TscZWinApiScenario
import sg.znt.services.zwin.ZWinApiScenario
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher

@Deo(description='''
Simulate soft start event
''')
class _SimSoftStart_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="DeoEventDispatcher")
    private DeoEventDispatcher deoEventDispatcher

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def runtimeParameters = new HashMap<String, Object>()
        def parameters = new HashMap<String, Object>()
        runtimeParameters.put("Equipment", cEquipment)
        runtimeParameters.put("SecsGemService", cEquipment.getExternalService())        
        runtimeParameters.put("Parameter", parameters)
        runtimeParameters.put("EventName", ZWinApiScenario.EVENT_SOFT_START)
        deoEventDispatcher.notifyEvent(TscZWinApiScenario.PROVIDER, TscZWinApiScenario.DEO_CATEGORY_EVENT, ZWinApiScenario.EVENT_SOFT_START, runtimeParameters)
    }
}