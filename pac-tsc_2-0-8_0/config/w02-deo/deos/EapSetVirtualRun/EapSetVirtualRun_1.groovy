package EapSetVirtualRun

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Set virtual run after track in
''')
class EapSetVirtualRun_1 {


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
        def virtualId = cEquipment.getVirtualSystemId()
        if (virtualId.length()>0)
        {
            if (request.getResourceName().equalsIgnoreCase(virtualId))
            {
                if (!cEquipment.getSystemId().equalsIgnoreCase(request.getResourceName()))
                {
                    logger.info("Equipment is switch from '" + cEquipment.getSystemId() + "' to '" + virtualId + "'")
                    cEquipment.setVirtualRun(true)
                }                
            }
            else
            {
                if (!cEquipment.getSystemId().equalsIgnoreCase(request.getResourceName()))
                {
                    logger.info("Equipment is switch from '" + cEquipment.getSystemId() + "' to '" + request.getResourceName() + "'")
                    cEquipment.setVirtualRun(false)
                }
            }
        }        
    }
}