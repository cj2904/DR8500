package W02ModbusCompletePM

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.CompleteMaintenanceRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintResponse
import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.CEquipmentImpl.ChildEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.Deo
import de.znt.pac.deo.annotations.DeoBinding
import de.znt.pac.deo.annotations.DeoExecute


@CompileStatic
@Deo(description='''
Complete PM upon modbus change acide event
''')
class W02ModbusCompletePM_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modBusEvent

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def eqLogicalId = PacConfig.getStringProperty(modBusEvent.getChamber() + ".SystemId", "")
        if (eqLogicalId.length()>0)
        {
            logger.info("eqLogicalId "+ eqLogicalId)
            String formatedTransactionTime = CDateFormat.getFormatedDate(new Date());
            logger.info("setLastPMTime for '" + eqLogicalId + "' to '" + formatedTransactionTime + "'.");
            mainEquipment.getPropertyContainer().setString(eqLogicalId + "_LastPMTime", formatedTransactionTime);
            
            if(ChildEqpIdWithMaterial(eqLogicalId))
            {
                def pmeqplist = new ArrayList<String>();

                def pmeqp = mainEquipment.getPropertyContainer().getStringArray(mainEquipment.getSystemId() + "_PendingPM", new String[0])
                for (pe in pmeqp)
                {
                    logger.info(" existing pending pm eqp " + pe)
                    pmeqplist.add(pe)
                }

                def found = false
                for (pei in pmeqplist)
                {
                    logger.info(" pending pm eqp array list " + pei)
                    if(eqLogicalId.equalsIgnoreCase(pei))
                    {
                        found = true
                        break
                    }
                }

                if(!found)
                {
                    pmeqplist.add(eqLogicalId)
                }

                mainEquipment.getPropertyContainer().setStringArray(mainEquipment.getSystemId() + "_PendingPM", pmeqplist.toArray(new String[0]))
            }
            else
            {
                def pmName=""
                def eqId=""
                def childEq = getChildEquipmentByLogicalId(eqLogicalId)
                if(childEq!=null)
                {
                    try
                    {
                        pmName = childEq.getThruputRequirement()
                        eqId =childEq.getEquipmentId()
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace()
                    }
                }
                if (pmName ==null || pmName.length()==0)
                {
                    GetEquipmentMaintRequest eqreq = new GetEquipmentMaintRequest(eqLogicalId)
                    eqreq.getRequestData().getObjectChanges().initChildParameter("tscPMReqNameRef")

                    GetEquipmentMaintResponse eqreply = new GetEquipmentMaintResponse()
                    eqreply.getResponseData().getObjectChanges().initChildParameter("tscPMReqNameRef")
                    eqreply = cCamstarService.getEquipmentMaint(eqreq)
                    if(eqreply.isSuccessful())
                    {
                        def eqpm = eqreply.getResponseData().getObjectChanges().getChildParameter("tscPMReqNameRef").getValue()
                        logger.info(" GetEquipmentMaintRequest tscPMReqNameRef " + eqpm)
                        pmName = eqpm
                        eqId = eqLogicalId
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
                    sdi.getMaintenanceStatus().setId(getStatusId(pmName,eqId))

                    def reply = cCamstarService.completePMMaint(request)
                    if(reply.isSuccessful())
                    {
                        logger.info(reply.getResponseData().getCompletionMsg())

                        def pmeqplist = new ArrayList<String>();
                        def pmeqp = mainEquipment.getPropertyContainer().getStringArray(mainEquipment.getSystemId() + "_PendingPM", new String[0])
                        for (pe in pmeqp)
                        {
                            if(pe.equalsIgnoreCase(eqId)==false)
                            {
                                logger.info(" existing pending pm eqp " + pe)
                                pmeqplist.add(pe)
                            }
                        }
                        mainEquipment.getPropertyContainer().setStringArray(mainEquipment.getSystemId() + "_PendingPM", pmeqplist.toArray(new String[0]))
                    }
                    CamstarMesUtil.handleNoChangeError(reply)
                }
            }
        }
    }

    public boolean ChildEqpIdWithMaterial(String eqLogicalId)
    {
        def existingLotList = cMaterialManager.getCLotList(new LotFilterAll())
        for (existingLot in existingLotList)
        {
            def childeqp = existingLot.getPropertyContainer().getStringArray(existingLot.getEquipmentId() + "_ChildEquipments", new String[0])
            for(int i =0 ; i < childeqp.length ; i ++)
            {
                logger.info("lot eq [" + childeqp[i] + "]")
                if(eqLogicalId.equalsIgnoreCase(childeqp[i]))
                {
                    return true;
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