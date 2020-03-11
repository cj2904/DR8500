package W02ModbusAlarmAddResourceComment

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetResourceCommentRequest
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
modbus alarm event message and comment
''')
class W02ModbusAlarmAddResourceComment_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="ModbusEvent")
	private ModBusEvent modbusEvent
    
    @DeoBinding(id="ModbusAlarmText")
    private String modbusAlarmText
    
    @DeoBinding(id="ModbusAlarmId")
    private String modbusAlarmId

	/**
	 *
	 */
	@DeoExecute
	public void execute() {
		def eqLogicalId = PacConfig.getStringProperty(modbusEvent.getChamber() + ".SystemId", "")
		def lotList = cMaterialManager.getCLotList(new LotFilterAll())
		if(eqLogicalId.length() > 0) {
			if(lotList.size() > 0) {
				def eventTime = CDateFormat.getFormatedDate(new Date())
				def request = new SetResourceCommentRequest(eqLogicalId, "Alarm ID '$modbusAlarmId' with text '$modbusAlarmText' was trigger at equipment $eqLogicalId @ $eventTime")
				def reply = cCamstarService.setResourceComment(request)
				if(reply.isSuccessful()) {
					logger.info(reply.getResponseData().getCompletionMsg())
				}
				else {
					CamstarMesUtil.handleNoChangeError(reply)
				}
			}
		}
		else {
			throw new Exception("Child equipment logicalId is not define for " + modbusEvent.getChamber())
		}
	}
}