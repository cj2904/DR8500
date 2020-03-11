package MesErrorOutboundResponse_Eqp

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.PacUtils
import sg.znt.services.zwin.ZWinApiException
import de.znt.pac.deo.annotations.*
import de.znt.services.camstar.inbound.CamstarInboundResponse
import de.znt.util.error.ErrorManager

@Deo(description='''
Response error outbound message
''')
class MesErrorOutboundResponse_Eqp_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="Exception")
	private Throwable exception
    /**
     *
     */
    @DeoExecute(result="ResultXmlDocument")
	@TypeChecked
    public String execute()
    {
    	def cause = exception.getCause()
		while(cause != null)
		{
			if(cause instanceof ZWinApiException)
			{
				break
			}
			cause = cause.getCause()
		}
		
    	CamstarInboundResponse response = new CamstarInboundResponse()
		if(cause instanceof ZWinApiException)
		{
            def message = cause.getMessage()
            if (cause.getName().length()>0)
            {
                message =  "[" + cause.getName() + "] " + message
            }
			response.setExceptionData(((ZWinApiException) cause).getErrorCode(), CamstarMesUtil.trimCamstarErrorMessage(message))
		}
		else
		{
			if(cause != null)
			{
				response.setExceptionData("-1", CamstarMesUtil.trimCamstarErrorMessage(PacUtils.getErrorMessage(cause)))
			}
			else
			{
				response.setExceptionData("-1", CamstarMesUtil.trimCamstarErrorMessage(PacUtils.getErrorMessage(exception)))
			}
		}
		
        ErrorManager.handleError(exception, this)
        
		logger.info("ResultXmlDocument >>> " + response.createCamstarXmlDoc())
		
		return response.createCamstarXmlDoc()//toXmlString()
    }
}