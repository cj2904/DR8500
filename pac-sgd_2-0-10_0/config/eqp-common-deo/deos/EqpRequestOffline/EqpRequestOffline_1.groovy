package EqpRequestOffline

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService;
import groovy.transform.TypeChecked

@Deo(description='''
Send offline request to gateway, gateway will then release control back to MMI
''')
class EqpRequestOffline_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
    	secsGemService.requestOffline()
    }
}