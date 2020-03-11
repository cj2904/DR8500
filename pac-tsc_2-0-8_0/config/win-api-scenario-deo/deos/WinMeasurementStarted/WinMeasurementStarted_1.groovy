package WinMeasurementStarted

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.camstar.semisuite.service.dto.ViewContainerStatusRequest
import de.znt.camstar.semisuite.service.dto.ViewContainerStatusResponse
import de.znt.pac.ItemNotFoundException
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager
import de.znt.services.secs.SecsGemService
import elemental.json.JsonObject
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.FppReclassQueryRequest
import sg.znt.camstar.semisuite.service.dto.GetProductMaintTscRequest
import sg.znt.camstar.semisuite.service.dto.GetProductMaintTscResponseDto.ResponseData.ObjectChanges
import sg.znt.camstar.semisuite.service.dto.GetProductMaintTscResponseDto.ResponseData.ObjectChanges.TscCrossProduct.TscCrossProductItem
import sg.znt.camstar.semisuite.service.dto.GetProductMaintTscResponseDto.ResponseData.ObjectChanges.TscProductClass.TscProductClassItem
import sg.znt.pac.domainobject.WaferClassificationDomainObject
import sg.znt.pac.domainobject.WaferClassificationDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.W02CamstarService
import sg.znt.services.zwin.ZWinApiOperation
import sg.znt.services.zwin.ZWinApiService
import sg.znt.services.zwin.schema.ZWinApiSchema

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class WinMeasurementStarted_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="CCamstarService")
    private W02CamstarService cCamstarService

    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="EventId")
    private String eventTid

    @DeoBinding(id="EventName")
    private String eventName

    @DeoBinding(id="WaferClassificationManager")
    private WaferClassificationDomainObjectManager wcManager

    @DeoBinding(id="ZWinApiSchema")
    private ZWinApiSchema zWinApiSchema

    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService
    
    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

    private CLot clot
    private String winReplyCode = ""
    private String winReplyMsg = ""
    private static final String MODELLING_PN_VAL = "PN_Val"

    /**
     *
     */
    @DeoExecute(result="Reply")
    public JsonObject execute()
    {
        cEquipment.setVirtualRun(false)
        // Clear Wafer Classification Domain Object Before Measurement Start
        if(wcManager.getAllDomainObject().size() >0)
        {
            List<WaferClassificationDomainObject> wcall = wcManager.getAllDomainObject()
            for(int i =0 ; i < wcall.size(); i++)
            {
                WaferClassificationDomainObject item = (WaferClassificationDomainObject) wcall[i]
                wcManager.removeDomainObject(item)
            }
        }

        ZWinApiService winApiService = ((ZWinApiService)secsGemService)
        Map<String, Object> param = new HashMap<String, Object>();
        // Create Lot in EAP By ViewContainerStatus As No Trackin ot TrackOut from Camstar
        def lotId = paramMap.get("LotId")
        if (lotId == null || lotId.length()==0)
        {
            return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", "Missing param 'LotId'!")
        }
        
        def operator = paramMap.get("EmployeeId")
        if (operator == null || operator.length()==0)
        {
            return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", "Missing param 'EmployeeId'!")
        }
        param.put("LotId",lotId)
        param.put("EquipmentId", cEquipment.getSystemId())
        
        def lotList = materialManager.getCLotList(new LotFilterAll())
        if (lotList.size()>0)
        {
            def lotIdInPac = lotList.get(0).getId()
            if (!lotId.equalsIgnoreCase(lotIdInPac))
            {
                throw new Exception("Lot '$lotIdInPac' found in material manager, please ensure there is no lot in pac persistance!")
            }
        }

        try
        {
            clot = materialManager.getCLot(lotId)
        }
        catch (ItemNotFoundException e)
        {
            clot = materialManager.createCLot(lotId)
            materialManager.addCLot(clot)
        }

        ViewContainerStatusRequest request = new ViewContainerStatusRequest(lotId)
        ViewContainerStatusResponse reply = cCamstarService.viewContainerStatus(request)
        if(reply.isSuccessful())
        {
            if (reply.getResponseData().getLotStatus().equalsIgnoreCase("CLOSED"))
            {
                def msg = "Lot '$lotId' status is 'CLOSED'! Cannot proceed with Wafer Classification"
                logger.error(msg)
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", msg)
            }
            clot.setQty(Integer.parseInt(reply.getResponseData().getQty()))
            clot.setQty2(Integer.parseInt(reply.getResponseData().getQty2()))
            clot.setProduct(reply.getResponseData().getProduct())
            clot.setProductRevision(reply.getResponseData().getProductRevision())
            clot.setOperatorId(operator)
        }
        else
        {
            materialManager.removeCLot(clot)
            CamstarMesUtil.handleNoChangeError(reply)
        }
        
        if (clot.getQty() == 0)
        {
            def msg = "Lot '$lotId' quantity is '0'! Cannot proceed with Wafer Classification"
            logger.error(msg)
            materialManager.removeCLot(clot)
            wcManager.removeAllDomainObject()
            return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", msg)
            
        }

        // Fill Lot With Product Class & Cross Product
        String crossprodarr = "";
        def reqProduct = new GetProductMaintTscRequest()
        reqProduct.getInputData().getObjectToChange().setName(clot.getProduct())
        reqProduct.getInputData().getObjectToChange().setRev(clot.getProductRevision())
        def replyProduct = cCamstarService.getProductMaintTsc(reqProduct)
        if(replyProduct.isSuccessful())
        {
            ObjectChanges obj = replyProduct.getResponseData().getObjectChanges()
            Iterator<TscProductClassItem> classitems = obj.getTscProductClass().getTscProductClassItems()
            while(classitems.hasNext())
            {
                // Fill Main Product Resistance Class
                TscProductClassItem item = (TscProductClassItem) classitems.next()
                WaferClassificationDomainObject wcm = wcManager.createNewDomainObject(item.getTscResistanceClass(),item.getTscNewProduct().getName(),item.getTscNewProduct().getRev(),"","",clot.getProduct(),clot.getProductRevision())
                wcManager.addDomainObject(wcm)
            }

            Iterator<TscCrossProductItem> crossitems = obj.getTscCrossProduct().getTscCrossProductItems()
            while(crossitems.hasNext())
            {
                //Fill Cross Product Resistance Class
                TscCrossProductItem itemCrossProduct = (TscCrossProductItem) crossitems.next()
                def reqCrossProduct = new GetProductMaintTscRequest()
                reqCrossProduct.getInputData().getObjectToChange().setName(itemCrossProduct.getTscCrossProduct().getName())
                reqCrossProduct.getInputData().getObjectToChange().setRev(itemCrossProduct.getTscCrossProduct().getRev())
                def replyCrossProduct = cCamstarService.getProductMaintTsc(reqCrossProduct)
                if(replyCrossProduct.isSuccessful())
                {
                    ObjectChanges objCrossProduct = replyCrossProduct.getResponseData().getObjectChanges()
                    Iterator<TscProductClassItem> crossProductClass = objCrossProduct.getTscProductClass().getTscProductClassItems()
                    while(crossProductClass.hasNext())
                    {
                        TscProductClassItem crossProductClassItem = (TscProductClassItem) crossProductClass.next()
                        WaferClassificationDomainObject wcm = wcManager.createNewDomainObject(crossProductClassItem.getTscResistanceClass(),crossProductClassItem.getTscNewProduct().getName(),crossProductClassItem.getTscNewProduct().getRev(),itemCrossProduct.getTscCrossProduct().getName(),itemCrossProduct.getTscCrossProduct().getRev(),clot.getProduct(),clot.getProductRevision())
                        wcManager.addDomainObject(wcm)
                    }
                }

                // Fill CLot Cross Product String Array Property
                String str = itemCrossProduct.getTscCrossProduct().getName() + ":" + itemCrossProduct.getTscCrossProduct().getRev()
                if(crossprodarr.length()!=0)
                {
                    str = "," + str
                }
                crossprodarr = crossprodarr + str
            }
            clot.getPropertyContainer().setStringArray("CrossProduct",crossprodarr.split(","))
        }
        else
        {
            materialManager.removeCLot(clot)
            CamstarMesUtil.handleNoChangeError(reply)
        }

        def fppReclassRequest = new FppReclassQueryRequest(lotId)
        def tscWaferResistance = ""
        def fppReclassReply = cCamstarService.getTscWaferResistance(fppReclassRequest)
        if (fppReclassReply.isSuccessful())
        {
            logger.info(Arrays.toString(fppReclassReply.getTscWaferResistance().toArray()))
            tscWaferResistance = fppReclassReply.getTscWaferResistance().get(0).trim()
        }
        else
        {
            materialManager.removeCLot(clot)
            wcManager.removeAllDomainObject()
            CamstarMesUtil.handleNoChangeError(reply)
        }

        if (tscWaferResistance.length()>0)
        {
            
            def pnValue = ""
            def thicknessMin = ""
            def thicknessMax = ""
            def requestProduct = new GetProductMaintTscRequest()
            requestProduct.getInputData().getObjectToChange().setName(clot.getProduct())
            requestProduct.getInputData().getObjectToChange().setRev(clot.getProductRevision())
            def replyProductMaint = cCamstarService.getProductMaintTsc(requestProduct)
            if(replyProductMaint.isSuccessful())
            {
                ObjectChanges obj = replyProductMaint.getResponseData().getObjectChanges()
                pnValue = obj.getFormatCode().trim()
                if (pnValue.length()==0)
                {
                    materialManager.removeCLot(clot)
                    wcManager.removeAllDomainObject()
                    return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", "'Format Code (PN Value)' field is empty! Please model a value in the Material Part >> Product Data >> Format Code!")
                }
                
                def thicknessValue = obj.getAlternateName().trim()
                def charPos = thicknessValue.lastIndexOf("-")
                thicknessMin = thicknessValue.substring(0, charPos)
                thicknessMax = thicknessValue.substring(charPos+1)
            }
            else
            {
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", "GetProductMaintTscRequest failed will error message:'" + replyProductMaint.getExceptionData().getErrorDescription() + "'!")
            }

            def gotoLoginScreenOpr = new ZWinApiOperation("GotoLoginScreen", zWinApiSchema)
            def result = zWinApiService.sendWinApiOperation(gotoLoginScreenOpr)
            if (!result.isSucessful())
            {
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
            }

            def recipeMap = new HashMap()
            recipeMap.put("Recipe", PacConfig.getStringProperty("Recipe.WaferClassification", "FPP03-WAFERSORTING"))
            def gotoSelectRecipeView = new ZWinApiOperation("GotoSelectRecipeView", zWinApiSchema, recipeMap)
            result = zWinApiService.sendWinApiOperation(gotoSelectRecipeView)
            if (!result.isSucessful())
            {
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
            }

            def uncheckAllCheckboxOpr = new ZWinApiOperation("UncheckAllBinCheckbox", zWinApiSchema)
            result = zWinApiService.sendWinApiOperation(uncheckAllCheckboxOpr)
            if (!result.isSucessful())
            {
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
            }

            String[] tscWaferResistanceList = tscWaferResistance.split("-")
            def startRange = PacUtils.valueOfDouble(tscWaferResistanceList[0].trim())
            List<WaferClassificationDomainObject> waferClassDomObjList = wcManager.getAllDomainObject()
            
            Map<Double, Double> map = new HashMap<>()
            for(int j=0; j<waferClassDomObjList.size(); j++)
            {
                map.putAt(waferClassDomObjList[j].getMin(), waferClassDomObjList[j].getMax())
            }
            List<Double> minValByKey = new ArrayList<>(map.keySet())
            Collections.sort(minValByKey)
            
            def startIndex = 0
            for (int k=0; k<minValByKey.size(); k++)
            {
                if (startRange == minValByKey.get(k))
                {
                    if (k>0)
                    {
                        startIndex = k-1
                    }
                    else
                    {
                        startIndex = k
                    }
                    break
                }
            }
            
            def thicknessMap = new HashMap()
            thicknessMap.put("thicknessBinMin", thicknessMin)
            thicknessMap.put("thicknessBinMax", thicknessMax)
            int startBinIndex = 1
            for (int m=startIndex; m<startIndex+5; m++)
            {
                for (int n=0; n<waferClassDomObjList.size(); n++)
                {
                    if (minValByKey.get(m) == waferClassDomObjList[n].getMin())
                    {
                        def resistanceMap = new HashMap()
                        resistanceMap.put("resistBinMin", waferClassDomObjList[n].getMin() + "")
                        resistanceMap.put("resistBinMax", waferClassDomObjList[n].getMax() + "")
                        def setResistivityOpr = new ZWinApiOperation("SetResistivityBin" + startBinIndex, zWinApiSchema, resistanceMap)
                        result = zWinApiService.sendWinApiOperation(setResistivityOpr)
                        if (!result.isSucessful())
                        {
                            materialManager.removeCLot(clot)
                            wcManager.removeAllDomainObject()
                            return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
                        }
                        
                        def setThicknessOpr = new ZWinApiOperation("SetThicknessBin" + startBinIndex, zWinApiSchema, thicknessMap)
                        result = zWinApiService.sendWinApiOperation(setThicknessOpr)
                        if (!result.isSucessful())
                        {
                            materialManager.removeCLot(clot)
                            wcManager.removeAllDomainObject()
                            return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
                        }
                        break
                    }
                }
                startBinIndex++;
            }

            def pnTypeMap = new HashMap()
            pnTypeMap.put("newPNVal", pnValue)
            def setPnTypeOpr = new ZWinApiOperation("SelectPnVal", zWinApiSchema, pnTypeMap)
            result = zWinApiService.sendWinApiOperation(setPnTypeOpr)
            if (!result.isSucessful())
            {
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
            }
            
            def saveEdittedRecipeOpr = new ZWinApiOperation("SaveEdittedRecipe", zWinApiSchema)
            result = zWinApiService.sendWinApiOperation(saveEdittedRecipeOpr)
            if (!result.isSucessful())
            {
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
            }
            
            def lotInfoMap = new HashMap()
            lotInfoMap.put("LotId", lotId)
            lotInfoMap.put("Operator", operator)
            lotInfoMap.put("Comment", tscWaferResistance)
            def setMeasurementInfoOpr = new ZWinApiOperation("SetMeasurementInfo", zWinApiSchema, lotInfoMap)
            result = zWinApiService.sendWinApiOperation(setMeasurementInfoOpr)
            if (!result.isSucessful())
            {
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
            }
            
            def recipeSelectOpr = new ZWinApiOperation("RecipeSelect", zWinApiSchema, recipeMap)
            result = zWinApiService.sendWinApiOperation(recipeSelectOpr)
            if (!result.isSucessful())
            {
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
            }
            
            def disableRecipeSelectOpr = new ZWinApiOperation("DisableRecipeSelectControl", zWinApiSchema)
            result = zWinApiService.sendWinApiOperation(disableRecipeSelectOpr)
            if (!result.isSucessful())
            {
                materialManager.removeCLot(clot)
                wcManager.removeAllDomainObject()
                return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", result.getErrorMessage())
            }
        }
        else
        {
            materialManager.removeCLot(clot)
            wcManager.removeAllDomainObject()
            return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", "TSCWAFERRESISTANCE from FPP RECLASS is empty for lot $lotId")
        }
        
        def dataToCollect = ""
        def schemaItems = mappingManager.getSchemaComponentByName("Measurement", "WaferClassification").getSchemaItems()
        if (schemaItems != null)
        {
            dataToCollect = schemaItems.get(0).getName()
        }
        else
        {
            materialManager.removeCLot(clot)
            wcManager.removeAllDomainObject()
            return winApiService.buildEventReplyMessage(eventTid, eventName, param, "99999", "Schema items is empty for Schema 'Measurement' Schema Components 'IQC'!")
        }
        
        param.put("DataToCollect", dataToCollect)
        param.put("ItemCount", clot.getQty())
        return winApiService.buildEventReplyMessage(eventTid, eventName, param, winReplyCode, winReplyMsg)
    }
}
