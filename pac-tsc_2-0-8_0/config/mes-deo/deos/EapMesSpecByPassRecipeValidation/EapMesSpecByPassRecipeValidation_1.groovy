package EapMesSpecByPassRecipeValidation

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.Recipe
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.RecipeParameter
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.camstar.outbound.W02TrackInLotRequest.PM
import de.znt.pac.deo.annotations.Deo
import de.znt.pac.deo.annotations.DeoBinding
import de.znt.pac.deo.annotations.DeoExecute
import de.znt.pac.mapping.MappingManager

@CompileStatic
@Deo(description='''
Select recipe id at modbus equipment
''')
class EapMesSpecByPassRecipeValidation_1
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXml")
    private String inputXml


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new W02TrackInLotRequest(inputXml)
        def equipmentId = request.getResourceName()
        def trackInLotId = request.getContainerName()
        def trackInLotRecipeName = request.getRecipeName()
        def trackInLotRecipeVersion = request.getRecipeRevision()

        //def trackInLotRecipe = getTrackInLotUsedRecipe(equipmentId,trackInLotId,trackInLotRecipeName)

        def selectRecipeValue = ""

        def allRecipe = getRecipeList()
        for (recipe in allRecipe)
        {
            logger.info("mainexe lot="+trackInLotId+" recipeName " + recipe.getRecipeName())
            logger.info("mainexe lot="+trackInLotId+" getUsedRecipeName " + recipe.getUsedRecipeName())

            if (!recipe.getRecipeName().equals(recipe.getUsedRecipeName()))
            {
                if (recipe.getFixRecipe() !=null && recipe.getFixRecipe().trim().length()>0)
                {
                    //selectRecipeValue= recipe.getFixRecipe()
                    selectRecipeValue = recipe.getParamFinalValue("Duration2")
                    logger.info("mainexe 1 lot="+trackInLotId+" selectRecipeValue " + selectRecipeValue)
                }
                else
                {
                    selectRecipeValue = recipe.getUsedRecipeName()
                    logger.info("mainexe 2 lot="+trackInLotId+" selectRecipeValue " + selectRecipeValue)
                }
            }
            else
            {
                if (!recipe.containSubRecipe())
                {
                    if (recipe.getFixRecipe() !=null && recipe.getFixRecipe().trim().length()>0)
                    {
                        //selectRecipeValue= recipe.getFixRecipe()
                        selectRecipeValue = recipe.getParamFinalValue("Duration2")
                        logger.info("mainexe 3 lot="+trackInLotId+" selectRecipeValue " + selectRecipeValue)
                    }
                    else
                    {
                        selectRecipeValue = recipe.getUsedRecipeName()
                        logger.info("mainexe 4 lot="+trackInLotId+" selectRecipeValue " + selectRecipeValue)
                    }
                }
            }

            logger.info("mainexe 5 lot="+trackInLotId+" selectRecipeValue " + selectRecipeValue)

            def allLot = materialManager.getCLotList(new LotFilterAll())
            if(allLot.size()>=1)
            {
                for (lot in allLot)
                {
                    logger.info("EapMesSpecByPass lot="+lot.getId()+" mainEqpId=" + lot.getEquipmentId() + " trackInLotId="+trackInLotId+" mainEqpId="+equipmentId+"")
                    logger.info("EapMesSpecByPass lot="+lot.getId()+" mainRecipe=" + lot.getRecipe() + " trackInLotId="+trackInLotId+" mainRecipe="+trackInLotRecipeName+"")
                    logger.info("EapMesSpecByPass lot="+lot.getId()+" mainRecipeVersion=" + lot.getRecipeVersion() + " trackInLotId="+trackInLotId+" mainRecipeVersion="+trackInLotRecipeVersion+"")

                    if (lot.getEquipmentId().equalsIgnoreCase(equipmentId))
                    {
                        def existingLotRecipe = getLotUsedRecipe(lot, recipe.getRecipeName())

                        logger.info("EapMesSpecByPassRecipeValidation compare lot="+lot.getId()+" lot recipname " +existingLotRecipe+ " trackinlot recipeName " + selectRecipeValue )
                        if (existingLotRecipe.length()>0 && selectRecipeValue != existingLotRecipe)
                        {
                            def errmsg = "Existing Lot ["+lot.getId()+"] Recipe '$existingLotRecipe' NOT SAME with Current TrackIn Lot ["+trackInLotId+"] TrackIn Recipe [" + selectRecipeValue + "]"
                            logger.info(errmsg)
                            throw new Exception(errmsg)
                        }
                    }
                }
            }
        }

    }

    public String getLotUsedRecipe(CLot lot, String recipeName)
    {
        def selectRecipeValue = ""

        def allRecipe = lot.getAllRecipeObj()
        for (recipe in allRecipe)
        {
            logger.info("W02SpecByPass lot="+lot.getId()+" lot recipname " +recipe.getRecipeName()+ " trackinlot recipeName " + recipeName )

            if(recipe.getRecipeName().equalsIgnoreCase(recipeName))
            {
                if (!recipe.getRecipeName().equals(recipe.getUsedRecipeName()))
                {
                    if (recipe.getFixRecipe() !=null && recipe.getFixRecipe().trim().length()>0)
                    {
                        //selectRecipeValue= recipe.getFixRecipe()
                        selectRecipeValue = recipe.getParamFinalValue("Duration2")
                        logger.info("W02SpecByPass 1 lot="+lot.getId()+" selectRecipeValue " + selectRecipeValue)
                    }
                    else
                    {
                        selectRecipeValue = recipe.getUsedRecipeName()
                        logger.info("W02SpecByPass 2 lot="+lot.getId()+" selectRecipeValue " + selectRecipeValue)
                    }
                }
                else
                {
                    if (!recipe.containSubRecipe())
                    {
                        if (recipe.getFixRecipe() !=null && recipe.getFixRecipe().trim().length()>0)
                        {
                            //selectRecipeValue= recipe.getFixRecipe()
                            selectRecipeValue = recipe.getParamFinalValue("Duration2")
                            logger.info("W02SpecByPass 3 lot="+lot.getId()+" selectRecipeValue " + selectRecipeValue)
                        }
                        else
                        {
                            selectRecipeValue = recipe.getUsedRecipeName()
                            logger.info("W02SpecByPass 4 lot="+lot.getId()+" selectRecipeValue " + selectRecipeValue)
                        }
                    }
                }
                logger.info("W02SpecByPass found lot="+lot.getId()+" selectRecipeValue " + selectRecipeValue)
                return selectRecipeValue
            }
        }

        logger.info("W02SpecByPass 5 lot="+lot.getId()+" selectRecipeValue " + selectRecipeValue)
        return selectRecipeValue
    }


    public List<Recipe> getRecipeList()
    {
        List<Recipe> rlist = new ArrayList<Recipe>()

        def request = new W02TrackInLotRequest(inputXml)
        if (request.getRecipeName().length()==0)
        {
            return rlist
        }

        def mainRecipe = recipeManager.createDomainObject(request.getResourceName() + "-" + request.getRecipeName())
        def paramList = request.getRecipeParamList();
        mainRecipe.setRecipeName(request.getRecipeName())
        mainRecipe.setMainRecipeName(request.getRecipeName())
        mainRecipe.setRecipeRevision(request.getRecipeRevision())
        mainRecipe.setEquipmentId(request.getResourceName())
        mainRecipe.setMainEquipmentId(request.getResourceName())
        mainRecipe.setIsSubRecipe("FALSE")
        mainRecipe.setLastCapability(request.getLastProcessCapability())
        mainRecipe.setRequireCapability(request.getProcessCapability())

        def PMList = request.getPMList()
        for (pm in PMList)
        {
            if (request.getThruputRequirement().equalsIgnoreCase(pm.PM_NAME))
            {
                mainRecipe.setThruput(pm.PM_THRUPUT_QTY)
                mainRecipe.setThruput2(pm.PM_THRUPUT_QTY2)
            }
        }

        for (recipeParam in paramList)
        {
            def param = new RecipeParameter(recipeParam.getParamName())
            param.setMaxValue(recipeParam.getMaxDataValue())
            param.setMinValue(recipeParam.getMinDataValue())
            param.setDataType(recipeParam.getFieldType())
            param.setParameterValue(recipeParam.getParamValue())
            param.setValidValues(recipeParam.getValidValues())
            mainRecipe.addElement(param)
        }

        rlist.add(mainRecipe)

        def subRecipeList = request.getSubRecipeList()

        if (subRecipeList.size()>0)
        {
            mainRecipe.setContainSubRecipe(true)
        }

        for (subRecipe in subRecipeList)
        {
            def ceqId = getEquipmentId(request, subRecipe.getEquipmentLogicalId())
            def subRecipeObj = recipeManager.createNewDomainObject(ceqId + "-" + subRecipe.getName())
            subRecipeObj.setRecipeRevision(subRecipe.getRevision())
            subRecipeObj.setRecipeName(subRecipe.getName())
            subRecipeObj.setEquipmentId(ceqId)
            subRecipeObj.setEquipmentLogicalId(subRecipe.getEquipmentLogicalId())
            subRecipeObj.setMainEquipmentId(request.getResourceName())
            subRecipeObj.setIsSubRecipe("TRUE")
            subRecipeObj.setSequence(subRecipe.getSequence())
            subRecipeObj.setLastCapability(request.getLastProcessCapability())
            subRecipeObj.setRequireCapability(request.getProcessCapability())
            subRecipeObj.setMainRecipeName(request.getRecipeName())
            def pm = getChildEquipmentThruput(request, ceqId)
            if (pm !=null)
            {
                subRecipeObj.setThruput(pm.PM_THRUPUT_QTY)
                subRecipeObj.setThruput2(pm.PM_THRUPUT_QTY2)
            }

            def params = subRecipe.getRecipeParams()

            for (recipeParam in params)
            {
                def param = new RecipeParameter(recipeParam.getParamName())
                param.setMaxValue(recipeParam.getMaxValue())
                param.setMinValue(recipeParam.getMinValue())
                param.setDataType(recipeParam.getFieldType())
                param.setParameterValue(recipeParam.getParamValue())
                param.setValidValues(recipeParam.getValidValues())
                subRecipeObj.addElement(param)
            }

            //here to add material manager lot track n qty
            def totalqty = Integer.parseInt(request.getTrackInQty())
            def getCLotList = materialManager.getCLotList(new LotFilterAll())
            for(lot in getCLotList)
            {
                if(lot.getEquipmentId().equalsIgnoreCase(request.getResourceName()))
                {
                    totalqty = lot.getTrackInQty()
                }
            }
            subRecipeObj.setTrackInQty(totalqty)

            rlist.add(subRecipeObj)
        }

        return rlist
    }

    public String getEquipmentId(W02TrackInLotRequest request,String logicalId)
    {
        def ceqList = request.getChildEquipmentList()
        for (ceq in ceqList)
        {
            if (ceq.CHILD_EQUIPMENT_LOGICAL_ID.equalsIgnoreCase(logicalId))
            {
                return ceq.CHILD_EQUIPMENT_ID
            }
        }
        return ""
    }

    public PM getChildEquipmentThruput(W02TrackInLotRequest request,String childEqId)
    {
        def ceqList = request.getChildEquipmentList()
        for (ceq in ceqList)
        {
            if (ceq.CHILD_EQUIPMENT_ID.equalsIgnoreCase(childEqId))
            {
                def requirement = ceq.CEQ_THRUPUT_REQUIREMENT
                def pmList = ceq.PM_LIST
                for(pm in pmList)
                {
                    if (pm.PM_NAME.equalsIgnoreCase(requirement))
                    {
                        return pm
                    }
                }
            }
        }
        return null
    }
}
