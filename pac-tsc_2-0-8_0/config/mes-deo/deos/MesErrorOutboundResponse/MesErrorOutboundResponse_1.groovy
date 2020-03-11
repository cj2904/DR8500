package MesErrorOutboundResponse
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.services.camstar.inbound.CamstarInboundResponse
import de.znt.util.error.ErrorManager
import groovy.transform.TypeChecked
import sg.znt.pac.TscConstants
import sg.znt.pac.exception.TrackInVirtualQtyException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.pac.util.PacUtils
import sg.znt.services.zwin.ZWinApiException

@Deo(description='''
Response error outbound message
''')
class MesErrorOutboundResponse_1
{

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

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
        if(exception instanceof TrackInVirtualQtyException)
        {
            cEquipment.getPropertyContainer().setBoolean(TscConstants.LOT_IS_VIRTUAL_RUN_CANCEL_TRACKIN, true)
            logger.info("TrackInVirtualQtyException Reply Camstar Ok.")
            CamstarInboundResponse response2 = new CamstarInboundResponse("Reply from PAC (Success)")
            logger.info("TrackInVirtualQtyException ResultXmlDocument >>> " + response2.createCamstarXmlDoc())
            return response2.createCamstarXmlDoc()
        }
        cEquipment.getPropertyContainer().setBoolean(TscConstants.LOT_IS_VIRTUAL_RUN_CANCEL_TRACKIN, false)

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
            response.setExceptionData(((ZWinApiException) cause).getErrorCode(), CamstarMesUtil.trimCamstarErrorMessage(cause.getMessage()))
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