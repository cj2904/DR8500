package EapVerifyModbusSoftStart

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.exception.ModbusException
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.modbus.W02ModBusService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify if softstart allow to start, 
otherwise throw exception
''')
class EapVerifyModbusSoftStart_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="W02ModBusService")
	private W02ModBusService modBusService

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager materialManager

	@DeoBinding(id="ModBusEvent")
	private ModBusEvent modbusEvent

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def allLot = materialManager.getCLotList(new LotFilterAll())
		if (allLot.size()==0)
		{
			throw new ModbusException("No lot to start!", ModbusException.NO_LOT_START)
		}
		else
		{
			for (lot in allLot) 
			{
				def allRecipe =lot.getAllRecipeObj()
				for (recipe in allRecipe) 
				{
					if (recipe.getEquipmentLogicalId().equalsIgnoreCase(modbusEvent.getChamber()))
					{
						return
					}
				}
			}
			throw new ModbusException("No lot to start!", 1)
		}
		//check material manager got lot to track in
		//verify selected recipe or recipe parameter is correct
		//how to mapping parameter 
    }
}