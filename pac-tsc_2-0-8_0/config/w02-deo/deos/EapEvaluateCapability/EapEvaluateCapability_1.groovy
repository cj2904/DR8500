package EapEvaluateCapability

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintResponse
import sg.znt.camstar.semisuite.service.dto.SetEqpProcessCapabilityRequest
import sg.znt.pac.exception.TrackInVirtualQtyException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.mes.ProcessCapability
import sg.znt.pac.mes.ProcessCapability.ProcessCapabilityParam
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''

''')
class EapEvaluateCapability_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService camstarService

    @DeoBinding(id="CEquipment")
    private CEquipment equipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml

    private int thruput2=0
    private CLot lot

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def req = new W02TrackInLotRequest(inputXml)
        def lotId = req.getContainerName()
        lot = cMaterialManager.getCLot(lotId)

        try
        {
            thruput2 = Integer.parseInt(req.getWaferQty())
        }
        catch (Exception e)
        {
            e.printStackTrace()
        }

        if (lot != null)
        {
            def capability = new ProcessCapability(lot.getProcessCapability())
            updateEquipmentCapability(capability, req)
        }
    }

    private void updateEquipmentCapability(ProcessCapability requireCapability, W02TrackInLotRequest outbound)
    {
        def deActvCapability = new ArrayList<String>()

        def wipDataList = outbound.getWipDataItemList()
        def realRunQty = -1
        int trackInWaferQty = Integer.parseInt(outbound.getWaferQty())
        if (wipDataList.size()>0)
        {
            for (var in wipDataList)
            {
                if (var.WIP_DATA_NAME.contains("Real Run Qty"))
                {
                    realRunQty = PacUtils.valueOfInteger(var.WIP_DATA_VALUE, -1)
                }
            }
        }


        def allRecipe = lot.getAllRecipeObj()
        for (recipe in allRecipe)
        {
            def runCount = ""
            logger.info("Recipe '" + recipe.getRecipeName() + "' has the run count of '$runCount'")

            if (realRunQty > -1)
            {
                if (trackInWaferQty>realRunQty)
                {
                    def recipeParams = recipe.getAllParam()
                    boolean rangeFound = false
                    int totalThruput = 0
                    for (recipeParam in recipeParams)
                    {
                        if (recipeParam.getParameterName().matches("[Rr][_]\\d+[-]{1}\\d+"))
                        {
                            String thruput2Str = recipe.getThruput2();
                            if (thruput2Str.length() == 0)
                            {
                                logger.error("Missing thruput PM since PM parameter '" + recipeParam.getParameterName() + "' is defined!");
                            }
                            try
                            {
                                totalThruput = Integer.parseInt(thruput2Str) + (realRunQty > 0 ? realRunQty : 0)
                                logger.info("Recipe='" + recipe.getRecipeName() + "' ThuruputTotal=" + totalThruput + " | thruput2Str=" + thruput2Str + " | RealRunQty=" + realRunQty)
                                String[] range = recipeParam.getParameterName().replaceAll("R_", "").split("-")
                                int min = Integer.parseInt(range[0])
                                int max = Integer.parseInt(range[1])
                                int minbuffer = PacConfig.getIntProperty("RecipeRangeThruputMinBuffer", 0)
                                int maxbuffer = PacConfig.getIntProperty("RecipeRangeThruputMaxBuffer", 0)
                                if (totalThruput >= (min - minbuffer) && totalThruput <= (max + maxbuffer))
                                {
                                    rangeFound = true
                                    runCount = recipeParam.getParameterValue()
                                    break
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace()
                            }
                        }
                    }
//                    if (!rangeFound)
//                    {
//                        throw new Exception("Range of total thruput '$totalThruput' not found in recipe '" + recipe.getRecipeName() + "'!")
//                    }
                }
            }
            if (runCount.length()==0)
            {
                runCount = recipe.getUsedRecipeName()
            }

            def params = recipe.getAllParam()
            def sameCapabilityConst = "SameCapability."
            for (param in params)
            {
                def paramName = param.getParameterName()
                if(paramName.startsWith(sameCapabilityConst) && param.getParameterValue().equalsIgnoreCase("TRUE"))
                {
                    def sameCapabilityRunCount = paramName.replace(sameCapabilityConst, "")
                    if (runCount.equalsIgnoreCase(sameCapabilityRunCount))
                    {
                        def req = new GetEquipmentMaintRequest()
                        req.getInputData().getObjectToChange().setName(lot.getEquipmentId())
                        req.getRequestData().getObjectChanges().initChildParameter("tscLastRunCapability")
                        GetEquipmentMaintResponse res = new GetEquipmentMaintResponse()
                        res.getResponseData().getObjectChanges().initChildParameter("tscLastRunCapability")
                        res = camstarService.getEquipmentMaint(req)
                        if (res.isSuccessful())
                        {
                            def lastRunCapability = res.getResponseData().getObjectChanges().getChildParameter("tscLastRunCapability").getValue()
                            logger.info("Last Run Capability:'$lastRunCapability'")
                            def list = lastRunCapability.tokenize(";")
                            if (list.size() < 1)
                            {
                                lastRunCapability = ""
                            }
                            else
                            {
                                lastRunCapability = list.get(0)
                            }

                            def curLotCapability = lot.getProcessCapability()
                            logger.info("Current Lot Capability:'$curLotCapability'")

                            if (!lastRunCapability.equalsIgnoreCase(curLotCapability))
                            {
                                throw new Exception("Last Run Capability:'$lastRunCapability' is not matched with Current Lot Capability:'$curLotCapability' for Run:'$runCount'!")
                            }
                        }
                        else
                        {
                            throw new Exception("Error in GetEquipmentMaintRequest: " + res.getExceptionData().getErrorDescription())
                        }
                    }
                }

                if (paramName.matches("(.*)\\.DeActv"))
                {
                    def capabilityNameDeActv = paramName.replace(".DeActv", "")
                    def runDeActv = param.getParameterValue()
                    if (runDeActv.equalsIgnoreCase(runCount))
                    {
                        deActvCapability.add(capabilityNameDeActv)
                    }
                }
            }
        }
        if (realRunQty > -1)
        {
            if (trackInWaferQty>realRunQty)
            {
            }
        }



        def request = new GetEquipmentMaintRequest(lot.getEquipmentId())
        def reply = camstarService.getEquipmentMaint(request)
        ArrayList<ProcessCapability> capabilityList = new ArrayList()
        if (reply.isSuccessful())
        {
            def items = reply.getResponseData().getObjectChanges().getEqpProcessCapability().getEqpProcessCapabilityItems()
            while(items.hasNext())
            {
                def item = items.next()
                ProcessCapability capability = new ProcessCapability(item.getProcessCapability().getName())
                capabilityList.add(capability)
            }
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
        requireCapability.updateCapabilityList(capabilityList)

        //get thruput from outbout and add current outbound
        //MZ //SZ
        def accumalatedThruput = equipment.getThruput2AtTrackIn()+thruput2

        def request2 = new SetEqpProcessCapabilityRequest()
        request2.getInputData().setResource(lot.getEquipmentId())
        for (var in capabilityList)
        {
            logger.info("EvaCap isActivated " +  var.isActivated())
            ArrayList<ProcessCapabilityParam> params = var.getCapabilityParams();
            for (ProcessCapabilityParam param : params)
            {
                logger.info("EvaCap rule " +  param.getCapabilityRule())
                logger.info("EvaCap paramname " +  param.getParamName())
                logger.info("EvaCap paramvalue" +  param.getParamValue())
            }
            def item = request2.getInputData().getDetails().addDetailsItem()
            item.setAvailability("TRUE")
            if (var.isActivated())
            {
                logger.info("EvaCap getMaxThruput " +  var.getMaxThruput())
                logger.info("EvaCap getMinThruput " +  var.getMinThruput())
                logger.info("EvaCap accumalatedThruput " +  accumalatedThruput)
                logger.info("EvaCap equipment.getThruput2WarningQty() " +  equipment.getThruput2WarningQty())

                if (var.getMaxThruput()==-1 && var.getMinThruput()==-1)
                {
                    item.setActivationStatus("TRUE")
                }
                else
                {
                    if (var.getMaxThruput()!=-1)
                    {
                        if ((accumalatedThruput+equipment.getThruput2WarningQty())>var.getMaxThruput())
                        {
                            logger.info("EvaCap 1 setActivationStatus false")
                            item.setActivationStatus("FALSE")
                        }
                        else
                        {
                            item.setActivationStatus("TRUE")
                        }
                    }
                    else if (accumalatedThruput>=var.getMinThruput())
                    {
                        if (accumalatedThruput>=var.getMinThruput())
                        {
                            item.setActivationStatus("TRUE")
                        }
                        else
                        {
                            logger.info("EvaCap 2 setActivationStatus false")
                            item.setActivationStatus("FALSE")
                        }
                    }
                }

                for (deActvItem in deActvCapability)
                {
                    if (var.getName().equalsIgnoreCase(deActvItem.toString()))
                    {
                        logger.info("DeActivate capability for '" + var.getName() + "' modelled in recipe parameter!")
                        item.setActivationStatus("FALSE")
                        break
                    }
                }
            }
            else
            {
                logger.info("EvaCap 3 setActivationStatus false")
                item.setActivationStatus("FALSE")
            }
            item.getProcessCapability().setName(var.getName())
        }

        def reply2=camstarService.setEqpProcessCapability(request2)
        if (reply2.isSuccessful())
        {
            logger.info(reply2.getResponseData().getCompletionMsg())
        }
        else
        {
            CamstarMesUtil.handleNoChangeError(reply2)
        }

    }
}