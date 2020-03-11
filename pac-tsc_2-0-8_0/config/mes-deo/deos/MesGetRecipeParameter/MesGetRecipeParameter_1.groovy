package MesGetRecipeParameter

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.camstar.semisuite.service.dto.GetRecipeMaintRequest;
import sg.znt.camstar.semisuite.service.dto.GetRecipeMaintResponseDto.ResponseData.ObjectChanges.DocumentParameters.DocumentParametersItem
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.CCamstarService

@Deo(description='''
Get recipe parameter from Camstar
''')
class MesGetRecipeParameter_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
    	def lotList = cMaterialManager.getCLotList(new LotFilterAll())
		CLot lot = lotList.get(0)
		GetRecipeMaintRequest request = new GetRecipeMaintRequest()
		request.getInputData().getObjectToChange().setName(lot.getRecipe())
		request.getInputData().getObjectToChange().setRev(lot.getRecipeVersion() + "")
		
		def reply = cCamstarService.getRecipeMaint(request)
		if(reply.isSuccessful())
		{
			Iterator<DocumentParametersItem> items = reply.getResponseData().getObjectChanges().getDocumentParameters().getDocumentParametersItems()
			for (item in items)
			{
				def maxLimitExist = ((DocumentParametersItem) item).getMaxDataValue() != null && ((DocumentParametersItem) item).getMaxDataValue().length() > 0
				def minLimitExist = ((DocumentParametersItem) item).getMinDataValue() != null && ((DocumentParametersItem) item).getMinDataValue().length() > 0
				
				String key = ""
				String value = ""
				if(maxLimitExist)
				{
					key = "Max_" + ((DocumentParametersItem) item).getParamName()
					value = ((DocumentParametersItem) item).getMaxDataValue()
					lot.getPropertyContainer().setString(key, value)
				}
				if(minLimitExist)
				{
					key = "Min_" + ((DocumentParametersItem) item).getParamName()
					value = ((DocumentParametersItem) item).getMinDataValue()
					lot.getPropertyContainer().setString(key, value)
				}
				if(!maxLimitExist && !minLimitExist)
				{
					key = "RcpParam_" + ((DocumentParametersItem) item).getParamName()
					value = ((DocumentParametersItem) item).getParamValue()
					lot.getPropertyContainer().setString(key, value)
				}
			}
		}
    }
}