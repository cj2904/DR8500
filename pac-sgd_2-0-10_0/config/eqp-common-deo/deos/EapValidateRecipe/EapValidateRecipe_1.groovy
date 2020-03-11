package EapValidateRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked;
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager

@Deo(description='''
Validation on recipe selection, selected recipe must match with Camstar recipe
''')
class EapValidateRecipe_1 {
	private def lot


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="Parameter")
	private Map parameter
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager
	
	@DeoBinding(id="EventName")
	private String eventName
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		if(parameter.size() == 0)
		{
			throw new ValidationFailureException("", eventName + "::: No parameter found")
		}
		
    	String lotId = parameter.get("LotName")
		CLot lot = cMaterialManager.getCLot(lotId)
		def recipeName = lot.getPropertyContainer().getString("LotStart_RecipeName", "")
		def camstarRecipe = lot.getRecipe()
		if(recipeName != null && recipeName.length() > 0)
		{
			if(recipeName.equals(camstarRecipe))
			{
				//ok
			}
			else
			{
				throw new ValidationFailureException(lotId, "Recipe not match")
			}
		}
    }
}