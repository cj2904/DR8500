package SnrTrackInSucceed

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.EquipmentIdentifyService
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.scenario.TscEqModelScenario
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Dispatch the track in succeed to scenario
''')
class SnrTrackInSucceed_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="EquipmentIdentifyService")
    private EquipmentIdentifyService equipmentIdentifyService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outboundRequest = new W02TrackInLotRequest(inputXmlDocument)
        def lotId = outboundRequest.getContainerName()
        def lot = cMaterialManager.getCLot(lotId)

        List<WipDataDomainObject> wipDataItems = null
        if(lot.getWipDataByEquipment(cEquipment.getSystemId())!=null)
        {
            wipDataItems = lot.getWipDataByEquipment(cEquipment.getSystemId()).getTrackOutWipDataItems()
        }

        def hm = new HashMap()
        hm.put("LotId", lot.getId())
        hm.put("Equipment", cEquipment.getSystemId())

        ((TscEqModelScenario) cEquipment.getModelScenario()).eqpMesTrackInSucceed(hm, cCamstarService, inputXmlDocument)
    }
}