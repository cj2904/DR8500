package EapResetInkProbeCount

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.SetResourceCommentRequest
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.equipment.file.EquipmentFileService

@CompileStatic
@Deo(description='''
To set Camstar Eqp Reserved 3 parameter.
''')
class EapResetInkProbeCount_1
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())
    
    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="EquipmentFileService")
    private EquipmentFileService equipmentFileService

    @DeoBinding(id="WorkFlowName")
    private String workflowName = "V-PROBE-AOI"

    @DeoBinding(id="Reason")
    private String reason

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def eqpId = cEquipment.getSystemId()
        if(workflowName.length() > 0)
        {
            def request = new SetEquipmentMaintRequest(eqpId)
            request.getInputData().getObjectChanges().initChildParameter("tscEqpReserved3")
            request.getInputData().getObjectChanges().getChildParameter("tscEqpReserved3").setValue(workflowName)

            def reply = cCamstarService.setEquipmentMaint(request)
            if(reply.isSuccessful())
            {
                logger.info(reply.getResponseData().getCompletionMsg())
                addResourceComment(eqpId)
            }
            else
            {
                logger.error(reply.getExceptionData().getErrorDescription())
            }
        }
        else
        {
            logger.warn("No Test WorkFlow is configured to set in Camstar...")
        }
    }

    void addResourceComment(String eqpId)
    {
        def eventTime = CDateFormat.getFormatedDate(new Date())
        def request = new SetResourceCommentRequest(eqpId, "Event '$reason' for TestWorkFlow at equipment $eqpId @ $eventTime")
        def reply = cCamstarService.setResourceComment(request)
        if(reply.isSuccessful())
        {
            logger.info(reply.getResponseData().getCompletionMsg())
        }else
        {
            CamstarMesUtil.handleNoChangeError(reply)
        }
    }
}