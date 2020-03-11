package EapVerifyAllSpecDone

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.camstar.semisuite.service.dto.ViewContainerStatusRequest
import de.znt.camstar.semisuite.service.dto.ViewContainerStatusResponse
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.CompleteMaintenanceRequest
import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.camstar.semisuite.service.dto.ModifyLotAttributesRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintResponse
import sg.znt.camstar.semisuite.service.dto.SetEquipmentStatusRequest
import sg.znt.pac.TscConstants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest

@CompileStatic
@Deo(description='''

''')
class EapVerifyAllSpecDone_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new W02CompleteOutLotRequest(inputXmlDocument)
        def monitorlotid = request.getContainerName()
        def currentStep = request.getStep()

        logger.info("lot isCancelTrackIn '" + request.isCancelTrackIn() + "'")
        if (request.isCancelTrackIn())
        {
            logger.info("lot '" + monitorlotid + "' is Cancel TrackIn. Do Not Perform EapVerifyAllSpecDone.")
            return
        }

        String wipStatus = request.getItemValue("WIPStatus")
        if (wipStatus.equalsIgnoreCase("fail"))
        {
            logger.info("Lot '$monitorlotid' WIP Status is '$wipStatus'. Do not execute remove spec...")
            return
        }

        def isHold=false
        Thread.sleep(3000)
        ViewContainerStatusRequest vr = new ViewContainerStatusRequest(monitorlotid)
        ViewContainerStatusResponse vp = new ViewContainerStatusResponse()
        vp =  cCamstarService.viewContainerStatus(vr)
        if(vp.isSuccessful())
        {
            isHold = vp.getResponseData().getLotStatus().equalsIgnoreCase("HOLD")
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(vp)
        }

        // CHECK WHETHER LOT ON HOLD DUE TO OOC. IF LOT HOLD THEN PRODUCTION RELEASE THEN MEASURE AGAIN
        if(isHold)
        {
            logger.info("Lot On Hold. Not Continue Update Attribute & Complete PM.")
            return;
        }

        // GET CURRENT LOT ATTRIBUTE PENDING MEASURE SPEC
        String lotAttrMeasSpec = getLotAttribute(monitorlotid,TscConstants.LOT_MES_ATTR_PENDING_MEASUREMENT_SPEC)

        //REMOVE THE CURERNT MEASURE SPEC
        def hMap  = new HashMap<String, String>()
        if(lotAttrMeasSpec!=null && lotAttrMeasSpec.length()>0)
        {
            def aSpec = lotAttrMeasSpec.split(",")

            for (int i=0; i<aSpec.length; i++)
            {
                if(aSpec[i].equals(currentStep)==false)
                {
                    hMap.put(i.toString(), aSpec[i])
                }
            }
        }

        // UPDATE LOT ATTRIBUTE REMAINING SPEC
        if(hMap==null)
        {
            logger.info("hMap is null or empty. no attribtue value pending spec to fill. return.")
            return
        }

        String newAttrVal=""
        for(Map.Entry<String, String> entry: hMap.entrySet())
        {
            if(newAttrVal.length()>0)
            {
                newAttrVal = newAttrVal + ","
            }
            newAttrVal = newAttrVal + entry.getValue()
        }

        def attributeRequest = new ModifyLotAttributesRequest(false, monitorlotid, TscConstants.LOT_MES_ATTR_PENDING_MEASUREMENT_SPEC, newAttrVal)
        def replyAttrSet = cCamstarService.setLotAttributes(attributeRequest)
        if (replyAttrSet.isSuccessful())
        {
            logger.info(replyAttrSet.getResponseData().toXmlString())
        }

        // GET UPDATED LOT ATTRIBUTE PENDING MEASURE SPEC
        String lotAttrMeasSpecAfter = getLotAttribute(monitorlotid,TscConstants.LOT_MES_ATTR_PENDING_MEASUREMENT_SPEC)
        if(lotAttrMeasSpecAfter.length()==0)
        {
            logger.info("Emptying resetEqpReserved4 field...")
            resetEqpReserved4(request)

            String lotAttrPmUsed = getLotAttribute(monitorlotid,TscConstants.LOT_MES_ATTR_USED_PM)
            if(lotAttrPmUsed.length()>0)
            {
                String lastProcessEqp = getLotAttribute(monitorlotid,TscConstants.LOT_MES_ATTR_LAST_PROCESS_EQP)

                if(lastProcessEqp==null || lastProcessEqp.length()==0)
                {
                    logger.info("Cannot Complete PM Due To No LastProcessEqp Attribute Value.")
                    return
                }

                def requestmaineqreq = new GetMaintenanceStatusesRequest()
                requestmaineqreq.getInputData().setResource(lastProcessEqp)
                def reply = cCamstarService.getMaintenanceStatuses(requestmaineqreq)
                if(reply.isSuccessful())
                {
                    def items = reply.getAllMaintenanceRecord()
                    while (items.hasNext())
                    {
                        def item = items.next()
                        def itemRow = item.getRow()
                        def dueTimeStamp = itemRow.getNextDateDue()
                        def pastDueTimeStamp = itemRow.getNextDateLimit()
                        def completed = itemRow.getCompleted()
                        def due = itemRow.getDue();
                        def pastDue = itemRow.getPastDue()
                        def pmState = itemRow.getMaintenanceState()
                        def pmUsed = itemRow.getMaintenanceReqName()
                        def maintenancestatus = itemRow.getMaintenanceStatus()

                        if(lotAttrPmUsed.equals(pmUsed))
                        {
                            def requestCompletePM = new CompleteMaintenanceRequest()
                            requestCompletePM.getInputData().getResource().setName(lastProcessEqp)
                            requestCompletePM.getInputData().setForceMaintenance("true")
                            def maintenanceReq = requestCompletePM.getInputData().getMaintenanceReq()
                            maintenanceReq.setName(pmUsed)
                            maintenanceReq.setUseROR("true")
                            def sdi = requestCompletePM.getInputData().getServiceDetails().addServiceDetailsItem()
                            sdi.getMaintenanceStatus().setId(maintenancestatus)
                            def replycompletePM = cCamstarService.completePMMaint(requestCompletePM)
                            if(reply.isSuccessful())
                            {
                                def completionMsg =reply.getResponseData().getCompletionMsg()
                                logger.info(completionMsg)
                                setEqpStatus(lastProcessEqp, pmUsed)
                            }
                            else
                            {
                                CamstarMesUtil.handleNoChangeError(reply)
                            }
                        }
                    }
                }
            }
        }
    }

    public void setEqpStatus(String eqpId, String pmName)
    {
        def eqpStatus = PacConfig.getStringProperty("EquipmentStatus.WaitProduction", "")
        def request = new SetEquipmentStatusRequest(eqpId, eqpStatus)
        def comment = "Equipment '$eqpId' PM '$pmName' completed, set equipment status to '$eqpStatus'"
        request.getInputData().setComments(comment)

        def reply = cCamstarService.setEquipmentStatus(request)
        if(reply.isSuccessful())
        {
            logger.info(reply.getResponseData().getCompletionMsg())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }

    void resetEqpReserved4(W02CompleteOutLotRequest outbound)
    {
        def maintenancestatus = ""
        def lastProcessEqp = ""
        def eqpId = outbound.getResourceName()
        def lotId = outbound.getContainerName()

        String result = ""
        ViewContainerStatusRequest requestStat = new ViewContainerStatusRequest(lotId)
        requestStat.getRequestData().getLotAttributes().initChildParameter(TscConstants.LOT_MES_ATTR_LAST_PROCESS_EQP, false)

        ViewContainerStatusResponse replyStat = new ViewContainerStatusResponse()
        replyStat.getResponseData().getLotAttributes().initChildParameter(TscConstants.LOT_MES_ATTR_LAST_PROCESS_EQP, false)
        replyStat =  cCamstarService.viewContainerStatus(requestStat)
        if(replyStat.isSuccessful())
        {
            lastProcessEqp = replyStat.getResponseData().getLotAttributes().getChildParameter(TscConstants.LOT_MES_ATTR_LAST_PROCESS_EQP).getValue()
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(replyStat)
        }
        //return lastProcessEqp
        logger.info ("This is the value of lastProcessEqp '$lastProcessEqp'")

        if (lastProcessEqp.length()>0)
        {
            logger.info("outbound lotId id " + lotId)
            logger.info("outbound eqp id " + eqpId)

            if (outbound.isCancelTrackIn())
            {
                logger.info("lot '" + lotId + "' is Cancel TrackIn. Do not perform EapSetEqpMainRequest.")
                return
            }

            String wipStatus = outbound.getItemValue("WIPStatus")
            if (!wipStatus.equalsIgnoreCase("fail"))
            {

                SetEquipmentMaintRequest request = new SetEquipmentMaintRequest(lastProcessEqp)

                request.getInputData().getObjectChanges().initChildParameter("tscEqpReserved4")
                request.getInputData().getObjectChanges().getChildParameter("tscEqpReserved4").setValue("")

                SetEquipmentMaintResponse reply = cCamstarService.setEquipmentMaint(request)
                if (reply.isSuccessful())
                {
                    logger.info(reply.getResponseData().getCompletionMsg())
                }
                else
                {
                    throw new Exception(reply.getExceptionData().getErrorDescription())
                }
            }
            else
            {
                logger.warn("Wip status is 'FAIL'!")
            }
        }
        else
        {
            logger.info("tscLastProcessEqp field is empty. SetEquipmentMaintRequest will not be performed.")
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