package EapQueryPortStatus_GRD

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.services.secs.dto.S2F13EquipmentConstantRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.EqpUtil

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapQueryPortStatus_GRD_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="SecsControl")
	private SecsControl secsControl

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="WipDataDomainObjectManager")
	private WipDataDomainObjectManager wipDataDomainObjectManager

	/**
	 *
	 */
	@DeoExecute
	public void execute() {

		def eqpId = cEquipment.getSystemId()
		def lotId = cEquipment.getCurrentLotId()
		def Port = W06Constants.MES_WIP_DATA_PORT_NAME
		def wipDataDomainObject = wipDataDomainObjectManager.getDomainObject(eqpId + "-" + lotId)
		def trackInWipDataItems = wipDataDomainObject.getElement(Port)
		def wipPortValue = trackInWipDataItems.getValue().trim()		

		def cLot = cMaterialManager.getCLot(lotId)
		cLot.getPropertyContainer().setString("PortSelected", wipPortValue.trim().toUpperCase())
		
		def newValue

		if (!wipPortValue.isEmpty() && !wipPortValue.equals("")){
			if (wipPortValue.contains(",")){
				newValue = wipPortValue.split(",")
			}
			else if (!wipPortValue.contains(",")){

				newValue = wipPortValue
			}

			for (def item in newValue) {
				def eqpPortState = getSecsValueFromEqp("SV@PSS_CST_" + item.trim())

				def portState = false
				def portVal = eqpPortState
				def eqp = PacConfig.getStringProperty("Equipment1.Name", "")

				def portStatus = PacConfig.getStringArrayProperty("Secs.PortState." + eqp + ".States.Ready2Start", "", ",")

				for (var in portStatus) {
					if (portVal.equalsIgnoreCase(var)) {
						logger.info("The Equipment Port '$item' for equipment '$eqpId' with status code '$portVal' is ready to start! ")
						portState = true
						//break
					}
				}

				if (!portState) {
					logger.info("current port status : " + portStatus)
					throw new Exception("The Equipment Port '$item' for equipment '$eqpId' with status code '$portVal' is not ready to start! ")
				}
			}
		}
	}

	public String getSecsValueFromEqp(String vidName) {
		def wipDataValue = null
		if (vidName.startsWith("SV@") || vidName.startsWith("DV@")) {
			def svid = secsControl.translateSvVid(vidName.replaceAll("SV@","").replaceAll("DV@",""))
			SecsComponent< ? > svidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svid)))
			def s1f3 = new S1F3SelectedEquipmentStatusRequest(svidItem)
			def replyS1F3 = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3)
			return EqpUtil.getVariableData(replyS1F3.getData().getSV(0))
		}
		else if (vidName.startsWith("EC@")) {
			def ecid = secsControl.translateEcVid(vidName.replaceAll("EC@",""))
			SecsComponent< ? > ecidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(ecid)))
			def s2f13 = new S2F13EquipmentConstantRequest(ecidItem)
			def replyS2F13 = secsGemService.sendS2F13EquipmentConstantRequest(s2f13)
			return EqpUtil.getVariableData(replyS2F13.getData().getECV(0))
		}
		else {
			//must start with SV@,DV@ or EC@
			throw new Exception("VID of $vidName is not defined with a valid VID format!")
		}
	}
}
