package EapSendCancelCommandToEqp

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.zsecs.SecsMessage
import groovy.transform.CompileStatic
import imp.ImpUtil
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager

@CompileStatic
@Deo(description='''
eap send cancel track in and close door command to eqp
''')
class EapSendCancelCommandToEqp_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="MaterialManager")
    private CMaterialManager cMaterialManager

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
        def request = new CommonOutboundRequest(inputXml)
        def eqpId = request.getResourceName()
        def isCancelTrackIn = true
        def recipeValue
        def lotId
        def quantity
        def portNumber

        def portList = cEquipment.getPortList()
        def found = false

        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(eqpId))
            {
                found = true
                recipeValue = port.getPropertyContainer().getString(W06Constants.MES_LOT_RECIPE, "")
                lotId = port.getLotId()
                quantity = port.getPropertyContainer().getInteger(W06Constants.MES_LOT_TRACK_IN_QTY, 0).toString()
                portNumber = port.getNumber().toString()
                logger.info("Selected Port info = Recipe '$recipeValue', Lot '$lotId', Quantity '$quantity', Port '$portNumber'.")
                break
            }
        }

        if(!found)
        {
            throw new Exception("Track Out Equipment '$eqpId' for Lot: '$lotId' is not found!!! Please verfiy again")
        }

        if(isCancelTrackIn)
        {
            //send s4f93 cancel / abort message to eqp
            def msg = ImpUtil.sendMessage(4, 93, recipeValue, lotId, quantity, portNumber)
            sendMessage(msg)
        }
        else
        {
            logger.info("Jimmy Not cancel TrackIn")
        }
    }

    public void sendMessage(SecsMessage secsMessage)
    {
        secsGemService.sendMessage(secsMessage)
        logger.info("Request message: " + secsMessage)
    }
}