package W02ModbusCompletePMRef2

import javax.xml.stream.events.Comment

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.CompleteMaintenanceRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintResponse
import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentStatusRequest
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.CEquipmentImpl.ChildEquipment
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent

@CompileStatic
@Deo(description='''
Complete PM for ref 2 upon modbus add acid event
''')
class W02ModbusCompletePMRef2_1
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modbusEvent

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def eqpLogicalId = PacConfig.getStringProperty(modbusEvent.getChamber() + ".SystemId", "")
        if(eqpLogicalId.length() > 0)
        {
            logger.info("eqpLogicalId : $eqpLogicalId")
            def pmName = ""
            def eqpId = ""
            GetEquipmentMaintRequest eqpReq = new GetEquipmentMaintRequest(eqpLogicalId)
            eqpReq.getRequestData().getObjectChanges().initChildParameter("tscPMReqNameRef2")

            GetEquipmentMaintResponse eqpReply = new GetEquipmentMaintResponse()
            eqpReply.getResponseData().getObjectChanges().initChildParameter("tscPMReqNameRef2")
            eqpReply = cCamstarService.getEquipmentMaint(eqpReq)

            if(eqpReply.isSuccessful())
            {
                def eqpM = eqpReply.getResponseData().getObjectChanges().getChildParameter("tscPMReqNameRef2").getValue()
                logger.info("GetEquipmentMaintRequest for tscPMReqNameRef2 : '$eqpM'")
                pmName = eqpM
                eqpId = eqpLogicalId
            }
            else
            {
                logger.error(eqpReply.getExceptionData().getErrorDescription())
            }

            logger.info("Child Eqp ID:'$eqpId' with PM Name:'$pmName'")

            if(pmName != null && pmName.length() > 0)
            {
                def request = new CompleteMaintenanceRequest()
                request.getInputData().getResource().setName(eqpId)
                request.getInputData().setForceMaintenance("TRUE")
                def maintenanceReq = request.getInputData().getMaintenanceReq()
                maintenanceReq.setName(pmName)
                maintenanceReq.setUseROR("true")
                def serviceDetailsItem = request.getInputData().getServiceDetails().addServiceDetailsItem()
                serviceDetailsItem.getMaintenanceStatus().setId(getStatusId(pmName, eqpId))

                def reply = cCamstarService.completePMMaint(request)
                if(reply.isSuccessful())
                {
                    logger.info(reply.getResponseData().getCompletionMsg())
                    setEqpStatus(eqpId, pmName)
                }
                else
                {
                    CamstarMesUtil.handleNoChangeError(reply)                    
                }
            }
        }
    }
    
    public void setEqpStatus(String eqpId, String pmName)
    {
        def newEqpStatus = PacConfig.getStringProperty("EquipmentStatus.WaitProduction", "")
        def request = new SetEquipmentStatusRequest(eqpId, newEqpStatus)
        def comment = "Equipment '$eqpId' PM '$pmName' completed, set equipment status to '$newEqpStatus'"
        request.getInputData().setComments(comment)
        
        def reply = cCamstarService.setEquipmentStatus(request)
        if (reply.isSuccessful())
        {
            logger.info(reply.getResponseData().getCompletionMsg())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
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
                def due = itemRow.getDue();
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