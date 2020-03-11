package SnrTrackOutLot

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.TrackOutLotRequest
import de.znt.pac.deo.annotations.*

@Deo(description='''
Dispatch to equipment scenario to track out lot
''')
class SnrTrackOutLot_1 {


   @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="MainEquipment")
    private CEquipment equipment
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
		TscEquipment mainEquipment=(TscEquipment) equipment
        
        def request = new TrackOutLotRequest(inputXmlDocument)
        def lotId = request.getContainerName()
        def eqId = request.getResourceName()
        def waferList = request.getTrackOutWaferList()
        if (waferList.size() == 0)
        {
            logger.info("CAMSTAR: TrackOut: " + lotId + "|" + eqId + "|" + request.getTrackOutQty())
        }
        else
        {
            logger.info("CAMSTAR: TrackOut: " + lotId + "|" + waferList.get(0).getWaferScribeNumber() + "|" + eqId + "|" + request.getTrackOutQty())
        }
        
        mainEquipment.getModelScenario().eqpMesTrackOutLot(cCamstarService, inputXmlDocument)
    }
}