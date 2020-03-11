package EapValidateMesOutBoundLotId

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked;
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.TrackInLotRequest

@Deo(description='''
Validate the lot ID with pac, if not found throw ItemNotFoundException exception
''')
class EapValidateMesOutBoundLotId_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument
    
    /**
     *
     */
    @DeoExecute
    @TypeChecked
    public void execute()
    {
        def outboundRequest = new TrackInLotRequest(inputXmlDocument)
        def newLotId = outboundRequest.getContainerName()
        cMaterialManager.getCLot(newLotId)
    }
}