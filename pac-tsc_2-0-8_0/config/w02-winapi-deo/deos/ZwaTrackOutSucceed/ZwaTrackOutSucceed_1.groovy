package ZwaTrackOutSucceed

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConstants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.zwin.ZWinApiService
import de.znt.pac.deo.annotations.*
import elemental.json.JsonObject

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class ZwaTrackOutSucceed_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="ZWinApiService")
    private ZWinApiService zWinApiService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def clot = materialManager.getCLotList(new LotFilterAll()).get(0)

        Map<String, Object> param = new HashMap<String, Object>();
        param.put("LotId", clot.getId())
        param.put("EquipmentId", cEquipment.getSystemId())
        param.put("Message", "")

        if(!clot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH, false))
        {
            logger.info("Skip SnrTrackOutSucceed for lot '" + clot.getId() + "' since it's not first batch!")
        }
        else
        {
            JsonObject jo = zWinApiService.buildCommandMessage("CompleteLot", param)
            zWinApiService.sendTcpMessage(jo)    
        }
    }
}