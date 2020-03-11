package MesGetFactoryMaintRequest

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.camstar.semisuite.service.dto.GetFactoryMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetFactoryMaintResponse
import sg.znt.pac.SgdConfig
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.subject.SgdCamstarQueueSubject

@Deo(description='''
Get factory setting from Camstar
''')
class MesGetFactoryMaintRequest_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
    	def factory = SgdConfig.getCamstarFactory()
		GetFactoryMaintRequest request = new GetFactoryMaintRequest(factory)
		GetFactoryMaintResponse reply = cCamstarService.getFactoryMaint(request)
		if(reply.isSuccessful())
		{
			String outboundTimeout = reply.getResponseData().getObjectChanges().getOutboundWCFTimeout()
			def subjIter = cCamstarService.getSubjectsIterator()
			while(subjIter.hasNext())
			{
				def subj = subjIter.next()
				if(subj instanceof SgdCamstarQueueSubject)
				{
					subj.setMsgTimeout(Integer.parseInt(outboundTimeout))
				}
			}
		}
    }
}