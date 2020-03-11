package EapHandleEqpReplyMessage

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.zsecs.SecsMessage
import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsU1Item
import groovy.transform.CompileStatic
import imp.ImpUtil
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager

@CompileStatic
@Deo(description='''
eap handle customise secs eqp reply message
''')
class EapHandleEqpReplyMessage_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="MaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="SecsMessage")
    private SecsMessage secsMessage

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def reply = secsMessage.getData().getValueList()
        def sml = secsMessage.getSML()
        //        def parser = new SmlAsciiParser()
        //        def reply = parser.parse(sml)
        //        reply = reply.getData().getValueList()

        logger.info("SML Message: '$sml'")

        def recipe = ((SecsAsciiItem)reply.get(0)).getString()
        def lotId = ((SecsAsciiItem)reply.get(1)).getString()
        def qty = ((SecsU1Item)reply.get(2)).getShort(0)
        def portNumber = ((SecsU1Item)reply.get(3)).getShort(0)
        def errorCode = 0

        if((secsMessage.getStream().equals((byte)4) && secsMessage.getFunction().equals((byte)91))) {
            errorCode = ((SecsU1Item)reply.get(4)).getShort(0)
        }

        logger.info("Receive reply: 'S" + secsMessage.getStream() + "F" + secsMessage.getFunction() + "', message: recipe: '$recipe', lot: '$lotId', qty: '$qty', port: '$portNumber', erroCode: '$errorCode'.")

        def portList = cEquipment.getPortList()
        def found = false
        def portId
        def po

        for(port in portList)
        {
            def portValue = port.getNumber().toString()
            if(portValue.equalsIgnoreCase(portNumber.toString()))
            {
                po = port
                portId = port.getPortId()
                port.getPropertyContainer().setString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_ERROR_CODE, errorCode.toString())
                found = true
                if(port.getPropertyContainer().getString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_ERROR_CODE, "").equalsIgnoreCase("0"))
                {
                    port.getPropertyContainer().removeProperty(W06Constants.PAC_PORT_PROPERTY_CONTAINER_ERROR_CODE)
                    port.getPropertyContainer().removeProperty(W06Constants.PAC_PORT_PROPERTY_CONTAINER_EQP_MSG)
                }
                else
                {
                    def msg = portValue + "," + recipe + "," + lotId + "," + errorCode.toString()
                    port.getPropertyContainer().setString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_EQP_MSG, msg)
                }
            }
        }
        if(!found)
        {
            def errMsg = ImpUtil.getErrorMessage(errorCode.toString())
            po.getPropertyContainer().setString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_ERROR_CODE, "0")
            def exceptionMsg = "Equipment: '$portId' Reply: Recipe: '$recipe', LotId: '$lotId', Port: '$portNumber' ErrorCode: '$errorCode'! Error Message: '$errMsg'!!"
            throw new Exception(exceptionMsg)
        }
    }
}