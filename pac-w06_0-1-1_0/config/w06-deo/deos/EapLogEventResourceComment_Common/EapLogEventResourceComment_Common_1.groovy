package EapLogEventResourceComment_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import sg.znt.services.camstar.CCamstarService
import sg.znt.pac.TscConfig
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.ResourceCommentUtil

@CompileStatic
@Deo(description='''
W06 specific handling:<br/>
<b>Set resource comment for equipment event</b>
''')
class EapLogEventResourceComment_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        String[] eventList = TscConfig.getStringArrayProperty("W06.ResourceComment.Event.EnableForLogging", "", ",")
        if (Arrays.asList(eventList).contains(secsEvent.getCeid()))
        {
            def resourceId = cEquipment.getSystemId()
            String lotId = null
            def lotList = cMaterialManager.getCLotList(new LotFilterAll())
            if (!lotList.empty)
            {
                lotId = lotList.get(0)
            }
            def eventName = secsEvent.getName()
            ResourceCommentUtil.logEventResourceComment(cCamstarService, resourceId, lotId, eventName)
        }
        else
        {
            //do not handle event resource comment logging
        }
    }
}