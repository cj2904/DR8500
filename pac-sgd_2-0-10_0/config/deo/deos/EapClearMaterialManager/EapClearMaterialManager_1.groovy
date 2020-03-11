package EapClearMaterialManager

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll

@CompileStatic
@Deo(description='''
Clear all lot in material manager
''')
class EapClearMaterialManager_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def allLot = cMaterialManager.getCLotList(new LotFilterAll())
		for (lot in allLot) 
		{
			cMaterialManager.removeCLot(lot)
		}
    }
}