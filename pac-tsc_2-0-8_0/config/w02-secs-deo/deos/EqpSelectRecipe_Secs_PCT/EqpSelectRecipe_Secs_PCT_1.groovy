package EqpSelectRecipe_Secs_PCT

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.TrackInLotRequest

@CompileStatic
@Deo(description='''
select required recipe
''')
class EqpSelectRecipe_Secs_PCT_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new TrackInLotRequest(inputXml)
        def recipeName = outbound.getRecipeName()
        def lot = outbound.getContainerName()
        def eqp = outbound.getResourceName()

        if (recipeName == null || recipeName.length() == 0)
        {
            throw new Exception("Recipe not found!")
        }

        def recipeId = eqp + "-" + recipeName
        def recipe = recipeManager.getDomainObject(recipeId)

        if (recipe == null)
        {
            throw new Exception("$recipeId not found in recipe domain object!")
        }

        def recipeParam = recipe.getElement("EqpRecipe")

        if (recipeParam == null)
        {
            throw new Exception("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
        }

        def recipeValue = recipeParam.getParameterValue()
        if (recipeValue==null || recipeValue.length()==0)
        {
            throw new Exception("Equipment recipe value cannot be empty, please configure 'EqpRecipe' value in Camstar!")
        }

        def lastAlphabet = eqp[eqp.length()-1]
        recipeValue = lastAlphabet + recipeValue

        sendRecipe(recipeValue, lot)
    }


    void sendRecipe(String recipeValue, String lot)
    {

        def ppSelectFormat = PacConfig.getStringProperty("Secs.RemoteCommand.PpidSelect.Name","PP_SELECT")
        def request =  new S2F41HostCommandSend(new SecsAsciiItem(ppSelectFormat))

        request.addParameter(new SecsAsciiItem("PPID") , new SecsAsciiItem(recipeValue))
        request.addParameter(new SecsAsciiItem("LOTID") , new SecsAsciiItem(lot))

        S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
        logger.info "PPSelect command : " + reply.getHCAckMessage()
        if (!reply.isCommandAccepted())
        {
            throw new Exception("Fail to select recipe with error: " + reply.getHCAckMessage())
        }
    }
}