package EapSendTrackOutCommandToEqp

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
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.TrackOutLotRequest

@CompileStatic
@Deo(description='''
generic secs message to eqp when receive event trigger
''')
class EapSendTrackOutCommandToEqp_1
{

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def recipe
        def lotId
        def quantity
        def portNumber
        def portList = cEquipment.getPortList()

        def outbound = new TrackOutLotRequest(inputXml)
        def outboundLot = outbound.getContainerName()
        def eqpId = outbound.getResourceName()
        def found = false
        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(eqpId))
            {
                lotId = port.getLotId()
                recipe = port.getPropertyContainer().getString(W06Constants.MES_LOT_RECIPE, "")
                quantity = port.getPropertyContainer().getInteger(W06Constants.MES_LOT_TRACK_IN_QTY, 0).toString()
                portNumber = port.getNumber().toString()

                found = true
                String msg = portNumber + "," + recipe + "," + lotId + "," + "0"
                port.getPropertyContainer().setString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_EQP_MSG, msg)
                break
            }
        }

        if(!found)
        {
            throw new Exception("Lot: '$lotId' is not found in PAC's lot persistence!")
        }
        else
        {
            //send s4f95 track out command open door
            logger.info("LotId: '$lotId', recipe: '$recipe', quantity: '$quantity', port: '$portNumber'")
            def msg = ImpUtil.sendMessage(4, 95, recipe, lotId, quantity, portNumber)
            sendMessage(msg)
            //sendMessage(recipe, lotId, quantity, portNumber)
        }
    }

    public void sendMessage(SecsMessage secsMessage)
    {
        secsGemService.sendMessage(secsMessage)
        logger.info("Request message: " + secsMessage)
    }
}