package EapSendUnloadCassette

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import de.znt.services.secs.SecsGemService
import de.znt.zsecs.SecsMessage
import groovy.transform.CompileStatic
import imp.ImpUtil
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager

@CompileStatic
@Deo(description='''
eap send unload cassette command s4f85 to eqp
''')
class EapSendUnloadCassette_1
{

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="MaterialManager")
    private CMaterialManager cMaterialManager

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
        def recipeValue
        def lotId
        def quantity
        def portNumber

        def eventName = secsEvent.getName()

        def portList = cEquipment.getPortList()
        def found = false

        for(port in portList)
        {
            if(eventName.contains(port.getNumber().toString()))
            {
                found = true
                recipeValue = port.getPropertyContainer().getString(W06Constants.MES_LOT_RECIPE, "")
                lotId = port.getLotId()
                quantity = port.getPropertyContainer().getInteger(W06Constants.MES_LOT_TRACK_IN_QTY, 0).toString()
                portNumber = port.getNumber().toString()
                break
            }
        }

        if(!found)
        {
            logger.error("Event: '$eventName' is not defined! Please verify event's in SECS Configuration!!")
        }
        else
        {
            // send s4f85 unload cassette
            def msg = ImpUtil.sendMessage(4, 85, recipeValue, lotId, quantity, portNumber)
            sendMessage(msg)
        }
    }
    public void sendMessage(SecsMessage secsMessage)
    {
        secsGemService.sendMessage(secsMessage)
        logger.info("Request message: " + secsMessage)
    }
}