package EapLogAlarmResourceComment_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.TscConfig
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.ResourceCommentUtil
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsAlarm

@CompileStatic
@Deo(description='''
W06 specific handling:<br/>
<b>Set resource comment for equipment alarm</b>
''')
class EapLogAlarmResourceComment_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
    
    @DeoBinding(id="SecsAlarm")
    private SecsAlarm secsAlarm
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        String[] alarmList = TscConfig.getStringArrayProperty("W06.ResourceComment.Alarm.EnableForLogging", "", ",")
        if (Arrays.asList(alarmList).contains(secsAlarm.getId()) && secsAlarm.isSet())
        {
            def resourceId = cEquipment.getSystemId()
            String lotId = null
            def lotList = cMaterialManager.getCLotList(new LotFilterAll())
            if (!lotList.empty)
            {
                lotId = lotList.get(0)
            }
            def alarmText = secsAlarm.getText()
            ResourceCommentUtil.logAlarmResourceComment(cCamstarService, resourceId, lotId, alarmText)
        }
        else
        {
            //do not handle alarm resource comment logging
        }
    }
}