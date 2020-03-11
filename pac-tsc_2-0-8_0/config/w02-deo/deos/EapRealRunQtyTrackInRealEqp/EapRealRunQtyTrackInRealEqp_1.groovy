package EapRealRunQtyTrackInRealEqp

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.camstar.semisuite.service.dto.TrackInWIPMainWafersRequest
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainResponseDto.ResponseData.WafersDetailsSelectionAll.WafersDetailsSelectionAllItem
import sg.znt.pac.exception.TrackInVirtualQtyException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''

''')
class EapRealRunQtyTrackInRealEqp_1
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

    @DeoBinding(id="Exception")
    private Throwable exception


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        if(exception instanceof TrackInVirtualQtyException)
        {
            def request = new W02CompleteOutLotRequest(inputXml)
            def lotId = request.getContainerName()
            def eqpId = request.getResourceName()
            int qty2 = Integer.parseInt(String.valueOf(request.getItemValue("Qty2")))
            int trackOutQty = Integer.parseInt(request.getTrackOutQty())
            int trackOutWaferQty = Integer.parseInt(request.getWaferQty())

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

            if (trackOutQty==0 && realRunQty>0)
            {
                def balQty = qty2 - realRunQty
                if(balQty>0)
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


                    // TRACK IN REAL QTY TO REAL EQP
                    def realTrackInRequest = new TrackInWIPMainWafersRequest(lotId, eqpId)
                    for(int j=0 ; j<realRunQty ; j++)
                    {
                        WafersDetailsSelectionAllItem wafer = waferlist.get(j)
                        def waferItem = realTrackInRequest.getInputData().getWafersDetails().addWafersDetailsItem()
                        waferItem.setWaferScribeNumber(wafer.getWaferScribeNumber())
                        waferItem.setContainer(lotId)
                    }
                    def realTrackInReply = cCamstarService.trackInWafers(realTrackInRequest)
                    if (!realTrackInReply.isSuccessful())
                    {
                        logger.error(realTrackInReply.getExceptionData().getErrorDescription())
                    }

                }
                else
                {
                    def wipMain = new GetLotWIPMainRequest(lotId)
                    def wipMainReply = cCamstarService.getLotWIPMain(wipMain)
                    if (wipMainReply.isSuccessful())
                    {

                        def trackInRequest = new TrackInWIPMainWafersRequest(lotId, eqpId)
                        def iterator = wipMainReply.getResponseData().getWafersDetailsSelectionAll().getWafersDetailsSelectionAllItems()
                        while (iterator.hasNext())
                        {
                            def wafer = iterator.next()
                            def waferItem = trackInRequest.getInputData().getWafersDetails().addWafersDetailsItem()
                            waferItem.setWaferScribeNumber(wafer.getWaferScribeNumber())
                            waferItem.setContainer(lotId)
                        }
                        def trackInReply = cCamstarService.trackInWafers(trackInRequest)
                        if (!trackInReply.isSuccessful())
                        {
                            logger.error(trackInReply.getExceptionData().getErrorDescription())
                        }

                    }
                    else
                    {
                        logger.error(wipMainReply.getExceptionData().getErrorDescription())
                    }
                }

            }
        }
    }


}