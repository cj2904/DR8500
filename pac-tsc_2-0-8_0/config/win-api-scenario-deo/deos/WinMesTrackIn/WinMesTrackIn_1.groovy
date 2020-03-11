package WinMesTrackIn

import static sg.znt.pac.resources.i18n.TscI18n.localize
import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.TrackInWIPMainWafersRequest
import sg.znt.pac.domainobject.PM
import sg.znt.pac.domainobject.PmManager
import sg.znt.pac.domainobject.Recipe
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.material.WaferFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem

@CompileStatic
@Deo(description='''
Perform MES track in from Win API gateway
''')
class WinMesTrackIn_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap

    @DeoBinding(id="Equipment")
    private CEquipment equipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
    
    @DeoBinding(id="PMManager")
    private PmManager pmManager
    
    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager
    
    /**
     *
     */
    @DeoExecute(result="Message")
    public String execute()
    {
        def lotId = paramMap.get("LotId")
        if (lotId == null || lotId.length()==0)
        {
            throw new Exception("Missing param 'LotId'!")
        }
        def waferId = paramMap.get("WaferId")
        if (waferId == null || waferId.length()==0)
        {
            throw new Exception("Missing param 'WaferId'!")
        }
        
        def employeeId = paramMap.get("EmployeeId")
        if(employeeId == null || employeeId.length() == 0)
        {
            throw new Exception("Missing param 'EmployeeId'!")
        }
        
        def equipmentId = equipment.getSystemId()
        
        def processingWafer = getProcessingWafer(equipmentId)
        if (processingWafer.length()>0)
        {
            if (processingWafer.length()>0)
            {
                throw new Exception("There is wafer [$processingWafer] pending for track out! Please track out the wafers first!")
            }
        }
        
        def message = ""
        def request = new TrackInWIPMainWafersRequest(lotId, equipmentId)
        def waferItem = request.getInputData().getWafersDetails().addWafersDetailsItem()
        waferItem.setContainer(lotId)
        waferItem.setWaferScribeNumber(waferId)
        //TODO: set employee id
        request.getInputData().setEmployee(employeeId)
        logger.info("TrackIn [$waferId]: Request to track in wafer...")
        def response = cCamstarService.trackInWafers(request)
        if (response.isSuccessful())
        {
            message = response.getResponseData().getCompletionMsg()
            logger.info("TrackIn [$waferId]: Success: " + message)
        }
        else
        {
            def errorCode = response.getExceptionData().getErrorCode()
            def erorDescription = response.getExceptionData().getErrorDescription()
                        
            if (erorDescription.equalsIgnoreCase(localize("MesPrepareLotSetEquipmentRecipe.Notification.Error.RecipeChange")))
            {
                response = cCamstarService.trackInWafers(request)
                if (response.isSuccessful())
                {
                    logger.info(response.getResponseData().getCompletionMsg())
                }
                else
                {
                    CamstarMesUtil.handleNoChangeError(response)
                }
            }
            else
            {
                logger.error("TrackIn [$waferId]: Fail track in with error: " + erorDescription)
                CamstarMesUtil.handleNoChangeError(response)
            }
        
            /**
            else if (errorCode.equalsIgnoreCase("679484855"))
            {
                //TrackInLot_E0621: Item Id 019 of lot D60100B-I30755-B0 is currently processing in equipment PRB34
                //TrackInLot_E0001: D60100B-I30755-B0 is currently not Active
                if (erorDescription.indexOf("TrackInLot_E0621") > -1)
                {
                    message = response.getResponseData().getCompletionMsg()
                    logger.info("TrackIn [$waferId]: Warn: " + erorDescription)
                }                
                else
                {
                    logger.error("TrackIn [$waferId]: Fail track in with error: " + erorDescription)
                    performAbnormalCleanup(lotId, waferId, equipmentId)
                    CamstarMesUtil.handleNoChangeError(response)
                }
            }
            **/
        }    
        return message
    }
    
    private String getProcessingWafer(String eqpId)
    {
        def wafers = ""
        try
        {
            def lotList = materialManager.getCLotList(new LotFilterAll())
            for (lot in lotList)
            {
                def wafersList = lot.getWaferList(new WaferFilterAll())
                for (wafer in wafersList)
                {
                    if (wafer.getEquipmentId().equalsIgnoreCase(eqpId))
                    {
                        if (wafers.length()>0)
                        {
                            wafers = wafers + ","
                        }
                        wafers = wafers + wafer.getWaferScribeID()
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace()
        }
        return wafers
    }
    
    private void completeWafer(String lotId, String waferId)
    {
        try
        {
            SecsGemService secsGemService = (SecsGemService) equipment.getExternalService()
            S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("CompleteLot"))
            def parameter = request.getData().getParameterList().addParameter()
            parameter.setCPName(new SecsAsciiItem("LotId"))
            parameter.setCPValue(new SecsAsciiItem(lotId))
            
            parameter = request.getData().getParameterList().addParameter()
            parameter.setCPName(new SecsAsciiItem("WaferId"))
            parameter.setCPValue(new SecsAsciiItem(waferId))
            
            parameter = request.getData().getParameterList().addParameter()
            parameter.setCPName(new SecsAsciiItem("EquipmentId"))
            parameter.setCPValue(new SecsAsciiItem(equipment.getSystemId()))
            
            parameter = request.getData().getParameterList().addParameter()
            parameter.setCPName(new SecsAsciiItem("Message"))
            parameter.setCPValue(new SecsAsciiItem(""))
    
            def reply = secsGemService.sendS2F41HostCommandSend(request)
            if(reply.isCommandAccepted())
            {
                //OK
            }
        }
        catch (Exception e)
        {
            e.printStackTrace()
        }
    }
    
    private performAbnormalCleanup(String lotId, String waferId, String equipmentId)
    {
        try
        {
            def lot = materialManager.getCLot(lotId)
        
            def wafer = lot.getWafer(waferId)
            if (wafer == null)
            {
                logger.error("TrackIn [$waferId]: performAbnormalCleanup::: Wafer '" + waferId + "' is missing from lot '" + lotId + "'")
            }
            else
            {
                logger.info("TrackIn [$waferId]: performAbnormalCleanup::: Remove wafer '" + wafer.getId() + "' from lot '" + lotId + "'")
                lot.removeWafer(wafer)
                cleanupRecipe(equipmentId)
                cleanupPM(equipmentId)
                completeWafer(lotId, waferId)
            }
        }
        catch(ItemNotFoundException e)
        {
            logger.info("Lot '" + lotId + "' is not found, skip performAbnormalCleanup")
        }
        catch (Exception e)
        {
            e.printStackTrace()
        }
        
    }
    
    private void cleanupRecipe(String eqpId)
    {
        logger.info("Clean up recipe for '" + eqpId + "'")
        def allRecipe = recipeManager.getAllDomainObject()
        ArrayList<Recipe> arrCleanUp = new ArrayList()
        for (recipe in allRecipe)
        {
            if (recipe.getEquipmentId().equalsIgnoreCase(eqpId))
            {
                arrCleanUp.add(recipe)
            }
        }
        for (recipe in arrCleanUp)
        {
            try
            {
                logger.info("Removing recipe '" + recipe.getId() + " from $eqpId")
                recipeManager.removeDomainObject(recipeManager.getDomainObject(recipe.getId()))
            }
            catch (Exception e)
            {
                e.printStackTrace()
            }
        }
    }
    
    private void cleanupPM(String eqpId)
    {
        logger.info("Clean up PM for '" + eqpId + "'")
        def allPm = pmManager.getAllDomainObject()
        ArrayList<PM> arrCleanUp = new ArrayList()
        for (pm in allPm)
        {
            if (pm.getEquipmentId().equalsIgnoreCase(eqpId))
            {
                arrCleanUp.add(pm)
            }
        }
        for (pm in arrCleanUp)
        {
            try
            {
                logger.info("Removing PM '" + pm.getId() + " from $eqpId")
                pmManager.removeDomainObject(pmManager.getDomainObject(pm.getId()))
            }
            catch (Exception e)
            {
                e.printStackTrace()
            }
        }
    }
}