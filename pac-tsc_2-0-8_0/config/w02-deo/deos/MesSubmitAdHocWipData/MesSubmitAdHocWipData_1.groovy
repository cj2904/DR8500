package MesSubmitAdHocWipData

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.EquipmentPMDomainObject
import sg.znt.pac.domainobject.EquipmentPMDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectSet
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.scenario.ExecutionResult
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher
import de.znt.pac.domainobject.filter.FilterAllDomainObjects

@CompileStatic
@Deo(description='''
Submit adhoc wip data
''')
class MesSubmitAdHocWipData_1 
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    @DeoBinding(id="TscEquipment")
    private CEquipment equipment
    
    @DeoBinding(id="WipDataSet")
    private WipDataDomainObjectSet wipDataSet
    
    @DeoBinding(id="EquipmentPMDomainObjectManager")
    private EquipmentPMDomainObjectManager equipmentPMDomainObjectManager

    @DeoBinding(id="DeoEventDispatcher")
    private DeoEventDispatcher deoEventDispatcher
    /**
     *
     */
    @DeoExecute(result="result")
    public ExecutionResult execute()
    {
        //TODO: to confirm if this is needed
        /**
        def result = new ExecutionResult() 
        def pmInfo = getEquipmentPm()
        def request = new AdhocWIPDataRequest(wipDataSet.getId(), wipDataSet.getObjectType(), equipment.getSystemId())
        //request.getInputData().initNameChildParameter("Resource").setValue(equipment.getSystemId())
        
        def wipEntrySet = wipDataSet.getDataCollection().entrySet()
        for (entry in wipEntrySet)
        {
            def detailItem = request.getInputData().getDetails().addDetailsItem()
            detailItem.setWIPDataName(entry.getKey())
            detailItem.setWIPDataValue(entry.getValue())
        }
        
        def reply = cCamstarService.sendAdhocWipData(request)
        if (reply.isSuccessful())
        {
            logger.info(reply.getResponseData().getCompletionMsg())
            
            request = new CompleteMaintenanceRequest()
            def inputData = request.getInputData()
            inputData.getResource().setName(equipment.getSystemId())
            inputData.setForceMaintenance("false")
            def maintenanceReq = inputData.getMaintenanceReq()
            maintenanceReq.setName(pmInfo.getId())
            maintenanceReq.setUseROR("true")
            def sdi = inputData.getServiceDetails().addServiceDetailsItem()
            sdi.getMaintenanceStatus().setId(pmInfo.getMaintenanceStatusId())
    
            reply = cCamstarService.completePMMaint(request)
            if(reply.isSuccessful())
            {
                def completionMsg =reply.getResponseData().getCompletionMsg() 
                logger.info(completionMsg)
                result.setMessage(completionMsg)
                pmInfo.setExecuted(false)
                try 
                {
                    new PMMeasurementScenario().mesUpdatPmInfo(deoEventDispatcher, equipment, cCamstarService, pmInfo.getId(), pmInfo.getLotId(), pmInfo.getSpecName())
                } 
                catch (Exception e) 
                {
                    ErrorManager.handleError(e, this)                    
                }
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(reply)
            }
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
        return result
        **/
    }
    
    private EquipmentPMDomainObject getEquipmentPm()
    {
        def equipmentPMSet = equipmentPMDomainObjectManager.getEquipmentPMSet(equipment.getSystemId())
        if (equipmentPMSet == null)
        {
            throw new Exception("Equipment PM '" + equipment.getSystemId() + "' set not found!")
        }
        else
        {
            def equipmentPMList = equipmentPMSet.getAll(new FilterAllDomainObjects())
            for (pmInfo in equipmentPMList)
            {
                if (pmInfo.getPMType().equalsIgnoreCase(EquipmentPMDomainObject.PM_TYPE_TOOL_SPC))
                {
                    return pmInfo
                }
            }
        }
        throw new Exception("Equipment adhoc PM '" + equipment.getSystemId() + "' not found!")            
    }
}