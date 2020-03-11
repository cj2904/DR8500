package EapSendStartCommandToEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

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
eap send start command to eqp
''')
class EapSendStartCommandToEqp_1
{
    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

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
        }

        def portList = cEquipment.getPortList()
        def tubeId, recipeId

        for(port in portList)
        {
            logger.info("Jimmy:" + port.getPropertyContainer().getString("EqpBatchId", ""))
            if(port.getPropertyContainer().getString("EqpBatchId", "").equalsIgnoreCase(batch) && port.getNumber().toString().equalsIgnoreCase(tube))
            {
                logger.info("Jimmy Found")
                recipeId = port.getPortId() + "-" + port.getPropertyContainer().getString(W06Constants.MES_LOT_RECIPE, "")
                tubeId = port.getNumber().toString()
            }
        }

        def recipe = recipeManager.getDomainObject(recipeId)

        if (recipe == null)
        {
            logger.error("$recipeId not found in recipe domain object! The received event batch id: '$batch', and tube id: '$tube'")
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
            def msg = "Tube: '$tube' is finish load for batch: '$batch'. Now start equipment process!!!"
            sendTerminalMessage(msg)
            sendStartCommand(recipeValue, tubeId)
        }
    }

    void sendStartCommand(String recipe, String tubeId)
    {
        def request =  new S2F41HostCommandSend(new SecsAsciiItem("START"))
        request.addParameter(new SecsAsciiItem("RECIPEID") , new SecsAsciiItem(recipe))
        request.addParameter(new SecsAsciiItem("TUBEID") , new SecsAsciiItem(tubeId))

        S2F42HostCommandAcknowledge response = secsGemService.sendS2F41HostCommandSend(request)
        logger.info "PPSelect command : " + response.getHCAckMessage()

        if(!response.isCommandAccepted())
        {
            throw new Exception("Equipment Reply Error Message: '" + response.getHCAckMessage() + "'")
        }
    }

    void sendTerminalMessage(String message)
    {
        SecsGemService secsService = (SecsGemService) cEquipment.getExternalService()
        secsService.sendTerminalMessage((byte)0, message)
    }
}