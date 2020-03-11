package WinApiSubmitWIPData_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.vaadin.ui.Notification
import com.vaadin.ui.Notification.Type

import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.domainobject.WinApiWipData
import sg.znt.pac.domainobject.WinApiWipDataManager
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.CCamstarService

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Submit WIP data to Camstar MES</b>
''')
class WinApiSubmitWIPData_Common_1
{


	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="LotId")
	private String lotId

	@DeoBinding(id="EqpId")
	private String eqpId

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="WinApiWipDataManager")
	private WinApiWipDataManager cWinApiWipDataManager

	private static String SERVICE_TYPE_TRACK_IN_LOT = "TrackInLot"
	private static String SERVICE_TYPE_TRACK_OUT_LOT = "TrackOutLot"
	private static String SERVICE_TYPE_MOVE_OUT_LOT = "LotMoveOut"

	/**
	 *
	 */
	@DeoExecute
	public void execute()
	{
		CLot cLot = null
		try
		{
			cLot = cMaterialManager.getCLot(lotId)
		}
		catch (ItemNotFoundException e)
		{
			def errMsg = "No Lot found in Material Manager, skip submit WIP data to Camstar MES!"
			Notification.show("WinApiSubmitWIPData_Common", errMsg, Type.ERROR_MESSAGE)
			throw new Exception(errMsg)
		}

		def serviceType = cLot.getMesLotStatus()
		switch(serviceType)
		{
			case SERVICE_TYPE_TRACK_IN_LOT:
			case SERVICE_TYPE_TRACK_OUT_LOT:
			case SERVICE_TYPE_MOVE_OUT_LOT:
				submitWipData(cLot, eqpId, serviceType)
				break
			default:
				def errMsg = ("WIP Data Service Type '$serviceType' for lot ID '$lotId' is invalid!")
				Notification.show("WinApiSubmitWIPData_Common", errMsg, Type.ERROR_MESSAGE)
				throw new Exception(errMsg)
		}
	}

	private void submitWipData(CLot cLot, String eqpId, String serviceType)
	{
		def lotId = cLot.getId()
		def batchId = cLot.getBatchId()
		if (batchId == null)
		{
			def errMsg = ("Batch ID for lot ID '$lotId' is empty!")
			Notification.show("WinApiSubmitWIPData_Common", errMsg, Type.ERROR_MESSAGE)
			throw new Exception(errMsg)
		}

		List<WinApiWipData> wdDomainObjList = cWinApiWipDataManager.getAllDomainObject()
		if (wdDomainObjList.size() > 0)
		{
			List<CLot> cLotList = cMaterialManager.getCLotList(new LotFilterAll())
			def completionMsg = ""
			def errorMessage = ""
			for (curCLot in cLotList)
			{
				if (curCLot.getBatchId() == batchId)
				{
					curCLot.getPropertyContainer().setString("WipDataMessage", "")
					SetWIPDataRequest request = new SetWIPDataRequest()
					request.getInputData().setContainer(curCLot.getId())
					request.getInputData().setEquipment(eqpId)
					request.getInputData().setServiceName(serviceType)
					request.getInputData().setProcessType("NORMAL")

					boolean wipDataReadySubmit = false
					for (wdDO in wdDomainObjList)
					{
						if (wdDO.getWipDataServiceName().equalsIgnoreCase(serviceType))
						{
							wipDataReadySubmit = true
							def detailItem = request.getInputData().getDetails().addDetailsItem()
							detailItem.setWIPDataName(wdDO.getWipDataItemName())
							detailItem.setWIPDataValue(wdDO.getWipDataValue())
						}
					}

					if (wipDataReadySubmit)
					{
						def reply = cCamstarService.setWIPData(request)
						if(reply.isSuccessful())
						{
							def msg = reply.getResponseData().getCompletionMsg()
							completionMsg += msg + "\n"
							curCLot.getPropertyContainer().setString("WipDataMessage", msg)
						}
						else
						{
							def errMsg = ("Set WIP Data for lot ID '$lotId' failed with error message:" + reply.getExceptionData().getErrorDescription() + "'")
							errorMessage += errMsg + "\n"
						}
					}
				}
			}
			if (errorMessage.length()>0)
			{
				Notification.show("WinApiSubmitWIPData_Common", errorMessage, Type.ERROR_MESSAGE)
				throw new Exception(errorMessage)
			}
			else
			{
				Notification.show("WinApiSubmitWIPData_Common", completionMsg, Type.HUMANIZED_MESSAGE)
			}
		}
		else
		{
			logger.info("WIP data list is empty, skip submit WIP Data to Camstar MES...")
		}
	}
}