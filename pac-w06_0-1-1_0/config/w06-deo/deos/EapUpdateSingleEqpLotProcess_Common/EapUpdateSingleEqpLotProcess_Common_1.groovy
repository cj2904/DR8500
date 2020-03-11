package EapUpdateSingleEqpLotProcess_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll

@CompileStatic
@Deo(description='''
eap update lot had undergo processing
''')
class EapUpdateSingleEqpLotProcess_Common_1
{


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        for(lot in lotList)
        {
            lot.getPropertyContainer().setBoolean("LotProcessed", true)
        }
    }
}