package W02ModbusVerifyThruput

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainRequest
import sg.znt.camstar.semisuite.service.dto.TrackInWIPMainWafersRequest
import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainWafersRequest
import sg.znt.camstar.semisuite.service.dto.GetLotWIPMainResponseDto.ResponseData.WafersDetailsSelectionAll.WafersDetailsSelectionAllItem
import sg.znt.pac.SgdConfig
import sg.znt.pac.TscConfig
import sg.znt.pac.domainobject.PM
import sg.znt.pac.domainobject.PmManager
import sg.znt.pac.exception.ModbusException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.CEquipmentImpl
import sg.znt.pac.machine.ThruputPM
import sg.znt.pac.machine.CEquipmentImpl.ChildEquipment
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.CCamstarService.WIPFlag
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.camstar.semisuite.service.dto.MoveInRequest
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.Deo
import de.znt.pac.deo.annotations.DeoBinding
import de.znt.pac.deo.annotations.DeoExecute

@CompileStatic
@Deo(description='''
Verify if right chamber to start against thruput
''')
class W02ModbusVerifyThruput_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modBusEvent

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="PmManager")
    private PmManager pmManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        try
        {

            def eqLogicalId = PacConfig.getStringProperty(modBusEvent.getChamber() + ".SystemId", "")
            def alternateChamber = SgdConfig.findThruputCheckChamberId(modBusEvent.getChamber())
            def alternateEqLogicalId = PacConfig.getStringProperty(alternateChamber + ".SystemId", "")
            def childEq = getChildEquipmentByLogicalId(eqLogicalId)
            if (childEq != null)
            {
                def alternateChildEq = getChildEquipmentByLogicalId(alternateEqLogicalId)
                if (alternateChildEq == null)
                {
                    return
                }
                def qtyEq = childEq.getThruput2AtTrackIn()
                def qtyAlternateEq = alternateChildEq.getThruput2AtTrackIn()

                def childDummyLotId = ""
                for(pm in pmManager.getAllDomainObject())
                {
                    if(pm.getEquipmentId().equalsIgnoreCase(childEq.getEquipmentId()))
                    {
                        childDummyLotId = pm.getDummyLotId()
                        break
                    }
                }

                logger.info("childEq " + childEq.getEquipmentId())
                logger.info("alternateChildEq " + alternateChildEq.getEquipmentId())
                logger.info("qtyEq " + qtyEq)
                logger.info("qtyAlternateEq " + qtyAlternateEq)
                logger.info("childDummyLotId " + childDummyLotId)

                def pmeqp = mainEquipment.getPropertyContainer().getStringArray(mainEquipment.getSystemId() + "_PendingPM", new String[0])
                if(TscConfig.getBooleanProperty("VerifyThruputWithPendingPM", true) && pmeqp.length>0)
                {
                    for (pe in pmeqp)
                    {
                        logger.info(" pending pm eqp " + pe)
                        if(eqLogicalId.equalsIgnoreCase(pe))
                        {
                            def errMsg = "Must start at $alternateEqLogicalId As $eqLogicalId Pending PM."
                            throw new ModbusException(errMsg, ModbusException.WRONG_ACID_CHAMBER)
                        }
                    }
                }

                //TODO: Please help to verify if user want to start at new acid or old acid >>>>>> Start at old acid
                if (qtyEq<qtyAlternateEq)
                {
                    def errMsg = "Must start at $alternateEqLogicalId thruput $qtyAlternateEq, $eqLogicalId thruput $qtyEq."
                    throw new ModbusException(errMsg, ModbusException.WRONG_ACID_CHAMBER)
                }

                if(mainEquipment.getPropertyContainer().getBoolean("DummyLotTrackInOutStarted",false))
                {
                    throw new Exception("Dummy Lot TrackIn and Track Out InProgress. Not Handle Chamber Soft Start Trigger.")
                }


                //TODO: increase thruput if allow to start, need to have model dummy lot id able to run on the dummy spec, that
                //dummy spec able to track in to child equipment
                if(qtyEq==0 && qtyAlternateEq==0)
                {

                    def moveinok = false
                    def mvwipMainRequest = new GetLotWIPMainRequest(childDummyLotId)
                    def mvwipMainReply = cCamstarService.getLotWIPMain(mvwipMainRequest)
                    if (mvwipMainReply.isSuccessful())
                    {
                        def wipFlag = mvwipMainReply.getResponseData().getWIPFlagSelection()
                        def isPendingMoveIn = wipFlag.equals(CCamstarService.WIPFlag.MOVEIN.getValue())
                        def isPendingTrackIn = wipFlag.equals(CCamstarService.WIPFlag.TRACKIN.getValue())

                        logger.info("$childDummyLotId pending move in $isPendingMoveIn.")
                        if (isPendingMoveIn)
                        {
                            def moveinRequest = new MoveInRequest(childDummyLotId,CCamstarService.DEFAULT_PROCESS_TYPE)
                            def moveinReply = cCamstarService.moveIn(moveinRequest)

                            if (moveinReply.isSuccessful())
                            {
                                moveinok = true
                                logger.info(moveinReply.getResponseData().getCompletionMsg())
                            }
                            else
                            {
                                logger.error(moveinReply.getExceptionData().getErrorDescription())
                            }
                        }
                        else if(isPendingTrackIn)
                        {
                            moveinok = true
                        }
                    }
                    else
                    {
                        logger.error(mvwipMainReply.getExceptionData().getErrorDescription())
                    }


                    // TRACK IN DUMMY LOT TO CHILD EQUIPMENT
                    if(childDummyLotId!=null && childDummyLotId.length()>0)
                    {
                        mainEquipment.getPropertyContainer().setBoolean("DummyLotTrackInOutStarted",true)
                        List<WafersDetailsSelectionAllItem> waferlist = new ArrayList<WafersDetailsSelectionAllItem>()
                        def wipMain = new GetLotWIPMainRequest(childDummyLotId)
                        def wipMainReply = cCamstarService.getLotWIPMain(wipMain)
                        if (wipMainReply.isSuccessful())
                        {
                            def iterator = wipMainReply.getResponseData().getWafersDetailsSelectionAll().getWafersDetailsSelectionAllItems()
                            while (iterator.hasNext())
                            {
                                def wafer = iterator.next()
                                waferlist.add(wafer)
                            }
                        }
                        else
                        {
                            logger.error(wipMainReply.getExceptionData().getErrorDescription())
                        }

                        def trackInOk = false
                        def trackInRequest = new TrackInWIPMainWafersRequest(childDummyLotId, childEq.getEquipmentId())
                        for(int k=0;k<waferlist.size();k++)
                        {
                            def dummyWafer = waferlist[k]
                            def waferItem = trackInRequest.getInputData().getWafersDetails().addWafersDetailsItem()
                            waferItem.setWaferScribeNumber(dummyWafer.getWaferScribeNumber())
                            waferItem.setContainer(childDummyLotId)
                        }
                        def trackInReply = cCamstarService.trackInWafers(trackInRequest)
                        if (trackInReply.isSuccessful())
                        {
                            logger.info(trackInReply.getResponseData().getCompletionMsg())
                            trackInOk = true
                        }
                        else
                        {
                            CamstarMesUtil.handleNoChangeError(trackInReply)
                        }

                        if(trackInOk)
                        {
                            Thread.sleep(1000)
                            // TRACK OUT DUMMY LOT FROM CHILD EQUIPMENT
                            def trackOutRequest = new TrackOutWIPMainWafersRequest()
                            trackOutRequest.getInputData().setEquipment(childEq.getEquipmentId())
                            def  con = trackOutRequest.getInputData().getContainers().addContainersItem()
                            con.setName(childDummyLotId)
                            trackOutRequest.getInputData().setProcessType("NORMAL")
                            WIPFlag f = WIPFlag.TRACKOUT
                            trackOutRequest.getInputData().setWIPFlag(f.getValue())
                            trackOutRequest.getInputData().setTrackOutQty(String.valueOf(waferlist.size()))
                            trackOutRequest.getInputData().setRemainInEquipment("false")
                            trackOutRequest.getInputData().setRemainInEquipmentIfPossible("false")

                            for(int k=0;k<waferlist.size();k++)
                            {
                                def dummyWafer = waferlist[k]
                                def trackOutWaferItem = trackOutRequest.getInputData().getWafersDetails().addWafersDetailsItem()
                                trackOutWaferItem.setWaferScribeNumber(dummyWafer.getWaferScribeNumber())
                                trackOutWaferItem.setContainer(childDummyLotId)
                            }
                            def trackOutReply = cCamstarService.trackOutWafers(trackOutRequest)
                            if(trackOutReply.isSuccessful())
                            {
                                logger.info(trackOutReply.getResponseData().getCompletionMsg())

                                try
                                {
                                    updateDummyThruputQty(childEq, waferlist.size())
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace()
                                }
                            }
                            else
                            {
                                CamstarMesUtil.handleNoChangeError(trackOutReply)
                            }
                        }
                        mainEquipment.getPropertyContainer().setBoolean("DummyLotTrackInOutStarted",false)
                    }
                }
            }
        }
        finally
        {
            mainEquipment.getPropertyContainer().setBoolean("DummyLotTrackInOutStarted",false)
        }
    }

    private void updateDummyThruputQty(ChildEquipment childEq, int dummyQty)
    {
        List thruputPMList = childEq.getThruputPM();
        for (ThruputPM thruputPM : thruputPMList)
        {
            if ((thruputPM.getEquipmentId().equalsIgnoreCase(childEq.getEquipmentId())) && (thruputPM.isThruputControl()))
            {
                logger.info("Update PM " + thruputPM.getPmName() + " for dummy qty after track in successfully!")
                ((PM)thruputPM).setThruputQty2(dummyQty);
                break;
            }
        }
    }

    ChildEquipment getChildEquipmentByLogicalId(String logicalId)
    {
        return mainEquipment.getChildEquipment(logicalId)
    }

}