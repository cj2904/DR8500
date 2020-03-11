package EapFileBaseEventMesAddResourseComment

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetResourceCommentRequest
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService

@CompileStatic
@Deo(description='''
File base event message add resource comment
''')
class EapFileBaseEventMesAddResourseComment_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="Command")
    private String command

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def eqpId = cEquipment.getSystemId()
        def lotId = ""
        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        if(lotList.size()>0)
        {
            lotId = lotList.get(0).getId()
            addResourceComment(eqpId, lotId)
        }
        else
        {
            throw new Exception("There is no lot id in PAC!")
        }
    }

    void addResourceComment(String eqpId, String lotId)
    {
        def eventTime = CDateFormat.getFormatedDate(new Date())
        def request = new SetResourceCommentRequest(eqpId, "'$command' Event triggered for lot id '$lotId' @ $eventTime")
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