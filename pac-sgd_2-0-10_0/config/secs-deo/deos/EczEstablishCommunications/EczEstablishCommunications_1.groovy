package EczEstablishCommunications

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F13EstablishCommunicationsRequestByHost

@Deo(description='''
Establish communication to request the model and equipment version
''')
class EczEstablishCommunications_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def req = new S1F13EstablishCommunicationsRequestByHost()
		secsGemService.sendS1F13EstablishCommunicationsRequest(req)
    }
}