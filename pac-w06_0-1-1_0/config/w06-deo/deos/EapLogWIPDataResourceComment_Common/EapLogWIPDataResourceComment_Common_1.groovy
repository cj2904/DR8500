package EapLogWIPDataResourceComment_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
W06 specific handling:<br/>
<b>Set resource comment for Camstar WIP data collection</b>
''')
class EapLogWIPDataResourceComment_Common_1 {


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
        
        //TODO: get WIP data from domain object?
    }
}