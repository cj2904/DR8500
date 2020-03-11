package W02PCTEqpCheckProcessState

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.pac.mapping.MappingManager
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
Describe your DEO here.<br/>
update process state for eqp
''')
class W02PCTEqpCheckProcessState_1
{

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

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

        def eqpValue
        def eqpA = PacConfig.getStringProperty("Track1EqpName", "")
        def eqpB = PacConfig.getStringProperty("Track2EqpName", "")
        def vEqpA = PacConfig.getStringProperty("Track1VEqpName", "")
        def vEqpB = PacConfig.getStringProperty("Track2VEqpName", "")

        if(eqp.equalsIgnoreCase(eqpA) || eqp.equalsIgnoreCase(vEqpA))
        {
            def track1 = "Track1Status"
            getValueFromEqp(eqpA, track1)
        }
        else if(eqp.equalsIgnoreCase(eqpB) || eqp.equalsIgnoreCase(vEqpB))
        {
            def track2 = "Track2Status"
            getValueFromEqp(eqpB, track2)
        }
        else
        {
            throw new Exception("Track in eqp: '$eqp' is not found!")
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
            if (eqpValue.equalsIgnoreCase("1"))
            {
                throw new Exception("Equipment '$eqp' is in Processing State...Not allowed to process Camstar transaction!")
            }
//            mainEquipment.updateProcessState()
//            logger.info("Equipment: '$eqp' process state successful update.")
        }
    }
}