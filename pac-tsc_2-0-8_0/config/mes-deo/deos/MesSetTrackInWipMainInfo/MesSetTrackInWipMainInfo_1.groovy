package MesSetTrackInWipMainInfo

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.W02TrackInWipMainRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Set track in wip main info to equipment
''')
class MesSetTrackInWipMainInfo_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outboundRequest = new W02TrackInWipMainRequest(inputXmlDocument)
        def resourceName = outboundRequest.getResourceName()
        def lotList = outboundRequest.getLotList()
        def totalQty = outboundRequest.getTotalSelectedWaferQty()
		def equipmentLotCount = outboundRequest.getEquipmentLotCount()
        logger.info("CAMSTAR: TrackInWipMain: " + lotList + "|" + totalQty + "|" + resourceName)
        def container = cEquipment.getPropertyContainer()
        container.setStringArray(resourceName + "_BatchTrackInLots", lotList.toArray(new String[0]));
        container.setInteger(resourceName + "_BatchTotalQty", PacUtils.valueOfInteger(totalQty, 0));
        container.setLong(resourceName + "_BatchID", System.currentTimeMillis());
		container.setInteger(resourceName + "_BatchEquipmentLotCount", PacUtils.valueOfInteger(equipmentLotCount, 0));
    }
}