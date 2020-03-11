package EapVerifyLotExist

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.exception.LotNotExistException
import sg.znt.pac.material.CLotState
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify if lot exist in material manager
''')
class EapVerifyLotExist_1 {


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
    	def lotList = cMaterialManager.getCLotList(new LotFilterAll());
		if (lotList.size()==0)
		{
			throw new LotNotExistException();
		}
		
		boolean trackedInLotFound = false
		for (lot in lotList) 
		{
			//TODO: define list allow to start
			if (lot.isState(CLotState.TrackedIn))
			{
				trackedInLotFound = true
			}
		}
		
		if (!trackedInLotFound)
		{
			throw new LotNotExistException("No lot was tracked in.");
		}
    }
}