package EqpSelectRecipe_Secs_GSD

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.zsecs.SecsMessage
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.sml.SecsItemType
import groovy.transform.CompileStatic
import imp.ImpUtil
import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.TrackInLotRequest

@CompileStatic
@Deo(description='''
select required recipe
''')
class EqpSelectRecipe_Secs_GSD_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="MaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="RecipeManager")
    private RecipeManager recipeManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="EventType")
    private String eventType

    @DeoBinding(id="WipDataManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new TrackInLotRequest(inputXml)
        def lotId = outbound.getContainerName()
        def recipeName = outbound.getRecipeName()
        def qty = outbound.getTrackInQty()
        def eqpId = outbound.getResourceName()

        if (recipeName == null || recipeName.length() == 0) {
            throw new Exception("Recipe not found!")
        }

        def recipeId = outbound.getResourceName() + "-" + recipeName
        def recipe = recipeManager.getDomainObject(recipeId)

        if (recipe == null) {
            throw new Exception("$recipeId not found in recipe domain object!")
        }

        def recipeParam = recipe.getElement("EqpRecipe")

        if (recipeParam == null) {
            throw new Exception("Equipment recipe cannot be empty, please configure as Recipe Parameter with 'EqpRecipe' in Camstar!")
        }

        def recipeValue = recipeParam.getParameterValue()
        if (recipeValue==null || recipeValue.length()==0) {
            throw new Exception("Equipment recipe value cannot be empty, please configure 'EqpRecipe' value in Camstar!")
        }

        def portNumber
        def portList = cEquipment.getPortList()
        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(outbound.getResourceName()))
            {
                portNumber = port.getNumber().toString()
            }
        }
        logger.info("Request portNumber: " + portNumber)
        //send s4f81 select recipe
        def secsMsg = ImpUtil.sendMessage(4, 81, recipeValue, lotId, qty, portNumber)
        sendRecipe(secsMsg)

        def found = false
        for(port in portList) {
            if(port.getPortId().equalsIgnoreCase(eqpId)) {
                found = true
                def errorCode = "0"
                def container = port.getPropertyContainer()
                def msg = portNumber + "," + recipeValue + "," + lotId + "," + errorCode
                port.getPropertyContainer().setString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_EQP_MSG, msg)
                port.getPropertyContainer().setString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_ERROR_CODE, errorCode)
            }
        }
        if(!found) {
            throw new Exception("Lot: '$lotId' is not found in PAC's lot persistence!")
        }
    }

    void sendRecipe(SecsMessage secsMsg) {
        secsGemService.sendMessage(secsMsg)
        logger.info("Request message: " + secsMsg)
    }
}