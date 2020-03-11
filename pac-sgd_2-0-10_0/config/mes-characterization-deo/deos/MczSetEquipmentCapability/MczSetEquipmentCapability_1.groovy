package MczSetEquipmentCapability

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.camstar.semisuite.service.dto.SetEqpProcessCapabilityRequest
import sg.znt.camstar.semisuite.service.dto.SetEqpProcessCapabilityRequestDto.InputData.Details.DetailsItem.ProcessCapability;
import sg.znt.services.camstar.CCamstarService
import java.lang.String

@CompileStatic
@Deo(description='''
Set Equipment Capability
''')
class MczSetEquipmentCapability_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="EquipmentId")
    private String equipmentId

    @DeoBinding(id="CapabilityName")
    private String capabilityName

    @DeoBinding(id="ActivationStatus")
    private String activationStatus

    @DeoBinding(id="Availability")
    private String availability

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	def request = new SetEqpProcessCapabilityRequest()
		request.getInputData().setResource(equipmentId)
		def item = request.getInputData().getDetails().addDetailsItem()
		item.setActivationStatus(activationStatus)
		item.setAvailability(availability)
		item.getProcessCapability().setName(capabilityName)
		def reply = cCamstarService.setEqpProcessCapability(request)
		logger.info(reply.getResponseData().toXmlString())
    }
}