package EqpRequestEstablishConnection_Secs

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F13EstablishCommunicationsRequestByHost

@CompileStatic
@Deo(description='''
Establish SECS connection
''')
class EqpRequestEstablishConnection_Secs_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        if(cEquipment.isSecs())
        {
            SecsGemService secsGemService = (SecsGemService) cEquipment.getExternalService()
            S1F13EstablishCommunicationsRequestByHost request = new S1F13EstablishCommunicationsRequestByHost()
            def reply = secsGemService.sendS1F13EstablishCommunicationsRequest(request)
            
            if(reply.getData())
            {
                def softwareRevision = reply.getData().getEquipmentInfo().getSoftwareRevision()
                logger.info("Equipment software version: " + softwareRevision)
                cEquipment.getPropertyContainer().setString("SoftwareRev", softwareRevision)
            }
        }
    }
}