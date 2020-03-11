package EapDispatchCamstarScenario

import groovy.transform.CompileStatic

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import OutboundRequest.CommonOutboundRequest
import sg.znt.pac.EquipmentIdentifyService
import sg.znt.pac.TscConfig;
import sg.znt.pac.machine.CEquipment;
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher
import de.znt.services.camstar.subject.CamstarQueueSubject

@CompileStatic
@Deo(description='''
Dispatch Camstar Scenario
''')
class EapDispatchCamstarScenario_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

	@DeoBinding(id="EquipmentIdentifier")
	private EquipmentIdentifyService equipmentIdentifier

	@DeoBinding(id="DeoEventDispatcher")
	private DeoEventDispatcher deoEventDispatcher

    @DeoBinding(id="InputXml")
    private String inputXml

	@DeoBinding(id="TriggerName")
	private String triggerName

    /**
     *
     */
	@DeoExecute(result="ResultXmlDocument")
    public String execute()
    {
		Map<String, Object> params = new HashMap<String, Object>()
		params.put(CamstarQueueSubject.BINDING_CAMSTAR_SERVICE, cCamstarService)
		params.put(CamstarQueueSubject.BINDING_INPUT_XML_DOC, inputXml)
		def request = new CommonOutboundRequest(inputXml)
		def eqId = request.getInputData().getOutboundRequestInfo().getResourceName()
		def equipment = equipmentIdentifier.getEquipmentBySystemId(eqId)
		if (equipment==null)
		{
			//Handle modbus equipment equipment id not direct register in pac
			equipment = equipmentIdentifier.getEquipmentBySystemId(TscConfig.getPacServiceName())
		}
		params.put(CEquipment.class.getSimpleName(), equipment)
    	return deoEventDispatcher.notifyEvent(CCamstarService.CAMSTAR_SCENARIO, CamstarQueueSubject.CATEGORY_NAME, triggerName, params)
    }
}