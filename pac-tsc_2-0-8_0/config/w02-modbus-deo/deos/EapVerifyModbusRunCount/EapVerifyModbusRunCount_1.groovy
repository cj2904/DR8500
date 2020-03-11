package EapVerifyModbusRunCount

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.SgdConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.exception.ModbusRunCountInvalidException
import sg.znt.pac.machine.CEquipment;
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.modbus.W02ModBusService
import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify run count for modbus equipment
''')
class EapVerifyModbusRunCount_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="InputXml")
	private String inputXml

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

    @DeoBinding(id="ModBusService")
    private W02ModBusService modbusService
	
	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def request = new CommonOutboundRequest(inputXml)
		def lotId = request.getContainerName()
		def eqId = request.getResourceName()
		def allLot = cMaterialManager.getCLotList(new LotFilterAll())
		for (lot in allLot)
		{
			if (lot.getId().equals(lotId))
			{
				def allLotRecipe = lot.getAllRecipeObj()
				for(recipe in allLotRecipe)
				{
					def runCount = recipe.getRunCount()
					def childEqId = recipe.getEquipmentLogicalId()
					def chamberId = SgdConfig.findChamberIdBySystemId(modbusService.getChambers(), childEqId, eqId, cEquipment)
					def currentRunCount = lot.getPropertyContainer().getInteger(childEqId+TscConstants.LOT_ATTR_CHAMBER_RUN_COUNT_POSTFIX, 0)
					
                    logger.info("childEqId " + childEqId)
                    logger.info("runCount " + runCount)
                    logger.info("currentRunCount " + currentRunCount)
                    
                    if (recipe.getRunCount()>currentRunCount)
					{
						throw new ModbusRunCountInvalidException("Require $runCount run, but currently only have $currentRunCount run at $chamberId:$childEqId.")
					}
				}
				break
			}
		}

    }
}