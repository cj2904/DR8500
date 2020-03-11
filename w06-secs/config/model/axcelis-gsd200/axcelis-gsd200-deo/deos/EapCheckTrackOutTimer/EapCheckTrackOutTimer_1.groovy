package EapCheckTrackOutTimer

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.zsecs.SecsMessage
import de.znt.zutil.ZPersHelper
import groovy.transform.CompileStatic
import imp.ImpUtil
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.TrackOutLotRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapCheckTrackOutTimer_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

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
        def outbound = new TrackOutLotRequest(inputXml)

        def eqpId = outbound.getResourceName()

        def portList = cEquipment.getPortList()
        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(eqpId))
            {
                ZPersHelper.checkPoint()
                def counter = PacConfig.getIntProperty("SleepTime.Counter", 45)
                def reply = false
                def i = 0
                while(i < counter)
                {
                    def replyMsg = port.getPropertyContainer().getString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_EQP_MSG, "")
                    if(replyMsg.length() == 0)
                    {
                        reply = true
                        break
                    }

                    logger.info("Equipment not yet reply!")
                    Thread.sleep(1000)
                    i++
                }

                if(!reply)
                {
                    if(!port.getPropertyContainer().getString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_ERROR_CODE, "").equalsIgnoreCase("0"))
                    {
                        def errCode = port.getPropertyContainer().getString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_ERROR_CODE, "")
                        def errMsg = ImpUtil.getErrorMessage(errCode)
                        port.getPropertyContainer().setString(W06Constants.PAC_PORT_PROPERTY_CONTAINER_ERROR_CODE, "0")
                        throw new Exception("Transaction Failed with Equipment reply ErrorCode: '$errCode', Error Message: '$errMsg'!!")
                    }
                    else
                    {
                        //send s4f93 cancel / abort message to eqp
                        def recipeValue = port.getPropertyContainer().getString(W06Constants.MES_LOT_RECIPE, "")
                        def lotId = port.getLotId()
                        def quantity = port.getPropertyContainer().getInteger(W06Constants.MES_LOT_TRACK_IN_QTY, 0).toString()
                        def portNumber = port.getNumber().toString()
                        def msg = ImpUtil.sendMessage(4, 93, recipeValue, lotId, quantity, portNumber)
                        sendMessage(msg)
                        throw new Exception("Time Out!!! Equipment not reply in configured duration: '$counter' seconds!!")
                    }
                }
            }
        }
    }

    public void sendMessage(SecsMessage secsMessage)
    {
        secsGemService.sendMessage(secsMessage)
        logger.info("Request message: " + secsMessage)
    }
}