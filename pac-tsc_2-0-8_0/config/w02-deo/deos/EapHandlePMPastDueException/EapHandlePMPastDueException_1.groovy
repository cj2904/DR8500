package EapHandlePMPastDueException

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetRecipeMatrixMaintDetailsRequest
import sg.znt.camstar.semisuite.service.dto.GetRecipeMatrixMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetWIPDataSetupMatrixMaintDetailsRequest
import sg.znt.camstar.semisuite.service.dto.GetWIPDataSetupMatrixMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetWipDataSetupMaintDetailsRequest
import sg.znt.pac.domainobject.EquipmentPMDomainObject
import sg.znt.pac.domainobject.EquipmentPMDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.domainobject.WipDataDomainObjectSet
import sg.znt.pac.exception.PMPastDueException
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects.*

@CompileStatic
@Deo(description='''
Handle PM Past due exception
''')
class EapHandlePMPastDueException_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="Exception")
    private Throwable exception
    
    @DeoBinding(id="CamstarService")
    private CCamstarService camstarService
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="EquipmentPMDomainObjectManager")
    EquipmentPMDomainObjectManager equipmentPMDomainObjectManager
    
    @DeoBinding(id="WipDataDomainObjectManager")
    WipDataDomainObjectManager wipDataDomainObjectManager
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {        
        if (exception instanceof PMPastDueException)
        {
            logger.error(exception.getMessage())
            TscEquipment cEquipment = (sg.znt.pac.machine.TscEquipment) exception.getEquipment() 
            def equipmentPm = exception.getEquipmentPm()
            def maintenanceRequirement = equipmentPm.getId()
            def lotCount = cMaterialManager.getCLotList(new LotFilterAll()).size()
            if (lotCount == 0)
            {
                def toolSpcSpecName = equipmentPm.getSpecName()
                def pmRecipe = queryPmRecipe(equipmentPm, toolSpcSpecName, cEquipment)
                def wipDataSetupName = queryWipDataSetup(toolSpcSpecName, cEquipment, equipmentPm.getLotId())
                
                def map = new HashMap<String, Object>()
                map.put("LotId", equipmentPm.getLotId())
                map.put("Recipe", equipmentPm.getPMRecipe())
                cEquipment.getModelScenario().eqpSelectRecipe(pmRecipe, map)
                
                /**
                def msg = equipmentPm.getId() + " is required!"
                def adhocWipData = TscWinApiEqpUtil.getAdhocWipData(wipDataDomainObjectManager, wipDataSetupName)
                def mapTrackIn = TscWinApiEqpUtil.createMesTrackInSucceedRequest(equipmentPm.getLotId(), adhocWipData, wipDataSetupName, cEquipment.getSystemId(), msg, "", "")
                cEquipment.getModelScenario().eqpMesTrackInSucceed(mapTrackIn)
                
                equipmentPm.setExecuted(true)
                
                **/
            }
            else
            {
                logger.info("PM '" + maintenanceRequirement + "' is past due! Pending lot to completed.")
            }
        }        
        else
        {
            throw exception
        }
    }
    
    private String queryWipDataSetup(String toolSpcSpecName, TscEquipment cEquipment, String lotId)
    {
        def wipDataSetupRequest = new GetWIPDataSetupMatrixMaintRequest()
        def objectChanges = wipDataSetupRequest.getInputData().getObjectChanges()
        objectChanges.getSsSpec().setName(toolSpcSpecName)
        objectChanges.getSsSpec().setUseROR("true")
        objectChanges.setSsEquipment(cEquipment.getSystemId())
        def wipDataSetupResponse = camstarService.getWIPDataSetupMatrixMaint(wipDataSetupRequest)
        if (wipDataSetupResponse.isSuccessful())
        {
            logger.info(wipDataSetupResponse.getResponseData().toXmlString())
            def iterator = wipDataSetupResponse.getAllWIPDataSetupMatrixMaint()
            while (iterator.hasNext())
            {
                def headerName = iterator.next().getName()
                def requestDetails = new GetWIPDataSetupMatrixMaintDetailsRequest()
                requestDetails.getInputData().getObjectToChange().setName(headerName)
                def detailsReply = camstarService.getWIPDataSetupMatrixMaintDetails(requestDetails)
                logger.info(detailsReply.getResponseData().toXmlString())
                if (wipDataSetupResponse.isSuccessful())
                {
                    def replyObjectChanges = detailsReply.getResponseData().getObjectChanges()
                    def wIPDataDetails = replyObjectChanges.getSsWIPDataDetails().getSsWIPDataDetailsItems()
                    def name = replyObjectChanges.getSsWIPDataSetup().getName()
                    queryWipDataItem(name, cEquipment, lotId)
                    return name
                }
                else
                {
                    CamstarMesUtil.handleNoChangeError(detailsReply)
                }
                break
            }
            throw new Exception("No wip data setup define for tool spc spec '" + toolSpcSpecName + "'!")
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(wipDataSetupResponse)
        }
        throw new Exception("No wip data setup define for tool spc spec '" + toolSpcSpecName + "'!")
    }
    
    public void queryWipDataItem(String name, TscEquipment cEquipment, String lotId)
    {
        def wipDataSetupMaintDetailsRequest = new GetWipDataSetupMaintDetailsRequest()
        wipDataSetupMaintDetailsRequest.getInputData().setObjectToChange(name)
        def wipDataSetupMaintDetailsResponse = camstarService.getWipDataSetupMaintDetails(wipDataSetupMaintDetailsRequest)
        if (wipDataSetupMaintDetailsResponse.isSuccessful())
        {
            def wipDataSet = wipDataDomainObjectManager.getWipDataSet(name)
            if (wipDataSet == null)
            {
                wipDataSet = wipDataDomainObjectManager.createDomainObject(name)
                wipDataDomainObjectManager.addWipDataSet(wipDataSet)
            }
            else
            {
                wipDataSet.removeAllWipDataDomainObject()
            }
            //wipDataSet.setEquipmentId(cEquipment.getSystemId())            
            wipDataSet.setLotId(lotId)
            //wipDataSet.setServiceType(WipDataDomainObjectSet.SERVICE_TYPE_ADHOC_WIP_DATA)
            wipDataSet.setObjectType(WipDataDomainObjectSet.OBJECT_TYPE_EQUIPMENT)
            def items = wipDataSetupMaintDetailsResponse.getResponseData().getObjectChanges().getDetails().getDetailsItems()
            while (items.hasNext())
            {
                def wipDataItem = items.next()
                def wipDataItemName = wipDataItem.getWIPDataName().getName()
                logger.info("Adding wip data item '" + wipDataItemName + "'")
                
                def wipDataSetupName = wipDataSet.getWipDataDomainObject(wipDataItemName)
                if (wipDataSetupName == null)
                {
                    wipDataSetupName = new WipDataDomainObject(wipDataItemName)
                    wipDataSet.addWipDataDomainObject(wipDataSetupName)
                }
                wipDataSetupName.setIsHidden(PacUtils.valueOfBoolean(wipDataItem.getIsHidden(), false))
                wipDataSetupName.setIsRequired(PacUtils.valueOfBoolean(wipDataItem.getIsRequired(), false))
                wipDataSetupName.setIsHidden(PacUtils.valueOfBoolean(wipDataItem.getIsHidden(), false))
                wipDataSetupName.setIsRequired(PacUtils.valueOfBoolean(wipDataItem.getIsRequired(), false))
                wipDataSetupName.setLowerLimit(wipDataItem.getLowerLimit())
                wipDataSetupName.setMaxDataValue(wipDataItem.getMaxDataValue())
                wipDataSetupName.setMinDataValue(wipDataItem.getMinDataValue())
                wipDataSetupName.setServiceName(wipDataItem.getServiceName())
                wipDataSetupName.setUpperLimit(wipDataItem.getUpperLimit())
            }
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(wipDataSetupMaintDetailsResponse)
        }
    }
    
    public String queryPmRecipe(EquipmentPMDomainObject equipmentPm, String toolSpcSpecName, TscEquipment cEquipment)
    {
        def pmRecipe = ""
        def request = new GetRecipeMatrixMaintRequest()
        def objectChanges = request.getInputData().getObjectChanges()
        objectChanges.getSpec().setName(toolSpcSpecName)
        objectChanges.getSpec().setUseROR("true")
        objectChanges.getEquipment().setName(cEquipment.getSystemId())
        def reply = camstarService.getRecipeMatrixMaint(request)
        if (reply.isSuccessful())
        {
            def iterator = reply.getAllRecipeMatrixMaintRecord()
            while (iterator.hasNext())
            {
                def row = iterator.next().getRow()
                def requestDetails = new GetRecipeMatrixMaintDetailsRequest()
                requestDetails.getInputData().getObjectToChange().setName(row.getName())
                def detailsReply = camstarService.getRecipeMatrixMaintDetails(requestDetails)
                pmRecipe = detailsReply.getResponseData().getObjectChanges().getRecipe().getName()
                break
            }            
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
        equipmentPm.setPMRecipe(pmRecipe)
        return pmRecipe
    }
}