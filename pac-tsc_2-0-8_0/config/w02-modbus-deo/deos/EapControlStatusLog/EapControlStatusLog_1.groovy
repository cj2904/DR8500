package EapControlStatusLog

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import sg.znt.services.modbus.W02ModBusService
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.PacConfig
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.camstar.semisuite.service.dto.SetResourceCommentRequest

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapControlStatusLog_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="CEquipment")
    private CEquipment equipment

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="ControlStatusAddress")
    private int controlStatusAddress = 6048

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def status = w02ModBusService.readHoldingRegisterIntValue(controlStatusAddress)
        def eqpstatus = equipment.getPropertyContainer().getInteger("ControlStatus",-1)

        logger.info("Modbus Control Status [" + status + "] EAP Equipment Control Status ["+ eqpstatus + "]")
        if(status!=eqpstatus)
        {
            equipment.getPropertyContainer().setInteger("ControlStatus",status)
            for(int i=1 ; i <=8 ; i++)
            {
                def eqLogicalId = PacConfig.getStringProperty("C"+i + ".SystemId", "")
                if(eqLogicalId.length() > 0)
                {
                    def eventTime = CDateFormat.getFormatedDate(new Date())
                    def request = new SetResourceCommentRequest(eqLogicalId, "Event '"+ (status==1?'Online':'Offline')+"' at equipment $eqLogicalId @ $eventTime")
                    def reply = cCamstarService.setResourceComment(request)
                    if(reply.isSuccessful())
                    {
                        logger.info(reply.getResponseData().getCompletionMsg())
                    }
                    else
                    {
                        CamstarMesUtil.handleNoChangeError(reply)
                    }
                }
            }
        }
    }
}