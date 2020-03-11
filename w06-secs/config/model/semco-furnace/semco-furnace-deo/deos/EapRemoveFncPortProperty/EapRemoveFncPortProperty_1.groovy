package EapRemoveFncPortProperty

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.RecipeManager
import sg.znt.pac.domainobject.W06RecipeParameterManager
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager

@CompileStatic
@Deo(description='''
eap remove port properties when track out or track in failed
''')
class EapRemoveFncPortProperty_1
{

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="RecipeManager")
    RecipeManager recipeManager

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataManager

    @DeoBinding(id="W06RecipeParameterManager")
    private W06RecipeParameterManager w06RecipeManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXml

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new CommonOutboundRequest(inputXml)
        def eqpId = outbound.getResourceName()
        def lotList = outbound.getLotList()
        logger.info("Equipment: '$eqpId' and lot: '$lotList' from outbound!!!")

        def portList = cEquipment.getPortList()
        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(eqpId))
            {
                for(lt in lotList)
                {
                    port.getPropertyContainer().removeProperty(lt)
                }
                port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_RECIPE)
                port.getPropertyContainer().removeProperty("Lot1")
                port.getPropertyContainer().removeProperty("Lot2")
                port.getPropertyContainer().removeProperty("EqpBatchId")
                port.getPropertyContainer().removeProperty("EqpTubeId")
                port.getPropertyContainer().removeProperty("Recipe")
                logger.info("Successful remove lot: '$lotList' properties from equipment: '$eqpId'.")
            }
        }
    }
}