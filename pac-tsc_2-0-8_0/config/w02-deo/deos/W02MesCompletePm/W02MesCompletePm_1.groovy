package W02MesCompletePm

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent;
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Complete PM if lot was not on hold after track out
''')
class W02MesCompletePm_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

	@DeoBinding(id="ModBusEvent")
	private ModBusEvent modbusEvent

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def lotList = cMaterialManager.getCLotList(new LotFilterAll())
		def childEqLogicalId = modbusEvent.getChamber()
		def childEq = cEquipment.getChildEquipment(childEqLogicalId)
		for (lot in lotList) 
		{
			def allRecipe = lot.getAllRecipeObj()
			for (recipe in allRecipe) 
			{
				if (recipe.getEquipmentLogicalId().equals(childEqLogicalId))
				{
					//TODO: Wai NamHandle complete PM
					break
				}
			}
		}
    }
}