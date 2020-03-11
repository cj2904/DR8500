package EqpRequestRejectData

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked
import sg.znt.camstar.semisuite.service.dto.LotRejectsInProcessRequest
import sg.znt.pac.SgdConfig
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.RecipeUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.zwin.ZWinApiException

@Deo(description='''
Request reject data from equipment
''')
class EqpRequestRejectData_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="TesterEquipment")
    private CEquipment testerEquipment

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment
    
	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager materialManager
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager
    
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
        def lotList = materialManager.getCLotList(new LotFilterAll())
        if(lotList.size() == 0)
        {
            def exception = new ZWinApiException("-4", "Lot not found")
            exception.setName(mainEquipment.getName())
            throw exception
        }
        def lot = lotList.get(0)
        def propertyContainer = lot.getPropertyContainer()
        
        def testerProp = testerEquipment.getPropertyContainer()
        def testerRecipe = RecipeUtil.getEqpRecipeToSelect(testerProp.getString("MesRecipe", ""), testerProp.getString("MesRecipeRev", ""), testerEquipment.getName())

    	S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("RequestRejectData"))
        def addParameter = request.getData().getParameterList().addParameter()
        addParameter.setCPName(new SecsAsciiItem("WorkOrder"))
        addParameter.setCPValue(new SecsAsciiItem(propertyContainer.getString("WorkOrder", "")))
        
        addParameter = request.getData().getParameterList().addParameter()
        addParameter.setCPName(new SecsAsciiItem("Recipe"))
        addParameter.setCPValue(new SecsAsciiItem(testerRecipe))
        
		S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)		
		
        def list = reply.getData().getParameterList()
        if (list.getSize()>0)
        {
            def rejectRequest = new LotRejectsInProcessRequest()
            def input = rejectRequest.getInputData()
            input.setContainer(lot.getId())
            input.setEquipment(mainEquipment.getSystemId())
            input.setProcessType("NORMAL")
            boolean containReject = false
            
            def totalRejectQty = 0
            for (int j=0;j<list.getSize();j++) 
            {
                def param = list.getParameter(j)
                if(param!= null)
                {
                    SecsAsciiItem value = (SecsAsciiItem) param.getCPAckComponent()
                    
                    if(value.getString().contains(":"))
                    {
                        String[] str = value.getString().split(":")
                        if(str.length > 1)
                        {
                            String lossReason = str[0]
                            String lossQty = Integer.parseInt(str[1])
                            def rejectQty = Integer.parseInt(lossQty)
                            totalRejectQty = totalRejectQty + rejectQty
                            if (rejectQty>0)
                            {
                                def mesReason = getMesMappingReasonCode(lossReason)
                                logger.info("Set lot reject quantity " + value.getString() + "|" + mesReason)
                                
                                def di = input.getDetails().addDetailsItem()
                                di.setLossReason(mesReason)
                                di.setRejectQty(String.valueOf(rejectQty))
                                containReject = true
                            }                            
                        }
                    }           
                }                
            }  
            def varianceRejectName = SgdConfig.getStringProperty("Variance.Name","Variance")
            if (varianceRejectName.length()>0)
            {
                def inputQty = lot.getPropertyContainer().getInteger("InputQty", 0)
                if (inputQty == 0)
                {
                    inputQty = lot.getQty()
                }
                
                /**
                def varianceReject = inputQty - totalRejectQty - lot.getTrackOutQty()
                //def varianceReject = lot.getQty() - totalRejectQty - lot.getTrackOutQty()
                if (varianceReject > 0)
                {
                    containReject = true
                    
                    def mesReason = getMesMappingReasonCode(varianceRejectName)
                    logger.info("Query variance reject info : " + lot.getQty() + "|" + lot.getPropertyContainer().getInteger("InputQty", 0) + "|" + totalRejectQty + "|" + lot.getTrackOutQty() + "|" + varianceReject + "|" + mesReason)
                    
                     def di = input.getDetails().addDetailsItem()
                     di.setLossReason(mesReason)
                     di.setRejectQty(String.valueOf(varianceReject))
                }
                **/
                def varianceReject = lot.getQty() - totalRejectQty - lot.getTrackOutQty()
                logger.debug("TrackInQty=" + lot.getTrackInQty() + ", totalRejectQty=" + totalRejectQty + ", TrackOutQty=" + lot.getTrackOutQty())
                lot.setPropertyKey(varianceRejectName, varianceReject+"")
            }

            lot.setRejectQty(totalRejectQty)            
            if (containReject)
            {
                def reasonReply = cCamstarService.lotRejectsInProcess(rejectRequest)
                if(reasonReply.isSuccessful())
                {
                    logger.info(reasonReply.getResponseData().getCompletionMsg())
                }
                else
                {
                    throw new Exception(reasonReply.getExceptionData().getErrorDescription())
                }
            }
        }		
    }
    
    private String getMesMappingReasonCode(String equipmentReasonCode)
    {
        def mappingName = SgdConfig.getStringProperty("RejectMappingName","")
        if (mappingName.length()==0)
        {
            logger.info("No reject mapping defined, return default reason code '" + equipmentReasonCode + "'")
            return equipmentReasonCode   
        }
        def mappingSet = mappingManager.getMappingSet(mappingName)
        if (mappingSet == null)
        {
            logger.info("Reject mapping '" + mappingName + "' is not found, return default reason code '" + equipmentReasonCode + "'")
            return equipmentReasonCode
        }
        def mappingList = mappingSet.getMappings()
        for (mapping in mappingList)
        {
            def sourceList = mapping.getSources()
            for (src in sourceList)
            {
                def srcSchemaItem = mappingManager.getSchemaItem(src.getSchemaId(), src.getSchemaComponentId(), src.getSchemaItemId())
                def target = mapping.getTarget()
                def tgtSchemaItem = mappingManager.getSchemaItem(target.getSchemaId(), target.getSchemaComponentId(), target.getSchemaItemId())
                
                if (equipmentReasonCode.equalsIgnoreCase(srcSchemaItem.getName()))
                {
                    return tgtSchemaItem.getName()
                }
            }
        }
        logger.info("No reject mapping found, return default reason code '" + equipmentReasonCode + "'")
        return equipmentReasonCode
    }
}