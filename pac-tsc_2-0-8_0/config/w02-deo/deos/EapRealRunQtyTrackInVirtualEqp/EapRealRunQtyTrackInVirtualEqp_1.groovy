package EapRealRunQtyTrackInVirtualEqp

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.camstar.semisuite.service.dto.ModifyLotAttributesRequest
import sg.znt.camstar.semisuite.service.dto.TrackInWIPMainWafersRequest
import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainWafersRequest
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainResponseDto.ResponseData.WafersDetailsSelectionAll.WafersDetailsSelectionAllItem
import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainWafersRequestDto.InputData.Containers.ContainersItem
import sg.znt.pac.TscConstants
import sg.znt.pac.exception.TrackInVirtualQtyException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.CCamstarService.WIPFlag
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import de.znt.camstar.semisuite.service.dto.ViewContainerStatusRequest
import de.znt.camstar.semisuite.service.dto.ViewContainerStatusResponse
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''

''')
class EapRealRunQtyTrackInVirtualEqp_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment
    /**
     *
     */
    @DeoExecute
    public void execute()
    {

        def request = new W02CompleteOutLotRequest(inputXml)
        def employeeName =  request.getEmployeeName()
        def lotId = request.getContainerName()
        def eqpId = request.getResourceName()
        int qty2 = Integer.parseInt(String.valueOf(request.getItemValue("Qty2")))
        int trackOutQty = Integer.parseInt(request.getTrackOutQty())
        int trackOutWaferQty = Integer.parseInt(request.getWaferQty())
        def isCancelTrackIn = request.isCancelTrackIn()
        def dummyEqp = mainEquipment.getVirtualSystemId()

        logger.info("getEmployeeName " + employeeName)
        if(request.getEmployeeName().equalsIgnoreCase("PACADMIN")==false)
        {
            logger.info("NOT PACADMIN CANCEL TRACK IN. IGNORE CANCEL TRACKIN.")
            return;
        }

        int realRunQty = -1
        def wipDataList = request.getWipDataItemList()
        if (wipDataList.size()>0)
        {
            for (var in wipDataList)
            {
                if (var.WIP_DATA_NAME.contains("Real Run Qty"))
                {
                    realRunQty = PacUtils.valueOfInteger(var.WIP_DATA_VALUE, -1)
                }
            }
        }
        logger.info("realRunQty " + realRunQty)
        logger.info("trackOutQty " + trackOutQty)
        logger.info("isCancelTrackIn " + isCancelTrackIn)
        logger.info("trackOutWaferQty " + trackOutWaferQty)


        //Use cancel track in flag later.
        if (trackOutWaferQty ==0 && realRunQty>0)
        {
            def balQty = qty2 - realRunQty
            if(balQty>0)
            {
                logger.info("dummyEqp " + dummyEqp)
                if(dummyEqp.length()>0)
                {
                    List<WafersDetailsSelectionAllItem> waferlist = new ArrayList<WafersDetailsSelectionAllItem>()
                    def wipMain = new GetLotWIPMainRequest(lotId)
                    def wipMainReply = cCamstarService.getLotWIPMain(wipMain)
                    if (wipMainReply.isSuccessful())
                    {
                        def iterator = wipMainReply.getResponseData().getWafersDetailsSelectionAll().getWafersDetailsSelectionAllItems()
                        while (iterator.hasNext())
                        {
                            WafersDetailsSelectionAllItem wafer = iterator.next()
                            waferlist.add(wafer)
                        }
                    }
                    else
                    {
                        logger.error(wipMainReply.getExceptionData().getErrorDescription())
                    }

                    // TRACK IN BAL QTY TO VIRTUAL EQP
                    //1. change to FULLAUTO to prevent load recipe error
                    //2. service setup to prevent fire outboud..for all
                    //3.Need dummy wip data setup to this equipment not require to prevent track out error
                    def trackInRequest = new TrackInWIPMainWafersRequest(lotId, mainEquipment.getVirtualSystemId())
                    for(int i=realRunQty ; i<waferlist.size() ; i++)
                    {
                        WafersDetailsSelectionAllItem wafer = waferlist.get(i)
                        def waferItem = trackInRequest.getInputData().getWafersDetails().addWafersDetailsItem()
                        waferItem.setWaferScribeNumber(wafer.getWaferScribeNumber())
                        waferItem.setContainer(lotId)
                    }
                    def trackInReply = cCamstarService.trackInWafers(trackInRequest)
                    if (!trackInReply.isSuccessful())
                    {
                        logger.error(trackInReply.getExceptionData().getErrorDescription())
                    }

                    // TRACK OUT BAL QTY FROM VIRTUAL EQP
                    def request2 = new TrackOutWIPMainWafersRequest()
                    request2.getInputData().setEquipment(mainEquipment.getVirtualSystemId())
                    ContainersItem  con = request2.getInputData().getContainers().addContainersItem()
                    con.setName(lotId)
                    request2.getInputData().setProcessType("NORMAL")
                    WIPFlag f = WIPFlag.TRACKOUT
                    request2.getInputData().setWIPFlag(f.getValue())
                    request2.getInputData().setTrackOutQty(String.valueOf(balQty))
                    request2.getInputData().setRemainInEquipment("false")
                    request2.getInputData().setRemainInEquipmentIfPossible("false")

                    def trackInWafers = trackInRequest.getInputData().getWafersDetails().getWafersDetailsItems()
                    while(trackInWafers.hasNext())
                    {
                        def trackInWafer = trackInWafers.next()
                        def trackOutWaferItem = request2.getInputData().getWafersDetails().addWafersDetailsItem()
                        trackOutWaferItem.setWaferScribeNumber(trackInWafer.getWaferScribeNumber())
                        trackOutWaferItem.setContainer(lotId)
                    }
                    def reply = cCamstarService.trackOutWafers(request2)
                    if(reply.isSuccessful())
                    {
                        updateLotAttr(lotId)
                        logger.info(reply.getResponseData().getCompletionMsg())
                    }
                    else
                    {
                        CamstarMesUtil.handleNoChangeError(reply)
                    }
                }
                else
                {
                    logger.info("NO DUMMY EQP. IGNORE TRACKIN VIRTUAL EQP.")
                }
            }

            throw new TrackInVirtualQtyException()
        }
    }

    void updateLotAttr(String lotId)
    {
        String lotAttrVirtualRunSpec = getLotAttribute(lotId, TscConstants.LOT_MES_ATTR_VIRTUAL_RUN_SPEC_AFTER)
        int ilotAttrVirtualRun = PacUtils.valueOfInteger(lotAttrVirtualRunSpec, 0) + 1

        def attributePairValues = new HashMap<String, String>()
        attributePairValues.put(TscConstants.LOT_MES_ATTR_VIRTUAL_RUN_SPEC_AFTER, ilotAttrVirtualRun.toString())
        def attributeRequest = new ModifyLotAttributesRequest(false, lotId, attributePairValues)
        def reply = cCamstarService.setLotAttributes(attributeRequest)
        if (reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }

    String getLotAttribute(String lotId, String attrName)
    {
        String result = ""
        ViewContainerStatusRequest request = new ViewContainerStatusRequest(lotId)
        request.getRequestData().getLotAttributes().initChildParameter(attrName, false)

        ViewContainerStatusResponse reply = new ViewContainerStatusResponse()
        reply.getResponseData().getLotAttributes().initChildParameter(attrName,false)
        reply =  cCamstarService.viewContainerStatus(request)
        if(reply.isSuccessful())
        {
            result = reply.getResponseData().getLotAttributes().getChildParameter(attrName).getValue()
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
        return result
    }

}