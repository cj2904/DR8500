package EapVerifyVirtualRun

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.exception.TrackInVirtualQtyException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify if track in equipment is virtual run.
''')
class EapVerifyVirtualRun_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="InputXml")
    private String inputXml

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new W02TrackInLotRequest(inputXml)
        int waferQty = Integer.parseInt(request.getQty2())
        //int trackInWaferQty = request.getLotTrackInWaferList().size()
        int trackInWaferQty = Integer.parseInt(request.getWaferQty())

        int realRunQty = -1

        def wipDataList = request.getWipDataItemList()
        if (wipDataList.size()>0)
        {
            for (var in wipDataList)
            {
                if (var.WIP_DATA_NAME.contains("Real Run Qty"))
                {
                    realRunQty = PacUtils.valueOfInteger(var.WIP_DATA_VALUE, -1)
                }
            }
        }

        if (realRunQty > -1)
        {
            if (trackInWaferQty>realRunQty)
            {
                throw new TrackInVirtualQtyException()
            }
        }
    }
}