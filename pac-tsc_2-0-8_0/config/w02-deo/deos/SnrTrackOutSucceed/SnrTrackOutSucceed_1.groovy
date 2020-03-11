package SnrTrackOutSucceed

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConstants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.TrackOutLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Dispatch the track out succeed to scenario
''')
class SnrTrackOutSucceed_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        TscEquipment tEquipment = (TscEquipment) cEquipment
        def outboundRequest = new TrackOutLotRequest(inputXmlDocument)
        def lotId = outboundRequest.getContainerName()
        def eqId = outboundRequest.getResourceName()
        def lot = cMaterialManager.getCLot(lotId)
        
        logger.info("CAMSTAR: CompleteLot: " + lotId + "|" + eqId)
        
        def hm = new HashMap()
        hm.put("LotId", lotId)
        hm.put("Execute", lot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH, false))
        
        tEquipment.getModelScenario().eqpMesTrackOutSucceed(hm, cCamstarService, inputXmlDocument)
    }
}