package EapSendTransferCommandToEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.ItemNotFoundException
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
import sg.znt.pac.material.CMaterialManager

@CompileStatic
@Deo(description='''
eap send transfer boat command to eqp
''')
class EapSendTransferCommandToEqp_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

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
        def batch, tube, recipe
        def lots = new ArrayList<String>()

        for(report in reports)
        {
            if(report.getRptId().equals(104))
            {
                //*** all lot
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID1", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID1", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID2", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID3", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID4", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID5", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID6", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID7", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID8", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID9", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID10", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID11", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID12", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID13", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID14", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID15", ""))
                lots.add(report.getPropertyContainer().getValueAsString("DV@LotID16", ""))
                //***
                logger.info("Lots from event: '$lots'")
                batch = report.getPropertyContainer().getValueAsString("DV@BatchID", "")
                tube = report.getPropertyContainer().getValueAsString("DV@TubeID", "")
                recipe = report.getPropertyContainer().getValueAsString("DV@RecipeID", "")
            }
        }

        def portList = cEquipment.getPortList()
        def found = false
        def eqpPort
        def rcp = ""
        def recipeId = ""
        def recipeValue = ""

        for(port in portList)
        {
            rcp = port.getPropertyContainer().getString(W06Constants.MES_LOT_RECIPE, "")
            logger.info("recipeId: '$rcp'")
            if(rcp.length() > 0)
            {
                eqpPort = port
                def portId = port.getPortId()
                recipeId = portId + "-" + rcp
                logger.info("Main recipe: '$rcp' for eqp: '$portId' and recipeId: '$recipeId'")
                break
            }
        }

        if(recipeId.length() > 0)
        {
            def mesRecipe = recipeManager.getDomainObject(recipeId)

            if (mesRecipe == null)
            {
                logger.error("$recipeId not found in recipe domain object!")
            }

            def recipeParam = mesRecipe.getElement("EqpRecipe")

            if (recipeParam == null)
            {
                logger.error("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
            }

            recipeValue = recipeParam.getParameterValue()
            if (recipeValue==null || recipeValue.length()==0)
            {
                logger.error("Equipment recipe value cannot be empty, please configure 'EqpRecipe' value in Camstar!")
            }
        }

        if(recipeValue.length() > 0)
        {
            logger.info("Recipe $recipe ; recipe value $recipeValue ;  $tube")
            if(recipeValue.equalsIgnoreCase(recipe) && eqpPort.getNumber().toString().equalsIgnoreCase(tube))
            {
                found = true
                eqpPort.getPropertyContainer().setString("EqpBatchId", batch)
                eqpPort.getPropertyContainer().setString("EqpTubeId", tube)

                for(lot in lots)
                {
                    if(lot.trim().length() > 0)
                    {
                        try
                        {
                            def cLot = cMaterialManager.getCLot(lot)
                            cLot.getPropertyContainer().setString("EqpBatchId", batch)
                            cLot.getPropertyContainer().setString("EqpTubeId", tube)
                        } catch (ItemNotFoundException e)
                        {
                            logger.error("For event lot: '$lot' is not found in Material Manager!!!")
                        }
                    }
                }
            }
        }

        if(!found)
        {
            throw new Exception("Received Event with recipe: '$recipe', batch: '$batch', tube: '$tube' is not found in PAC!!")
        }
        else
        {
            def msg = "All lots for batch '$batch' are complete loaded."
            sendTerminalMessage(msg)
            sendTransferCommand(batch, tube)
        }
    }

    void sendTransferCommand(String batchId, String tubeId)
    {
        def request =  new S2F41HostCommandSend(new SecsAsciiItem("TRANSFER"))
        request.addParameter(new SecsAsciiItem("BATCHID") , new SecsAsciiItem(batchId))
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