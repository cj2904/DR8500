package EapCleanUpZeroMesLot

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConfig
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.material.WaferFilterAll
import sg.znt.pac.util.PacUtils
import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Clean up all lot when MES's equipment lot count is 0
''')
class EapCleanUpZeroMesLot_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument

	/**
	 *
	 */
	@DeoExecute
	public void execute() {
		def requestOutbound = new CommonOutboundRequest(inputXmlDocument)
		def eqpId = requestOutbound.getResourceName()
		def equipmentLotCount = PacUtils.valueOfInteger(requestOutbound.getEquipmentLotCount(), -1)
		logger.debug("Checking if clean up is require, equipmentLotCount = " + equipmentLotCount)
		if (equipmentLotCount == 0) {
			def existingLotList = cMaterialManager.getCLotList(new LotFilterAll())
			for (existingLot in existingLotList) {
				if (existingLot.getEquipmentId().equalsIgnoreCase(eqpId)) {
					if (TscConfig.getBooleanProperty("TrackInSubEquipment", false)) {
						def waferListIt = existingLot.getWaferList(new WaferFilterAll()).iterator()
						while (waferListIt.hasNext()) {
							def wafer = waferListIt.next()

							def equipmentId = wafer.getEquipmentId()
							if(equipmentId == null || equipmentId.length()==0 || equipmentId.equalsIgnoreCase(eqpId)) {
								logger.info("Remove wafer '" + wafer.getId() + "' from lot '" + existingLot.getId() + "' from equipment '" + eqpId + "''")
								existingLot.removeWafer(wafer)
							}
						}
						if(existingLot.getWaferCount()==0) {
							logger.info("Remove lot '" + existingLot.getId() + "' from equipment '" + eqpId + "''")
							cMaterialManager.removeCLot(existingLot)
						}
					}
					else {
						logger.info("Remove lot '" + existingLot.getId() + "' from equipment '" + eqpId + "''")
						cMaterialManager.removeCLot(existingLot)
					}
				}
			}
		}
	}
}