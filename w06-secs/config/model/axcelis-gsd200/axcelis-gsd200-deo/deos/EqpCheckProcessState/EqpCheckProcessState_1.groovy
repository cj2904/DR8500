package EqpCheckProcessState

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

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
import sg.znt.pac.util.EqpUtil
import sg.znt.services.camstar.outbound.TrackInLotRequest

@CompileStatic
@Deo(description='''
eqp check process state
''')
class EqpCheckProcessState_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new TrackInLotRequest(inputXmlDocument)
        def eqp = outbound.getResourceName()

        def portList = mainEquipment.getPortList()
        def found = false
        for(port in portList)
        {
            if(port.getPortId().equalsIgnoreCase(eqp))
            {
                found = true
                def svId = PacConfig.getStringProperty("Port" + port.getNumber().toString() + "ProcessStateSV", "")
                getValueFromEqp(port.getPortId(), svId)
            }
        }

        if(!found)
        {
            throw new Exception("Track In EqpId: '$eqp' is not found in configured equipment list!!!")
        }
    }

    void getValueFromEqp(String eqp, String vidName)
    {
        def svid = secsControl.translateSvVid(vidName)
        SecsComponent< ? > svidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svid)))
        def request = new S1F3SelectedEquipmentStatusRequest(svidItem)
        def reply = secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
        def eqpValue = EqpUtil.getVariableData(reply.getData().getSV(0))

        def epModel = PacConfig.getStringProperty("EqModel", "")
        def isReady2Start = PacConfig.getStringArrayProperty("Secs.ProcessState." + epModel + ".States.Ready2Start", "", ",")

        if(eqpValue.length() > 0)
        {
            def ready = false
            logger.info("Equipment: '$eqp' process state is '$eqpValue'")
            for(value in isReady2Start)
            {
                if(value.equalsIgnoreCase(eqpValue))
                {
                    ready = true
                    logger.info("Equipment '$eqp' is ready!")
                    break
                }
            }
            if(!ready)
            {
                throw new Exception("Equipment '$eqp' is in Processing State...Not allowed to process Camstar transaction!")
            }
        }
    }
}