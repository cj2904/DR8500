package EqpSelectRecipe

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.SecsServiceImpl
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked;
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.util.RecipeUtil
import sg.znt.services.zwin.ZWinApiServiceImpl

@Deo(description='''
Send remote command to gateway
''')
class EqpSelectRecipe_1 {

	static final String LOT_ID = "LotId"
	static final String QTY = "Qty"
	static final String RECIPE_ID = "RecipeId"
	static final String RECIPE_REV = "RecipeRev"
	static final String WAFER_NUMBER = "WaferNumber"
	static final String WAFER_LOT_ID = "WaferLotId"
	static final String OPERATOR = "Operator"
	static final String SHIFT = "Shift"
	static final String RECIPE_PARAMETERS = "RecipeParameters"


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService
		
	@DeoBinding(id="Parameters")
	private Map parameters
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		String paramLotValue = parameters.get(LOT_ID) == null ? "" : parameters.get(LOT_ID)
		S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("PP-SELECT"))
		def addParameter = request.getData().getParameterList().addParameter()
		addParameter.setCPName(new SecsAsciiItem(LOT_ID))
		addParameter.setCPValue(new SecsAsciiItem(paramLotValue))
		
		String paramQtyValue = parameters.get(QTY) == null ? "" : parameters.get(QTY)
		def addParameter1 = request.getData().getParameterList().addParameter()
		addParameter1.setCPName(new SecsAsciiItem(QTY))
		addParameter1.setCPValue(new SecsAsciiItem(paramQtyValue))
		
		def addParameter2 = request.getData().getParameterList().addParameter()
		addParameter2.setCPName(new SecsAsciiItem("PPID"))
        def eqpName = ""
        if (secsGemService instanceof SecsServiceImpl)
        {
            eqpName = ((SecsServiceImpl)secsGemService).getName()
        }
        else if (secsGemService instanceof ZWinApiServiceImpl)
        {
            eqpName = ((ZWinApiServiceImpl)secsGemService).getName()
        }
		String paramRecipeIdValue = parameters.get(RECIPE_ID) == null ? "" : parameters.get(RECIPE_ID)
		String paramRecipeRevValue = parameters.get(RECIPE_REV) == null ? "" : parameters.get(RECIPE_REV)
        def recipeId = RecipeUtil.getEqpRecipeFullName(paramRecipeIdValue, paramRecipeRevValue, eqpName)
  		addParameter2.setCPValue(new SecsAsciiItem(recipeId))
		
		String paramWaferValue = parameters.get(WAFER_NUMBER) == null ? "" : parameters.get(WAFER_NUMBER)
		def addParameter3 = request.getData().getParameterList().addParameter()
		addParameter3.setCPName(new SecsAsciiItem(WAFER_NUMBER))
		addParameter3.setCPValue(new SecsAsciiItem(paramWaferValue))
		
		String paramWaferLotValue = parameters.get(WAFER_LOT_ID) == null ? "" : parameters.get(WAFER_LOT_ID)
		def addParameter4 = request.getData().getParameterList().addParameter()
		addParameter4.setCPName(new SecsAsciiItem(WAFER_LOT_ID))
		addParameter4.setCPValue(new SecsAsciiItem(paramWaferLotValue))
		
		String paramOperatorValue = parameters.get(OPERATOR) == null ? "" : parameters.get(OPERATOR)
		def addParameter5 = request.getData().getParameterList().addParameter()
		addParameter5.setCPName(new SecsAsciiItem(OPERATOR))
		addParameter5.setCPValue(new SecsAsciiItem(paramOperatorValue))
		
		String paramShiftValue = parameters.get(SHIFT) == null ? "" : parameters.get(SHIFT)
		def addParameter6 = request.getData().getParameterList().addParameter()
		addParameter6.setCPName(new SecsAsciiItem(SHIFT))
		addParameter6.setCPValue(new SecsAsciiItem(paramShiftValue))
		
        Map<String, String> recipeParam = (Map<String, String>)parameters.get(RECIPE_PARAMETERS)
		if(recipeParam != null && recipeParam.size() > 0)
		{
			def addParameter7 = request.getData().getParameterList().addParameter()
			addParameter7.setCPName(new SecsAsciiItem(RECIPE_PARAMETERS))
			String recipeParamStr = ""
			for (var in recipeParam) 
			{
				recipeParamStr = recipeParamStr + var.getKey() + "=" + var.getValue() + ";"
			}
			if (recipeParam.size()>0)
			{
				recipeParamStr = recipeParamStr.substring(0, recipeParamStr.length() - 1)
			}
			addParameter7.setCPValue(new SecsAsciiItem(recipeParamStr))
		}
		
    	S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
		if(reply.isCommandAccepted())
		{
			//OK
		}
		else
		{
			throw new ValidationFailureException(paramLotValue, "Error sending command to machine - " + reply.getHCAckMessage(), true)
		}
    }
}