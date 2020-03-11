package EqpStartMachine_ALN

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
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EqpStartMachine_ALN_1
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def cLot = cMaterialManager.getCLotList(new LotFilterAll()).get(0)
        def recipeId = cLot.getEquipmentId() + "-" + cLot.getRecipe()

        def recipe = recipeManager.getDomainObject(recipeId)
        def recipeValue = ""
        if (recipe == null)
        {
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

        def startCommand = TscConfig.getStringProperty("Secs.RemoteCommand.Start.Name", "START")
        def process = cEquipment.updateProcessState()
        logger.info("Process state: '" + process.getState() + "'")
        if(process.getState().equals(4))
        {
            logger.info("Sending $startCommand to equipment")
            //        def recipe = setValue(clot.getRecipe())
            def opeId = setValue(cLot.getOperatorId())

            def request =  new S2F41HostCommandSend(new SecsAsciiItem(startCommand))
            request.addParameter(new SecsAsciiItem("LOTID") , new SecsAsciiItem(cLot.getId()))
            request.addParameter(new SecsAsciiItem("OPEID") , new SecsAsciiItem(opeId))
            request.addParameter(new SecsAsciiItem("PPID") , new SecsAsciiItem(recipeValue))


            def reply = secsGemService.sendS2F41HostCommandSend(request)
            logger.info("$startCommand command reply: " + reply.getHCAckMessage())

            if (!reply.isCommandAccepted())
            {
                throw new Exception("Fail to select recipe with error: " + reply.getHCAckMessage())
            }
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

    private String getLotId()
    {
        def lotId = ""
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
                }
            }
        }
        return lotId
    }
}