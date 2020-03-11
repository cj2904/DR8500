package MczCompleteMaintenance

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.CompleteMaintenanceRequest
import sg.znt.camstar.semisuite.service.dto.GetMaintenanceStatusesRequest
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarServiceImpl
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Complete maintenance for equipment
''')
class MczCompleteMaintenance_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarServiceImpl")
    private CCamstarServiceImpl cCamstarService
    
    @DeoBinding(id="MaintenanceRequirement")
    private String maintenanceRequirement
    
    @DeoBinding(id="EquipmentId")
    private String equipmentId
    
    @DeoBinding(id="ForceMaintenance")
    private String forceMaintenance
    
    /**
     *
     */
    @DeoExecute
    public void execute()
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
                if (key.equalsIgnoreCase(maintenanceRequirement))
                {
                    maintenanceStatusId = itemRow.getMaintenanceStatus()
                }
            }
        }
        CamstarMesUtil.handleNoChangeError(reply)
        
        request = new CompleteMaintenanceRequest()
        def inputData = request.getInputData()
        inputData.getResource().setName(equipmentId)
        inputData.setForceMaintenance(forceMaintenance)
        def maintenanceReq = inputData.getMaintenanceReq()
        maintenanceReq.setName(maintenanceRequirement)
        maintenanceReq.setUseROR("true")
        def sdi = inputData.getServiceDetails().addServiceDetailsItem()
        sdi.getMaintenanceStatus().setId(maintenanceStatusId)

        reply = cCamstarService.completePMMaint(request)
        if(reply.isSuccessful())
        {
            logger.info(reply.getResponseData().toXmlString())
        }
        CamstarMesUtil.handleNoChangeError(reply)
    }
}