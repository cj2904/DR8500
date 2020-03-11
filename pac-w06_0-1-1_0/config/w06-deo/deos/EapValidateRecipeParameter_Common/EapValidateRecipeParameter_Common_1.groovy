package EapValidateRecipeParameter_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsService
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.EqpUtil
import sg.znt.pac.util.RecipeParameterUtil
import sg.znt.pac.util.EqpUtil.VidType
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.domainobject.W06RecipeParameterSet

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Validate equipment recipe parameter setting against Camstar recipe parameter modelling</b>
''')
class EapValidateRecipeParameter_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    @DeoBinding(id="W06RecipeParameterManager")
    private W06RecipeParameterManager w06RecipeParameterManager
    
    private SecsGemService secsGemService
    private SecsControl secsControl
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        W02TrackInLotRequest trackInLot = new W02TrackInLotRequest(inputXmlDocument)
        def eqpId = trackInLot.getResourceName()
        def cLot = cMaterialManager.getCLot(trackInLot.getContainerName())
        if (cLot == null)
        {
            throw new Exception("Lot " + trackInLot.getContainerName() + " does not exist")
        }
        
        String eqpRecipe = RecipeParameterUtil.getEqpRecipe(trackInLot.getRecipeParamList())
        
        if (eqpRecipe == null || eqpRecipe.length() == 0)
        {
            throw new Exception("Could not find EqpRecipe in Camstar recipe parameter")
        }
        else
        {
            secsGemService = (SecsGemService) cEquipment.getExternalService()
            secsControl = cEquipment.getSecsControl()
            
            W06RecipeParameterSet domainObjectSet = w06RecipeParameterManager.getDomainObject(eqpId + "-" + eqpRecipe)
            def domainObject = domainObjectSet.getElement(eqpRecipe)
            def recipeParameters = domainObject.getAllRecipeParameter(eqpRecipe)
            for (param in recipeParameters) 
            {
				def paramName = param.getParameterName()
				paramName = paramName.replace(eqpRecipe + "@", "")
				
                def eqpValue = getEqpValue(paramName)
                boolean pass = RecipeParameterUtil.validateRecipeParameter(param, eqpValue)
                if (!pass)
                {
                    String fixValue = param.getParameterFixValue()
                    String minValue = param.getParameterMinValue()
                    String maxValue = param.getParameterMaxValue()
                    def exceptionMsg = cLot.getPropertyContainer().getString("ExceptionMessage", "")
                    if (exceptionMsg.length() == 0)
                    {
                        exceptionMsg = param.getParameterName() + " failed parameter checking, eqp value = " + eqpValue + " Camstar parameter value (fix = " + fixValue + ", minValue = " + minValue + ", maxValue = " + maxValue + ")"
                    }
                    else
                    {
                        exceptionMsg += "\t" + param.getParameterName() + " failed parameter checking, eqp value = " + eqpValue + " Camstar parameter value (fix = " + fixValue + ", minValue = " + minValue + ", maxValue = " + maxValue + ")"
                    }
					cLot.getPropertyContainer().setString("ExceptionMessage", exceptionMsg)
                }
            }
        }
    }
    
    private String getEqpValue(String parameterName)
    {
        VidType vid = EqpUtil.getVid(parameterName, secsControl)
        String value = EqpUtil.getStringSecsValue((SecsService)secsGemService, vid)
        
        return value
    }
}