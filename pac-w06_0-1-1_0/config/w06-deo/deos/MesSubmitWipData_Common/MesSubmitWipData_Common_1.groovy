package MesSubmitWipData_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainResponse
import sg.znt.camstar.semisuite.service.dto.SetWIPDataRequest
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Summit WIP data to Camstar MES</b>
''')
class MesSubmitWipData_Common_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="TimeToWaitInMilliSec")
    private int timeToWaitInMilliSec = 1000

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        Thread.sleep(timeToWaitInMilliSec)
        def outbound = new W02CompleteOutLotRequest(inputXml)
        def eqpId = outbound.getResourceName()
        def lotList = outbound.getLotList()
        for (lotId in lotList)
        {
            CLot cLot = null
            try
            {
                cLot = cMaterialManager.getCLot(lotId)
            }
            catch (ItemNotFoundException e)
            {
                logger.info("Lot '$lotId' is not found in pac Material Manager!")
                continue
            }

            String serviceType = getServiceType(cLot, eqpId)
            if (serviceType.length() > 0)
            {
                submitWipData(cLot, eqpId, serviceType)
            }
            else
            {
                logger.info("Service Type for WIP Data not matched")
            }
        }
    }

    private String getServiceType(CLot cLot, String eqpId)
    {
        def lotId = cLot.getId()
        GetLotWIPMainRequest lotWipMainRequest = new GetLotWIPMainRequest(lotId)
        GetLotWIPMainResponse lotWipMainReply = cCamstarService.getLotWIPMain(lotWipMainRequest)
        if (lotWipMainReply.isSuccessful())
        {
            def wipFlag = lotWipMainReply.getResponseData().getWIPFlagSelection()
            cLot.setWIPFlagSelection(wipFlag)
            if (cLot.getWipFlagValue(wipFlag).equals("tracked in"))
            {
                /*
                 * service name for WIP data submission is Track Out
                 */
                def eqList = lotWipMainReply.getResponseData().getEquipmentSelection().getEquipmentSelectionItems()
                boolean exist = false
                while(eqList.hasNext())
                {
                    def eq = eqList.next()
                    if(eq.getName().equals(eqpId))
                    {
                        exist = true
                        break
                    }
                }
                if(!exist)
                {
                    throw new ValidationFailureException(lotId, "Lot '$lotId is yet to track in to equipment'")
                }
                return WipDataDomainObject.SERVICE_TYPE_TRACK_OUT_WIP_DATA;
            }
            else if (cLot.getWipFlagValue(wipFlag).equals("tracked out"))
            {
                /*
                 * service name for WIP data submission is Move Out
                 */
                return WipDataDomainObject.SERVICE_TYPE_MOVE_OUT_WIP_DATA
            }
        }
        return "";
    }

    private void submitWipData(CLot cLot, String eqpId, String serviceType)
    {
        def lotId = cLot.getId()
        def wipDataSet = wipDataDomainObjectManager.getWipDataSet(eqpId + "-" + lotId)
        List <WipDataDomainObject> wdDomainObjList = null
        if (serviceType.equalsIgnoreCase(WipDataDomainObject.SERVICE_TYPE_TRACK_OUT_WIP_DATA))
        {
            wdDomainObjList = wipDataSet.getTrackOutWipDataItems()
        }
        else if (serviceType.equalsIgnoreCase(WipDataDomainObject.SERVICE_TYPE_MOVE_OUT_WIP_DATA))
        {
            wdDomainObjList = wipDataSet.getMoveOutWipDataItems()
        }
        if (wdDomainObjList.size() > 0)
        {
            cLot.getPropertyContainer().setString("WipDataMessage", "")
            SetWIPDataRequest request = new SetWIPDataRequest()
            request.getInputData().setContainer(lotId)
            request.getInputData().setEquipment(eqpId)
            request.getInputData().setServiceName(serviceType)
            request.getInputData().setProcessType("NORMAL")

            boolean wipDataReadySubmit = false
            for (wdDO in wdDomainObjList)
            {
                if (wdDO.isHidden())
                {
                    wipDataReadySubmit = true
                    def detailItem = request.getInputData().getDetails().addDetailsItem()
                    detailItem.setWIPDataName(wdDO.getId())
                    detailItem.setWIPDataValue(wdDO.getValue())
                }
            }

            if (wipDataReadySubmit)
            {
                def reply = cCamstarService.setWIPData(request)
                if(reply.isSuccessful())
                {
                    cLot.getPropertyContainer().setString("WipDataMessage", reply.getResponseData().getCompletionMsg())
                }
                else
                {
                    throw new Exception(reply.getExceptionData().getErrorDescription())
                }
            }
        }
        else
        {
            logger.info("WIP data list is empty, skip submit WIP Data to Camstar MES...")
        }
    }
}