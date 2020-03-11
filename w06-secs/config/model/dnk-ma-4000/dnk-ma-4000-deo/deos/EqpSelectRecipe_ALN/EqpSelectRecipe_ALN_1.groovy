package EqpSelectRecipe_ALN

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.TscConfig
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EqpSelectRecipe_ALN_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def lotId, recipeId
        def request1 = new W02TrackInLotRequest(inputXmlDocument)
        def outboundLot = request1.getContainerName()

        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        if(lotList.size() > 0)
        {
            for(lot in lotList)
            {
                def lotId2 = lot.getId()
                if(lotId2.equalsIgnoreCase(outboundLot))
                {
                    lotId = lotId2
                    recipeId = lot.getEquipmentId() + "-" + lot.getRecipe()
                }
            }
        }

        def ppSelectCommand = TscConfig.getStringProperty("Secs.RemoteCommand.PpidSelect.Name", "PP_SELECT")
        logger.info("Sending $ppSelectCommand with recipe ID '$recipeId' to equipment")

        def request =  new S2F41HostCommandSend(new SecsAsciiItem(ppSelectCommand))
        def lot = cMaterialManager.getCLot(lotId)
        def recipe = recipeManager.getDomainObject(recipeId)
        def recipeValue = ""
        if (recipe == null) {
            throw new Exception("$recipeId not found in recipe domain object!")
        }
        try
        {
            def recipeParam = recipe.getElement("EqpRecipe")

            if (recipeParam == null)
            {
                throw new Exception("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
            }

            recipeValue = recipeParam.getParameterValue()
            if (recipeValue==null || recipeValue.length()==0)
            {
                throw new Exception("Equipment recipe value cannot be empty, please configure 'EqpRecipe' value in Camstar!")
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage())
        }

        def opeId = setValue(lot.getOperatorId())
        def waferCount = lot.getQty2()
        def i = 1

        request.addParameter(new SecsAsciiItem("LOTID") , new SecsAsciiItem(lotId))
        request.addParameter(new SecsAsciiItem("OPEID") , new SecsAsciiItem(opeId))
        request.addParameter(new SecsAsciiItem("PPID") , new SecsAsciiItem(recipeValue))

		def count = PacConfig.getIntProperty("HostCommand.WaferSlot.Count", 25)
        while (i <= count)
        {
        	def seq = addZeroLeading(i.toString(), 2)
			if (i <= waferCount)
			{
				request.addParameter(new SecsAsciiItem("WAFERID" + seq) , new SecsAsciiItem(lotId))								
			}
			else
			{
				request.addParameter(new SecsAsciiItem("WAFERID" + seq) , new SecsAsciiItem(""))
			}
            i++
        }

        def reply = secsGemService.sendS2F41HostCommandSend(request)
        logger.info("$ppSelectCommand command reply: " + reply.getHCAckMessage())

        if (!reply.isCommandAccepted())
        {
            throw new Exception("Fail to select recipe with error: " + reply.getHCAckMessage())
        }
    }

    private String setValue(String value)
    {
        if (value != null)
        {
            return value
        }
        return ""
    }

    public static String addZeroLeading(String suspect, int digit)
    {
        if (suspect.isNumber())
        {
            String noZeroStr = removeZero(suspect + "")
            return StringUtils.leftPad(noZeroStr, digit, "0")
        }
        return StringUtils.leftPad("0", digit, "0")
    }

    public static String removeZero(String str)
    {
        int i = 0
        while (i < str.length() && str.charAt(i) == '0')
        {
            i++
        }
        def sb = new StringBuffer(str)
        sb.replace(0, i, "")
        return sb.toString()
    }
}