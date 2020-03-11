package EapVerifyTrackInLotQty

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
eap verify track in lot for semco per batch must be 2 only
''')
class EapVerifyTrackInLotQty_1
{

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
        def trackInLot = new W02TrackInLotRequest(inputXml)
        def batchLot = cEquipment.getPropertyContainer().getStringArray(trackInLot.getResourceName() + "_BatchTrackInLots", new String[0])
        def batchQty = batchLot.size()
        if(batchQty != 2)
        {
            throw new Exception("Batch TrackInLots must be 2 lots. LotList: '$batchLot' with size: '$batchQty'.")
        }
    }
}