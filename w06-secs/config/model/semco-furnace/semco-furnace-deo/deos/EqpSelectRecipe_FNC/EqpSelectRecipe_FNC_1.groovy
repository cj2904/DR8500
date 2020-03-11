package EqpSelectRecipe_FNC

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
eap select eqp recipe for FNC
''')
class EqpSelectRecipe_FNC_1
{

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def reports = secsEvent.getAssignedReports()
        def batch, tube

        for(report in reports)
        {
            batch = report.getPropertyContainer().getValueAsString("DV@BatchID", "")
            tube = report.getPropertyContainer().getValueAsString("DV@TubeID", "")
            logger.info("Jimmy Batch: '$batch', tube: '$tube'")
        }

        def portList = cEquipment.getPortList()
        def tubeId, recipeId
        def found = false

        for(port in portList)
        {
            if(port.getPropertyContainer().getString("EqpBatchId", "").equalsIgnoreCase(batch) && port.getNumber().toString().equalsIgnoreCase(tube))
            {
                found = true
                recipeId = port.getPortId() + "-" + port.getPropertyContainer().getString(W06Constants.MES_LOT_RECIPE, "")
                tubeId = port.getNumber().toString()
            }
        }

        if(!found)
        {
            logger.error("Batch: '$batch' is not found for tube: '$tube' in PAC persistence!!!!")
        }

        def recipe = recipeManager.getDomainObject(recipeId)

        if (recipe == null)
        {
            logger.error("$recipeId not found in recipe domain object!")
        }

        def recipeParam = recipe.getElement("EqpRecipe")

        if (recipeParam == null)
        {
            logger.error("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
        }

        def recipeValue = recipeParam.getParameterValue()
        if (recipeValue==null || recipeValue.length()==0)
        {
            logger.error("Equipment recipe value cannot be empty, please configure 'EqpRecipe' value in Camstar!")
        }
        else
        {
            sendRecipe(recipeValue, tubeId)
        }
    }

    void sendRecipe(String recipeValue, String tubeId)
    {
        def ppSelectFormat = PacConfig.getStringProperty("Secs.RemoteCommand.PpidSelect.Name","PP_SELECT")
        def request =  new S2F41HostCommandSend(new SecsAsciiItem(ppSelectFormat))

        request.addParameter(new SecsAsciiItem("RECIPEID") , new SecsAsciiItem(recipeValue))
        request.addParameter(new SecsAsciiItem("TUBEID") , new SecsAsciiItem(tubeId))

        S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
        logger.info "PPSelect command : " + reply.getHCAckMessage()
        if (!reply.isCommandAccepted())
        {
            throw new Exception("Fail to select recipe with error: " + reply.getHCAckMessage())
        }
    }
}