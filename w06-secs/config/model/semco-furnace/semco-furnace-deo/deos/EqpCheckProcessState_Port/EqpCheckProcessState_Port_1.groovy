package EqpCheckProcessState_Port

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import OutboundRequest.CommonOutboundRequest
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.EqpUtil

@CompileStatic
@Deo(description='''
eqp check process state for track in eqp port
''')
class EqpCheckProcessState_Port_1
{

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

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

        def portList = cEquipment.getPortList()
        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(eqpId))
            {
                def portId = port.getPortId()
                def vid = PacConfig.getStringProperty("Secs.ProcessState." + portId + ".StatusVariable.Name", "")
                logger.info("Vid: '$vid'")
                def processState =  getValueFromEqp( portId ,vid)
                def processName = PacConfig.getStringProperty("Secs.ProcessState." + portId + "." + processState, "")
                def isReady2Start = PacConfig.getStringArrayProperty("Secs.ProcessState." + portId + ".States.Ready2Start", "", ",")

                if(!isReady2Start.contains(processState))
                {
                    throw new Exception("Current Equipment: '$portId' Process State: '$processName' is not allow to track in!!!!")
                }
            }
        }
    }

    String getValueFromEqp(String eqp, String vidName)
    {
        def svid = secsControl.translateSvVid(vidName)
        SecsComponent< ? > svidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svid)))
        def request = new S1F3SelectedEquipmentStatusRequest(svidItem)
        def reply = secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
        def eqpValue = EqpUtil.getVariableData(reply.getData().getSV(0))

        if(eqpValue.length() > 0)
        {
            logger.info("Equipment: '$eqp' process state is '$eqpValue'")
            return eqpValue
        }
        return ""
    }
}