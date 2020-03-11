package W02ModbusAddAcidEventAddResourceComment

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetResourceCommentRequest
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.modbus.W02ModBusService;
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
modbus add acid and message
''')
class W02ModbusAddAcidEventAddResourceComment_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService

	@DeoBinding(id="ModbusEvent")
	private ModBusEvent modbusEvent
	
	@DeoBinding(id="W02ModBusService")
	private W02ModBusService w02ModBusService
	
	@DeoBinding(id="ControlStatusAddress")
	private int controlStatusAddress = 6048

	/**
	 *
	 */
	@DeoExecute
	public void execute() {
		def status = w02ModBusService.readHoldingRegisterIntValue(controlStatusAddress)
		def eqLogicalId = PacConfig.getStringProperty(modbusEvent.getChamber() + ".SystemId", "")
		if(eqLogicalId.length() > 0) {
			def eventTime = CDateFormat.getFormatedDate(new Date())
			def request = new SetResourceCommentRequest(eqLogicalId, "[$status] Event 'Add Acid' at equipment $eqLogicalId @ $eventTime")
			def reply = cCamstarService.setResourceComment(request)
			if(reply.isSuccessful()) {
				logger.info(reply.getResponseData().getCompletionMsg())
			}
			else {
				CamstarMesUtil.handleNoChangeError(reply)
			}
		}
		else{
			throw new Exception("Child equipment logicalId is not define for " + modbusEvent.getChamber())
		}
	}
}