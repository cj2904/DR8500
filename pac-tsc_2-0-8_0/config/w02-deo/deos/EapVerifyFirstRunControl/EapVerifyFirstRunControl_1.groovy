package EapVerifyFirstRunControl

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.RecipeParameter
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.camstar.outbound.W02TrackInLotRequest.PM
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify track in qty again first run
control qty
''')
class EapVerifyFirstRunControl_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="InputXml")
    private String inputXml
	
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	def request = new W02TrackInLotRequest(inputXml)
		addRecipeInformation(request)
    }
	
	public void addRecipeInformation(W02TrackInLotRequest request)
	{
		if (request.getRecipeName().length()==0)
		{
			return
		}
        def lotId = request.getContainerName()
        def cLot = cMaterialManager.getCLot(lotId)
        def batchTrackInQty = cLot.getPropertyContainer().getInteger("BatchTotalQty", 0);
		def trackInWaferQty = Integer.parseInt(request.getWaferQty())
        if(batchTrackInQty>0)
        {
            trackInWaferQty = batchTrackInQty
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
        mainRecipe.setTrackInQty(trackInWaferQty)

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
		
		def PMList = request.getPMList()
		def mainThruput = 0
		for (pm in PMList)
		{
			if (request.getThruputRequirement().equalsIgnoreCase(pm.PM_NAME))
			{
				if (Integer.parseInt(pm.PM_THRUPUT_QTY2)>0)
				{
					if (trackInWaferQty<=mainRecipe.getFirstRunControl())
					{
						//TODO: make i18n
						throw new Exception("Track in wafer qty $trackInWaferQty must run at first run, please perform change acid!")
					}
				}
			}
		}

		//recipeManager.addDomainObject(mainRecipe)
		
		def subRecipeList = request.getSubRecipeList()

		for (subRecipe in subRecipeList)
		{
			def subRecipeObj = recipeManager.createNewDomainObject(subRecipe.getName())
			subRecipeObj.setRecipeRevision(subRecipe.getRevision())
			subRecipeObj.setRecipeName(subRecipe.getName())
			subRecipeObj.setEquipmentLogicalId(subRecipe.getEquipmentLogicalId())
			subRecipeObj.setMainEquipmentId(request.getResourceName())
			subRecipeObj.setIsSubRecipe("TRUE")
			subRecipeObj.setSequence(subRecipe.getSequence())
			subRecipeObj.setLastCapability(request.getLastProcessCapability())
			subRecipeObj.setMainRecipeName(request.getRecipeName())
            subRecipeObj.setTrackInQty(trackInWaferQty)
            
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
			
			def pm = getChildEquipmentThruput(request, subRecipe.getEquipmentLogicalId())
			if (pm!=null && Integer.parseInt(pm.PM_THRUPUT_QTY2)>0)
			{
				if (trackInWaferQty<=subRecipeObj.getFirstRunControl())
				{
					//TODO: make i18n
					throw new Exception("Track in wafer qty $trackInWaferQty must run at first run, please perform change acid!")
				}
			}
		}
	}
	
	public PM getChildEquipmentThruput(W02TrackInLotRequest request,String childEqId)
	{
		def ceqList = request.getChildEquipmentList()
		for (ceq in ceqList)
		{
			if (ceq.CHILD_EQUIPMENT_LOGICAL_ID.equalsIgnoreCase(childEqId))
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