package EapCheckThruput

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintResponse
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.camstar.semisuite.service.dto.LotModifyWafersRequest
import sg.znt.camstar.semisuite.service.dto.TrackInWIPMainWafersRequest
import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainWafersRequest
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainResponseDto.ResponseData.WafersDetailsSelectionAll.WafersDetailsSelectionAllItem
import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainWafersRequestDto.InputData.Containers.ContainersItem
import sg.znt.pac.domainobject.PmManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.CCamstarService.WIPFlag
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest
import de.znt.camstar.semisuite.service.dto.MoveInRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Increase thruput if require
''')
class EapCheckThruput_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="CEquipment")
    private CEquipment equipment

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="PmManager")
    PmManager pmManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {

        //The real track in is  track in to main equipment
        //the dummy track in is track in to child equipment if thruput factor found
        //all dummy lot id and pm requirement name is get from equipment field, EqpReserve1

        //what we need to prepare
        //a dummy spec recusive
        //a dummy waferflow
        //a dummy process spec
        //lot assign to this process spec

        W02CompleteOutLotRequest outbound = new W02CompleteOutLotRequest(inputXml)
        def lotId = outbound.getContainerName()
        def dummyLotId = outbound.getDummyLotId()
        def waferCount = Integer.parseInt(outbound.getWaferQty())
        def lot = cMaterialManager.getCLot(lotId)

        def childEqp = ""
        def dummyWaferCountRequired = 0


        if (outbound.isCancelTrackIn())
        {
            logger.info("Is cancel track in do not perform EapCheckThruput.")
            return
        }

        def allRecipe = lot.getAllRecipeObj()
        for (recipe in allRecipe)
        {
            childEqp = recipe.getEquipmentLogicalId()
            def recipeThruputFactor = recipe.getThruputFactor()
            if (recipeThruputFactor>1)
            {
                //DETERMINE WAFER REQUIRED
                dummyWaferCountRequired = waferCount * (recipeThruputFactor-1)

                def req = new GetEquipmentMaintRequest()
                req.getInputData().getObjectToChange().setName(childEqp)
                req.getRequestData().getObjectChanges().initChildParameter("tscEqpReserved2")
                GetEquipmentMaintResponse res = new GetEquipmentMaintResponse()
                res.getResponseData().getObjectChanges().initChildParameter("tscEqpReserved2")
                res = cCamstarService.getEquipmentMaint(req)
                dummyLotId = res.getResponseData().getObjectChanges().getChildParameter("tscEqpReserved2").getValue()
                logger.info("GetEquipmentMaintRequest tscEqpReserved2 " + dummyLotId )

                logger.info("lotId " + lotId)
                logger.info("childEqp " + childEqp)
                logger.info("dummyLotId " + dummyLotId)
                logger.info("waferCount " + waferCount)
                logger.info("recipeThruputFactor " + recipeThruputFactor)
                logger.info("dummyWaferCountRequired " + dummyWaferCountRequired)


                def moveinok = false
                def wipMainRequest = new GetLotWIPMainRequest(dummyLotId)
                def wipMainReply = cCamstarService.getLotWIPMain(wipMainRequest)
                if (wipMainReply.isSuccessful())
                {
                    def wipFlag = wipMainReply.getResponseData().getWIPFlagSelection()
                    def isPendingMoveIn = wipFlag.equals(CCamstarService.WIPFlag.MOVEIN.getValue())
                    def isPendingTrackIn = wipFlag.equals(CCamstarService.WIPFlag.TRACKIN.getValue())

                    logger.info("$dummyLotId wipFlag $wipFlag.")
                    logger.info("$dummyLotId pending move in $isPendingMoveIn.")
                    logger.info("$dummyLotId pending track in $isPendingTrackIn.")
                    if (isPendingMoveIn)
                    {
                        def moveinRequest = new MoveInRequest(dummyLotId,CCamstarService.DEFAULT_PROCESS_TYPE)
                        def moveinReply = cCamstarService.moveIn(moveinRequest)

                        if (moveinReply.isSuccessful())
                        {
                            def request = new LotModifyWafersRequest()
                            request.getInputData().getContainer().setName(dummyLotId)
                            request.getInputData().setSelectionId(dummyLotId)
                            request.getInputData().setUpdateOnly("FALSE")
                            for (int i=1;i<=dummyWaferCountRequired;i++)
                            {
                                def item = request.getInputData().getWafers().addWafersItem()
                                item.setWaferNumber("" + (i))
                                item.setWaferScribeNumber("" + (i))
                                item.setRequireTracking("TRUE")
                                item.setRequireDataCollection("TRUE")
                            }
                            def reply = cCamstarService.lotModifyWafers(request)
                            if (reply.isSuccessful())
                            {
                                moveinok = true
                                logger.info(reply.getResponseData().getCompletionMsg())
                            }
                            else
                            {
                                logger.error(reply.getExceptionData().getErrorDescription())
                            }
                        }
                    }
                    else if(isPendingTrackIn)
                    {
                        def requestLMTI = new LotModifyWafersRequest()
                        requestLMTI.getInputData().getContainer().setName(dummyLotId)
                        requestLMTI.getInputData().setSelectionId(dummyLotId)
                        requestLMTI.getInputData().setUpdateOnly("FALSE")
                        for (int i=1;i<=dummyWaferCountRequired;i++)
                        {
                            def item = requestLMTI.getInputData().getWafers().addWafersItem()
                            item.setWaferNumber("" + (i))
                            item.setWaferScribeNumber("" + (i))
                            item.setRequireTracking("TRUE")
                            item.setRequireDataCollection("TRUE")
                        }
                        def replyLMTI = cCamstarService.lotModifyWafers(requestLMTI)
                        if (replyLMTI.isSuccessful())
                        {
                            moveinok = true
                            logger.info(replyLMTI.getResponseData().getCompletionMsg())
                        }
                        else
                        {
                            logger.error(replyLMTI.getExceptionData().getErrorDescription())
                        }
                        moveinok = true
                    }
                }

                def currentDummyWaferCount = 0
                if(moveinok)
                {
                    Thread.sleep(3000)

                    List<WafersDetailsSelectionAllItem> waferlist = new ArrayList<WafersDetailsSelectionAllItem>()
                    def wipMain2 = new GetLotWIPMainRequest(dummyLotId)
                    def wipMainReply2 = cCamstarService.getLotWIPMain(wipMain2)
                    if (wipMainReply2.isSuccessful())
                    {
                        def iterator = wipMainReply2.getResponseData().getWafersDetailsSelectionAll().getWafersDetailsSelectionAllItems()
                        while (iterator.hasNext())
                        {
                            def wafer = iterator.next()
                            waferlist.add(wafer)
                        }
                        currentDummyWaferCount = waferlist.size()
                        logger.info("currentDummyWaferCount " + currentDummyWaferCount)
                    }
                    else
                    {
                        logger.error(wipMainReply2.getExceptionData().getErrorDescription())
                    }

                    if(currentDummyWaferCount==dummyWaferCountRequired)
                    {

                        def trackInOk = false
                        def trackInRequest = new TrackInWIPMainWafersRequest(dummyLotId, childEqp)
                        for(int k=0;k<dummyWaferCountRequired;k++)
                        {
                            def dummyWafer = waferlist[k]
                            def waferItem = trackInRequest.getInputData().getWafersDetails().addWafersDetailsItem()
                            waferItem.setWaferScribeNumber(dummyWafer.getWaferScribeNumber())
                            waferItem.setContainer(dummyLotId)
                        }
                        def trackInReply = cCamstarService.trackInWafers(trackInRequest)
                        if (trackInReply.isSuccessful())
                        {
                            trackInOk = true
                            logger.info(trackInReply.getResponseData().getCompletionMsg())
                        }
                        else
                        {
                            CamstarMesUtil.handleNoChangeError(trackInReply)
                        }

                        if(trackInOk)
                        {
                            Thread.sleep(3000)

                            // TRACK OUT DUMMY LOT FROM CHILD EQUIPMENT
                            def trackOutRequest = new TrackOutWIPMainWafersRequest()
                            trackOutRequest.getInputData().setEquipment(childEqp)
                            ContainersItem  con = trackOutRequest.getInputData().getContainers().addContainersItem()
                            con.setName(dummyLotId)
                            trackOutRequest.getInputData().setProcessType("NORMAL")
                            WIPFlag f = WIPFlag.TRACKOUT
                            trackOutRequest.getInputData().setWIPFlag(f.getValue())
                            trackOutRequest.getInputData().setTrackOutQty(String.valueOf(dummyWaferCountRequired))
                            trackOutRequest.getInputData().setRemainInEquipment("false")
                            trackOutRequest.getInputData().setRemainInEquipmentIfPossible("false")

                            for(int k=0;k<dummyWaferCountRequired;k++)
                            {
                                def dummyWafer = waferlist[k]
                                def waferItem = trackOutRequest.getInputData().getWafersDetails().addWafersDetailsItem()
                                waferItem.setWaferScribeNumber(dummyWafer.getWaferScribeNumber())
                                waferItem.setContainer(dummyLotId)
                            }

                            def trackOutReply = cCamstarService.trackOutWafers(trackOutRequest)
                            if(trackOutReply.isSuccessful())
                            {
                                logger.info(trackOutReply.getResponseData().getCompletionMsg())
                            }
                            else
                            {
                                CamstarMesUtil.handleNoChangeError(trackOutReply)
                            }
                        }
                    }
                }
				//break
            }
        }
    }

}