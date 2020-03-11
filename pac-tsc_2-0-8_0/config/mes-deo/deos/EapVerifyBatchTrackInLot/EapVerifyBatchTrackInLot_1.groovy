package EapVerifyBatchTrackInLot

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify if the lot is within batch track in
''')
class EapVerifyBatchTrackInLot_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    @DeoBinding(id="MainEquipment")
    private CEquipment cEquipment
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def request = new CommonOutboundRequest(inputXmlDocument)
        def resourceName = request.getResourceName()
        def lotId = request.getContainerName()
        
        def container = cEquipment.getPropertyContainer()
        def batchTrackInLots = container.getStringArray(resourceName + "_BatchTrackInLots", new String[0]);
        def batchTotalQty = container.getInteger(resourceName + "_BatchTotalQty", -1);
        def batchId = container.getLong(resourceName + "_BatchID", new Long(-1));
        
        if (batchTrackInLots.length >0)
        {
            def found = false
            for (lot in batchTrackInLots) 
            {
                if (lotId.equalsIgnoreCase(lot))
                {
                    found = true
                    break
                }
            }
            if (!found)
            {
                throw new Exception("Lot '" + lotId + "' is not in the batch '" + batchTrackInLots + "'!")
            }
        }
    }
}