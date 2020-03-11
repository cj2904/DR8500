package W02ModbusTrackedOutCompletePM

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.Deo
import de.znt.pac.deo.annotations.DeoBinding
import de.znt.pac.deo.annotations.DeoExecute
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.CompleteMaintenanceRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintResponse
import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.camstar.semisuite.service.dto.SetEqpProcessCapabilityRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.pac.TscConfig
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.CEquipmentImpl.ChildEquipment
import sg.znt.pac.material.CLotState
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02CompleteOutLotRequest


@CompileStatic
@Deo(description='''
Complete PM upon modbus change acide event
''')
class W02ModbusTrackedOutCompletePM_1
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02CompleteOutLotRequest(inputXml)
        def lotId = outbound.getContainerName()

        logger.info("lot isCancelTrackIn '" + outbound.isCancelTrackIn() + "'")
        if (outbound.isCancelTrackIn())
        {
            logger.info("lot '" + lotId + "' is Cancel TrackIn. Do Not Perform W02ModbusTrackedOutCompletePM.")
            //return
        }


        def pmeqp = mainEquipment.getPropertyContainer().getStringArray(mainEquipment.getSystemId() + "_PendingPM", new String[0])
        for (pe in pmeqp)
        {
            logger.info(" pendg pm eqp " + pe)
            if(foundLotEqpWithPendingEqp(pe)==false)
            {
                def pmName=""
                def eqId=""
                def childEq = getChildEquipmentByLogicalId(pe)
                if(childEq!=null)
                {
                    pmName = childEq.getThruputRequirement()
                    eqId =childEq.getEquipmentId()
                }
                if (pmName == null || pmName.length()==0)
                {
                    GetEquipmentMaintRequest eqreq = new GetEquipmentMaintRequest(pe)
                    eqreq.getRequestData().getObjectChanges().initChildParameter("tscPMReqNameRef")

                    GetEquipmentMaintResponse eqreply = new GetEquipmentMaintResponse()
                    eqreply.getResponseData().getObjectChanges().initChildParameter("tscPMReqNameRef")
                    eqreply = cCamstarService.getEquipmentMaint(eqreq)
                    if(eqreply.isSuccessful())
                    {
                        def eqpm = eqreply.getResponseData().getObjectChanges().getChildParameter("tscPMReqNameRef").getValue()
                        logger.info(" GetEquipmentMaintRequest tscPMReqNameRef " + eqpm)
                        pmName = eqpm
                        eqId = pe
                    }
                }


                logger.info(" child eqp pmName " + pmName)
                logger.info(" child eqp Id " + eqId)
                if (pmName!=null && pmName.length()>0)
                {
                    def request = new CompleteMaintenanceRequest()
                    request.getInputData().getResource().setName(eqId)
                    request.getInputData().setForceMaintenance("TRUE")
                    def maintenanceReq = request.getInputData().getMaintenanceReq()
                    maintenanceReq.setName(pmName)
                    maintenanceReq.setUseROR("true")
                    def sdi = request.getInputData().getServiceDetails().addServiceDetailsItem()
                    def statusId = getStatusId(pmName,eqId)
                    if(statusId.length() > 0)
                    {
                        sdi.getMaintenanceStatus().setId(getStatusId(pmName,eqId))                        
                    }

                    def reply = cCamstarService.completePMMaint(request)
                    if(reply.isSuccessful())
                    {
                        logger.info(reply.getResponseData().getCompletionMsg())

                        def pmeqplist2 = new ArrayList<String>()
                        def pmeqp2 = mainEquipment.getPropertyContainer().getStringArray(mainEquipment.getSystemId() + "_PendingPM", new String[0])
                        for (pe2 in pmeqp2)
                        {
                            if(pe2.equalsIgnoreCase(eqId)==false)
                            {
                                logger.info(" existing pending pm eqp " + pe2)
                                pmeqplist2.add(pe2)
                            }
                        }
                        mainEquipment.getPropertyContainer().setStringArray(mainEquipment.getSystemId() + "_PendingPM", pmeqplist2.toArray(new String[0]))
                    }
                    CamstarMesUtil.handleNoChangeError(reply)
                    resetCapability(eqId)

                    def request3 = new SetEquipmentMaintRequest(outbound.getResourceName())
                    request3.getInputData().getObjectChanges().initChildParameter("tscLastRunCapability")
                    request3.getInputData().getObjectChanges().getChildParameter("tscLastRunCapability").setValue("")

                    def reply3 = cCamstarService.setEquipmentMaint(request3)
                    if (reply3.isSuccessful())
                    {
                        logger.info(reply3.getResponseData().getCompletionMsg())
                    }
                    else
                    {
                        throw new Exception(reply3.getExceptionData().getErrorDescription())
                    }
                }
            }
        }
    }

    private void resetCapability(String eqpId)
    {
        try
        {
            def req = new GetEquipmentMaintRequest()
            req.getInputData().getObjectToChange().setName(eqpId)

            GetEquipmentMaintResponse res = new GetEquipmentMaintResponse()
            res = cCamstarService.getEquipmentMaint(req)
            def parenteqp = res.getResponseData().getObjectChanges().getParentResource().getName()
            logger.info("EapResetProcessCapability parenteqp " + parenteqp )

            def request = new GetEquipmentMaintRequest(parenteqp)
            def reply = cCamstarService.getEquipmentMaint(request)
            List<String> capabilityList = new ArrayList<String>()
            if (reply.isSuccessful())
            {
                def items = reply.getResponseData().getObjectChanges().getEqpProcessCapability().getEqpProcessCapabilityItems()
                while(items.hasNext())
                {
                    def item = items.next()
                    capabilityList.add(item.getProcessCapability().getName())
                }
            }

            if(capabilityList.size()>0)
            {
                def request2 = new SetEqpProcessCapabilityRequest()
                request2.getInputData().setResource(parenteqp)
                for (var in capabilityList)
                {
                    logger.info("EapResetProcessCapability SetEqpProcessCapabilityRequest var " + var )
                    def item = request2.getInputData().getDetails().addDetailsItem()
                    item.getProcessCapability().setName(var)
                    item.setAvailability("TRUE")
                    item.setActivationStatus("TRUE")
                }
                def reply2=cCamstarService.setEqpProcessCapability(request2)
            }
        }
        catch (Exception e)
        {
            e.printStackTrace()
        }
    }

    public boolean foundLotEqpWithPendingEqp(String pendingeqp)
    {
        def existingLotList = cMaterialManager.getCLotList(new LotFilterAll())
        for (existingLot in existingLotList)
        {
            def childeqp = existingLot.getPropertyContainer().getStringArray(existingLot.getEquipmentId() + "_ChildEquipments", new String[0])
            for(int i =0 ; i < childeqp.length ; i ++)
            {
                logger.info("lot eq [" + childeqp[i] + "]")
                if(pendingeqp.equalsIgnoreCase(childeqp[i]))
                {
                    def trackInAfterPm = true

                    def lastPmTime = mainEquipment.getPropertyContainer().getString(pendingeqp + "_LastPMTime", "")
                    def initTime = existingLot.getStateTransactionTimeFormated(CLotState.Init)
                    logger.info("Last PM Time for " + pendingeqp + " is " + lastPmTime + " and lot initTime=" + initTime)
                    if (lastPmTime.length()>0 && initTime.length()>0)
                    {
                        def lastPmTimeInLong = CDateFormat.getTime(lastPmTime)
                        def initTimeInLong = CDateFormat.getTime(initTime)
                        logger.info("Last PM Time for " + pendingeqp + " is " + lastPmTimeInLong + " and lot initTime=" + initTimeInLong)
                        if (initTime > lastPmTime)
                        {
                            logger.info("Lot '" + existingLot.getId() + "' is tracked in '$initTime' after PM time '" + lastPmTime + "'")
                            if (TscConfig.getBooleanProperty("TrackInAfterPmCheck", false))
                            {
                                trackInAfterPm = false
                            }
                        }
                    }

                    if (trackInAfterPm)
                    {
                        logger.info("lot with eq [" + childeqp[i] + "] still found for pending pm eqp ["+pendingeqp+"], do not complete pm.")
                        return true
                    }
                }
            }
        }
        return false
    }

    public String getStatusId(String pmName,String equipmentId)
    {
        def maintenanceStatusId = ""
        def request = new GetMaintenanceStatusesRequest()
        request.getInputData().setResource(equipmentId)
        def reply = cCamstarService.getMaintenanceStatuses(request)
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
                def due = itemRow.getDue()
                def pastDue = itemRow.getPastDue()
                def pmState = itemRow.getMaintenanceState()
                def key = itemRow.getMaintenanceReqName()
                if (key.equalsIgnoreCase(pmName))
                {
                    return itemRow.getMaintenanceStatus()
                }
            }
        }

        return ""
    }

    ChildEquipment getChildEquipmentByLogicalId(String logicalId)
    {
        return mainEquipment.getChildEquipment(logicalId)
    }
}