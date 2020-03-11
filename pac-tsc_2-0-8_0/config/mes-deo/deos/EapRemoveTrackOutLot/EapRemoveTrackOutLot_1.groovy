package EapRemoveTrackOutLot

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.EquipmentWIPQueryRequest
import sg.znt.camstar.semisuite.service.dto.EquipmentWIPQueryResponse
import sg.znt.pac.TscConfig
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*

@Deo(description='''
Check if previous lot has successfully track out
''')
class EapRemoveTrackOutLot_1 {


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService

	@DeoBinding(id="TscEquipment")
	private CEquipment equipment

	private String newLotId
	/**
	 *
	 */
	@DeoExecute
	@TypeChecked
	public void execute() {
		TscEquipment cEquipment = (TscEquipment) equipment
		def outboundRequest = new W02CompleteOutLotRequest(inputXmlDocument)

		newLotId = outboundRequest.getContainerName()

		if (TscConfig.getBooleanProperty("EquipmentWip.CleanUp", false)) {
			def existingLotList = cMaterialManager.getCLotList(new LotFilterAll())
			for (existingLot in existingLotList) {
				if(newLotId.equals(existingLot.getId())) {
					cMaterialManager.removeCLot(existingLot)
				}
				else {
					EquipmentWIPQueryRequest request = new EquipmentWIPQueryRequest(cEquipment.getSystemId())
					EquipmentWIPQueryResponse reply = cCamstarService.equipmentWIPQuery(request)
					if(reply.isSuccessful()) {
						def lotIdList = reply.getLotIdList()
						def exist = false
						for (lotId in lotIdList) {
							if(lotId.equalsIgnoreCase(existingLot.getId())) {
								exist = true
								break
							}
						}

						if(!exist) {
							cMaterialManager.removeCLot(existingLot)
						}
					}
					else {
						throw new ValidationFailureException(existingLot.getId(), reply.getExceptionData().getErrorDescription())
					}
				}
			}
		}
		else{
			if (TscConfig.getBooleanProperty("TrackInSubEquipment", false) == false) {
				def lotList = outboundRequest.getLotList()
				if (lotList.size() == 0) {
					try {
						def lot = cMaterialManager.getCLot(newLotId)
						cMaterialManager.removeCLot(lot)
					}
					catch (ItemNotFoundException ex) {
						logger.info("Lot '$newLotId' already removed!")
					}
					catch (Exception e) {
						e.printStackTrace()
					}
				}
				else {
					for (lotId in lotList) {
						try {
							def lot = cMaterialManager.getCLot(lotId)
							cMaterialManager.removeCLot(lot)
						}
						catch (ItemNotFoundException ex) {
							logger.info("Lot '$lotId' already removed!")
						}
						catch (Exception e) {
							e.printStackTrace()
						}
					}
				}
			}
			else {

				def existingLotList = cMaterialManager.getCLotList(new LotFilterAll())
				def lotSize = existingLotList.size()
				def eqpLotSize = PacUtils.valueOfInteger(outboundRequest.getEquipmentLotCount(), 0)
				if(eqpLotSize == 1 && lotSize == 1) {
					logger.info("Removing the track out lot '" + newLotId + "' since it's single track out!")
					for (existingLot in existingLotList) {
						if(newLotId.equals(existingLot.getId())) {
							cMaterialManager.removeCLot(existingLot)
						}
					}
				}
			}
			cEquipment.setLastTrackOutLotId(newLotId)
		}
	}
}