package EapSendLoadCommandToEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.CEquipmentPort
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
eap send load lot id to port command to eqp
''')
class EapSendLoadCommandToEqp_1
{

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02TrackInLotRequest(inputXml)
        def eqpId = outbound.getResourceName()
        def outboundLot = outbound.getContainerName()
        def recipeId = outbound.getResourceName() + "-" + outbound.getRecipeName()

        def portList = cEquipment.getPortList()
        def tubeId
        def selectedPort
        def lotId1 = "", lotId2 = ""
        def eqpContainer = cEquipment.getPropertyContainer()

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

        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(eqpId))
            {
                if((port.getPropertyContainer().getString("Lot1", "")).length() == 0)
                {
                    port.getPropertyContainer().setString("Lot1", outboundLot)
                    port.getPropertyContainer().setString(outboundLot, outboundLot)
                    logger.info("Lot1: '$outboundLot'")
                }
                else
                {
                    selectedPort = port
                    port.getPropertyContainer().setString("Lot2", outboundLot)
                    port.getPropertyContainer().setString(outboundLot, outboundLot)
                    logger.info("Lot2: '$outboundLot'")

                    lotId1 = port.getPropertyContainer().getString("Lot1", "")
                    lotId2 = port.getPropertyContainer().getString("Lot2", "")
                }

                tubeId = port.getNumber().toString()
            }
        }

        if(lotId1.length() > 0 && lotId2.length() > 0)
        {
            sendLoadCommand(lotId1, lotId2, recipeValue, tubeId, selectedPort)
            selectedPort.getPropertyContainer().removeProperty("Lot1")
            selectedPort.getPropertyContainer().removeProperty("Lot2")
            logger.info("Success remove Lot1 '$lotId1' from persistance")
            logger.info("Success remove Lot2 '$lotId2' from persistance")
        }
    }

    void sendLoadCommand(String lot1, String lot2, String recipe, String tubeId, CEquipmentPort selectedPort)
    {
        def request =  new S2F41HostCommandSend(new SecsAsciiItem("LOAD"))
        request.addParameter(new SecsAsciiItem("LOTID1") , new SecsAsciiItem(lot1))
        request.addParameter(new SecsAsciiItem("LOTID2") , new SecsAsciiItem(lot2))
        request.addParameter(new SecsAsciiItem("RECIPEID") , new SecsAsciiItem(recipe))
        request.addParameter(new SecsAsciiItem("TUBEID") , new SecsAsciiItem(tubeId))

        S2F42HostCommandAcknowledge response = secsGemService.sendS2F41HostCommandSend(request)
        logger.info "PPSelect command : " + response.getHCAckMessage()

        if(!response.isCommandAccepted())
        {
            selectedPort.getPropertyContainer().removeProperty(lot1)
            selectedPort.getPropertyContainer().removeProperty(lot2)
            throw new Exception("Equipment Reply Error Message: '" + response.getHCAckMessage() + "'")
        }
    }
}