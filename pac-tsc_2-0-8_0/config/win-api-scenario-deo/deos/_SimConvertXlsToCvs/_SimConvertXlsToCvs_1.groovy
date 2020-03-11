package _SimConvertXlsToCvs

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.services.zwin.TscZWinApiScenario
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher

@CompileStatic
@Deo(description='''
Simulate ConvertXlsToCvs
''')
class _SimConvertXlsToCvs_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    
    @DeoBinding(id="DeoEventDispatcher")
    private DeoEventDispatcher deoEventDispatcher
    
    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="SourceFilePath")
    private String sourceFilePath
    
    @DeoBinding(id="DestFilePath")
    private String destFilePath
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def runtimeParameters = new HashMap<String, Object>()
        def parameters = new HashMap<String, Object>()
        parameters.put("SourceFilePath", sourceFilePath)
        parameters.put("DestFilePath", destFilePath)
        
        runtimeParameters.put("Equipment", cEquipment)
        runtimeParameters.put("SecsGemService", cEquipment.getExternalService())        
        runtimeParameters.put("Parameter", parameters)
        runtimeParameters.put("EventId", "1")
        runtimeParameters.put("EventName", TscZWinApiScenario.EVENT_CONVERT_XLS_TO_CVS)
        deoEventDispatcher.notifyEvent(TscZWinApiScenario.PROVIDER, TscZWinApiScenario.DEO_CATEGORY_EVENT, TscZWinApiScenario.EVENT_CONVERT_XLS_TO_CVS, runtimeParameters)
}
}