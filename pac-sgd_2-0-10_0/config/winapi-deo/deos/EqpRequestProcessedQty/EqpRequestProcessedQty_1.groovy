package EqpRequestProcessedQty

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.TypeChecked
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.RecipeUtil
import sg.znt.pac.util.WinApiEqpUtil
import sg.znt.services.zwin.ZWinApiException

@Deo(description='''
Request processed qty from equipment
''')
class EqpRequestProcessedQty_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="TesterEquipment")
    private CEquipment testerEquipment
    
	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService
	
	@DeoBinding(id="CMaterialManager")
	private CMaterialManager materialManager
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
        def lotList = materialManager.getCLotList(new LotFilterAll())
        if(lotList.size() == 0)
        {
            def exception = new ZWinApiException("-4", "Lot not found")
            exception.setName(mainEquipment.getName())
            throw exception
        }
        def lot = lotList.get(0)
        def propertyContainer = lot.getPropertyContainer()
        
        def testerProp = testerEquipment.getPropertyContainer()
        def testerRecipe = RecipeUtil.getEqpRecipeToSelect(testerProp.getString("MesRecipe", ""), testerProp.getString("MesRecipeRev", ""), testerEquipment.getName())
        
    	S2F41HostCommandSend request = new S2F41HostCommandSend(new SecsAsciiItem("RequestProcessedQty"))
        def addParameter = request.getData().getParameterList().addParameter()
        addParameter.setCPName(new SecsAsciiItem("WorkOrder"))
        addParameter.setCPValue(new SecsAsciiItem(propertyContainer.getString("WorkOrder", "")))
        
        addParameter = request.getData().getParameterList().addParameter()
        addParameter.setCPName(new SecsAsciiItem("Recipe"))
        addParameter.setCPValue(new SecsAsciiItem(testerRecipe))
		S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
				
        def parameterList = reply.getData().getParameterList()
        def trackOutQty = -1
        def inputQty = 0
        if (parameterList.getSize()>0)
        {
            for (int i=0; i<parameterList.getSize();i++)
            {
                def param = parameterList.getParameter(i)
                SecsAsciiItem value = (SecsAsciiItem) param.getCPAckComponent()
                logger.info("Set lot " + lot.getId() + "'s value to " + value.getString())
                
                try
                {
                    if (value.getString().indexOf("ProcessedQty")>-1)
                    {
                        trackOutQty = Integer.parseInt(WinApiEqpUtil.getWinApiParamValue(value))
                    }
                    else if (value.getString().indexOf("InputQty")>-1)
                    {
                        inputQty = Integer.parseInt(WinApiEqpUtil.getWinApiParamValue(value))
                    }
                }
                catch (Exception e)
                {
                    logger.error("Invalid trackout qty : '" + value.getString() + "'")
                }
            }
        }
        lot.setTrackOutQty(trackOutQty)
        lot.getPropertyContainer().setInteger("InputQty", inputQty)
   }
}