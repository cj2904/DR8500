package EapRemoveImplanterPortProperty

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
eap remove port properties when track out or track in failed
''')
class EapRemoveImplanterPortProperty_1
{
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
        def event = secsEvent.getName()
        logger.info("Received Event: '$event'.")

        def portList = cEquipment.getPortList()
        for(port in portList)
        {
            if(event.contains(port.getNumber().toString()))
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
                port.getPropertyContainer().removeProperty("RejectQty")
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