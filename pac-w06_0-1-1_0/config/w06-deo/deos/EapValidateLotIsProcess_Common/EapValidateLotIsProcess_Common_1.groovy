package EapValidateLotIsProcess_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.outbound.TrackOutLotRequest

@CompileStatic
@Deo(description='''
eap check track in lot is processed
''')
class EapValidateLotIsProcess_Common_1
{


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

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
        def outbound = new TrackOutLotRequest(inputXml)
        def outboundLot = outbound.getContainerName()
        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        for(lot in lotList)
        {
            if(lot.getId().equalsIgnoreCase(outboundLot))
            {
                def lotProcess = lot.getPropertyContainer().getBoolean("LotProcessed", false)
                if(!lotProcess)
                {
                    throw new Exception("Please run production line before perform track out for lot: $outboundLot")
                }
            }
        }
    }
}