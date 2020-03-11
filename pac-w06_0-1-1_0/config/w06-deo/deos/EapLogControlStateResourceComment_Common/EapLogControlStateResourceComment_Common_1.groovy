package EapLogControlStateResourceComment_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.ResourceCommentUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent

@CompileStatic
@Deo(description='''
W06 specific handling:<br/>
<b>Set resource comment for equipment control state changed</b>
''')
class EapLogControlStateResourceComment_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def resourceId = cEquipment.getSystemId()
        String lotId = null
        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        if (!lotList.empty)
        {
            lotId = lotList.get(0)
        }
        
        if (cEquipment.getControlState().isOffline())
        {
            ResourceCommentUtil.logControlStateResourceComment(cCamstarService, resourceId, lotId, "Offline")
        }
        else if (cEquipment.getControlState().isLocal())
        {
            ResourceCommentUtil.logControlStateResourceComment(cCamstarService, resourceId, lotId, "Online-Local")
        }
        else if (cEquipment.getControlState().isRemote())
        {
            ResourceCommentUtil.logControlStateResourceComment(cCamstarService, resourceId, lotId, "Online-Remote")
        }
    }
}