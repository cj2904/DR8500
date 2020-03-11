package SnrTrackInLot_SPT

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConstants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@Deo(description='''
Dispatch to equipment scenario to track in lot
''')
class SnrTrackInLot_SPT_1 {


   @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="MainEquipment")
    private CEquipment equipment
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
        def outboundRequest = new W02TrackInLotRequest(inputXmlDocument)
        def cLot = cMaterialManager.getCLot(outboundRequest.getContainerName())
        def firstLotInBatch = cLot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH, true)
		TscEquipment mainEquipment = (TscEquipment) equipment
		mainEquipment.getModelScenario().eqpMesTrackInLot(cCamstarService, inputXmlDocument)
    }
}