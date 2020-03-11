package MesUpdatePMMaintStatus

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.pac.domainobject.EquipmentPMDomainObject
import sg.znt.pac.domainobject.EquipmentPMDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.DateUtils
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Update PM Maintenance status
Use SnrMsmMesUpdatePMInfo to dispatch the request
''')
class MesUpdatePMMaintStatus_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="EquipmentPMDomainObjectManager")
    EquipmentPMDomainObjectManager equipmentPMDomainObjectManager
    
    @DeoBinding(id="MaintenanceRequirement")
    private String maintenanceRequirement
    
    @DeoBinding(id="ToolSpcDummyLotId")
    private String toolSpcDummyLotId
    
    @DeoBinding(id="ToolSpcSpecName")
    private String toolSpcSpecName
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {       
        def maintenanceStatusId = ""
        def request = new GetMaintenanceStatusesRequest()
        request.getInputData().setResource(cEquipment.getSystemId())
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
                def due = itemRow.getDue();     //false
                def pastDue = itemRow.getPastDue()      //true
                def pmState = itemRow.getMaintenanceState()  //Past Due
                def key = itemRow.getMaintenanceReqName()
                if (key.equalsIgnoreCase(maintenanceRequirement))
                {
                    logger.info("Updating maintenance requirement '" + maintenanceRequirement + "' status.")
                    def equipmentPmSet = equipmentPMDomainObjectManager.getEquipmentPMSet(cEquipment.getSystemId())
                    if (equipmentPmSet == null)
                    {
                        equipmentPmSet = equipmentPMDomainObjectManager.createEquipmentPMSet(cEquipment.getSystemId())
                        equipmentPMDomainObjectManager.addEquipmentPMSet(equipmentPmSet)
                    }
                    def equipmentPm = equipmentPmSet.getEquipmentPMDomainObject(maintenanceRequirement)
                    if (equipmentPm == null)
                    {
                        equipmentPm = new EquipmentPMDomainObject(maintenanceRequirement)
                        equipmentPmSet.addEquipmentPMDomainObject(equipmentPm)
                    }
                    maintenanceStatusId = itemRow.getMaintenanceStatus()
                    equipmentPm.setDue(due)
                    equipmentPm.setMaintenanceStatusId(maintenanceStatusId)
                    equipmentPm.setMaintenanceState(pmState)
                    equipmentPm.setNextDateDue(DateUtils.convertCamstarDateStringToDefaultDate(dueTimeStamp))
                    equipmentPm.setPastDue(pastDue)
                    equipmentPm.setPMType(EquipmentPMDomainObject.PM_TYPE_TOOL_SPC)
                    equipmentPm.setLotId(toolSpcDummyLotId)
                    equipmentPm.setSpecName(toolSpcSpecName)
                }
            }
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    
    }
}