package WinMesTrackOut

import static sg.znt.pac.resources.i18n.TscI18n.localize
import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.TrackOutWIPMainWafersRequest
import sg.znt.pac.domainobject.PM
import sg.znt.pac.domainobject.PmManager
import sg.znt.pac.domainobject.Recipe
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem

@CompileStatic
@Deo(description='''
Track out a lot through Win Api Gateway
''')
class WinMesTrackOut_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="ParamMap")
    private Map<String, String> paramMap

    @DeoBinding(id="Equipment")
    private CEquipment equipment

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager
    
    @DeoBinding(id="PMManager")
    private PmManager pmManager
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager
    
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
        if(employeeId == null || employeeId.length()==0)
        {
            throw new Exception("Missing param 'EmployeeId'!")
        }
        def equipmentId = equipment.getSystemId()
        
        def message = ""
        def request = new TrackOutWIPMainWafersRequest(lotId, equipmentId)
       
        request.getInputData().setEmployee(employeeId)
        def waferItem = request.getInputData().getWafersDetails().addWafersDetailsItem()
        waferItem.setWaferScribeNumber(waferId)
        def response = cCamstarService.trackOutWafers(request)
        if (response.isSuccessful())
        {
            message = response.getResponseData().getCompletionMsg()
            logger.info("TrackOut [$waferId]: " + message)
        }
        else
        {          
            def errorCode = response.getExceptionData().getErrorCode()
            def erorDescription = response.getExceptionData().getErrorDescription()

            if(errorCode.equalsIgnoreCase("3080195"))
            {
                //Invalid XML Node: expected "nothing", received "__responseData
                message = "TrackOut [$waferId] OK: " + response.getResponseData().getCompletionMsg()
                logger.warn("TrackOut [$waferId]: TBC: Do not consider error for '" + erorDescription + "'")
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(response)
            }
            
            /**        
            if (response.getExceptionData().getErrorCode().equalsIgnoreCase("679484855"))
            {
                logger.info("Encounter wafer track out exception. Make sure it's still in the wip...")
                def requestInsertion = new GetModifyInsertionRequest()
                requestInsertion.getInputData().getContainer().setName(lotId)
                requestInsertion.getInputData().setSelectionId(lotId)
                requestInsertion.getInputData().getProcessType().setName(CCamstarService.DEFAULT_PROCESS_TYPE)
                def reply = cCamstarService.getModifyInsertion(requestInsertion)
                if (reply.isSuccessful())
                {
                    List<String> unprocessedWafer = new ArrayList<>()
                    def waferSelectionIt = reply.getResponseData().getWafersSelection().getWafersSelectionItems()
                    while (waferSelectionIt.hasNext())
                    {
                        def item = waferSelectionIt.next()
                        if (item.getWaferScribeNumber().equalsIgnoreCase(waferId))
                        {
                            logger.info("Wafer '" + item.getWaferScribeNumber() + "' status = '" + item.getWaferStatus())
                            if (!item.getWaferStatus().equalsIgnoreCase("INPROCESS"))
                            {
                                completeWafer(lotId, waferId)
                                performAbnormalCleanup(lotId, waferId, equipmentId)
                                break
                            }
                        }                        
                    }
                }
            }
            else
            {
                CamstarMesUtil.handleNoChangeError(response)                    
            }
            **/
        }    
        return message
    }    
    
    private performAbnormalCleanup(String lotId, String waferId, String equipmentId)
    {
        try 
        {
            def lot = materialManager.getCLot(lotId)
        
            def wafer = lot.getWafer(waferId)
            if (wafer == null)
            {
                logger.error("performAbnormalCleanup::: Wafer '" + waferId + "' is missing from lot '" + lotId + "'")
            }
            else
            {
                logger.info("performAbnormalCleanup::: Remove wafer '" + wafer.getId() + "' from lot '" + lotId + "'")
                lot.removeWafer(wafer)
                cleanupRecipe(equipmentId)
                cleanupPM(equipmentId)
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