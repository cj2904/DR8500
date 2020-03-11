package EapCancelImplanterPortProperty

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager

@CompileStatic
@Deo(description='''
eap remove port properties when track out or track in failed
''')
class EapCancelImplanterPortProperty_1
{
    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

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
        def lotId = outbound.getContainerName()
        def recipeId
        def wipDataId = eqpId + "-" + lotId
        def lots = outbound.getLotList()
        logger.info("Complete Lot lot list: '$lots'")

        for(lt in lots)
        {
            def portList = cEquipment.getPortList()
            for(port in portList)
            {
                if(port.getPortId().equalsIgnoreCase(eqpId))
                {
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_ID)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_PRODUCTLINE)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_PRODUCT_FAMILY)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_PRODUCT)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_RECIPE)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_RECIPE_VERSION)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_QUANTITY)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_QUANTITY2)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_GDPW)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_TRACK_IN_QTY)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_WORKFLOW)
                    port.getPropertyContainer().removeProperty(W06Constants.PAC_PORT_PROPERTY_CONTAINER_EQP_MSG)
                    port.getPropertyContainer().removeProperty(W06Constants.PAC_PORT_PROPERTY_CONTAINER_ERROR_CODE)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_MODEL_NUMBER)
                    port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_YIELD_FAILURE_LIMIT)
                    port.getPropertyContainer().removeProperty("tscDriveInRecipe")
                    port.getPropertyContainer().removeProperty("tscPreDRecipe")
                    port.getPropertyContainer().removeProperty("tscResistance")
                    port.getPropertyContainer().removeProperty("tscIsRequiredRangeCheck")
                    port.getPropertyContainer().removeProperty("tscEpiThickness")
                    port.getPropertyContainer().removeProperty("tscLastProcessEqp")
                    logger.info("Successful remove PortValue: ''")
                }
            }
        }
    }
}