package EapCloneBatchLotsWipData_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.domainobject.filter.FilterAllDomainObjects
import groovy.transform.CompileStatic
import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.services.camstar.CCamstarService

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Clone wip data for all lot(s) with same batch for measurement equipment</b>
''')
class EapCloneBatchLotsWipData_Common_1
{
	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService

	@DeoBinding(id="WipDataDomainObjectManager")
	private WipDataDomainObjectManager wipDataDomainObjectManager

	@DeoBinding(id="CEquipment")
	private CEquipment cEquipment

	/**
	 *
	 */
	@DeoExecute
	public void execute()
	{
		CLot firstCLot = null
		def firstLotBatchId = null
		def cLotList = cMaterialManager.getCLotList(new LotFilterAll())
		for (cLot in cLotList)
		{
			if(cLot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH, false))
			{
				firstCLot = cLot
				firstLotBatchId = cLot.getBatchId()
				break;
			}
		}

		if (firstCLot != null)
		{
			def firstLotWipDataSet = wipDataDomainObjectManager.getWipDataSet(firstCLot.getId())
			if (firstLotWipDataSet != null)
			{
				def firstLotWipDataList = firstLotWipDataSet.getAll(new FilterAllDomainObjects())
				for (cLot in cLotList)
				{
					if(!cLot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH, false) && (cLot.getBatchId() == firstLotBatchId))
					{
						logger.info("Cloning WIP data to lot '" + cLot.getId() + "' begins...")
						cloneLotWipData(cLot, firstLotWipDataList)
					}
				}
			}
			else
			{
				logger.info("WIP data set is null for lot '" + firstCLot.getId() + "', skip cloning WIP data...")
			}
		}
	}

	private void cloneLotWipData(CLot curCLot, List <WipDataDomainObject> firstLotWipDataList)
	{
		def curLotWipDataSet = wipDataDomainObjectManager.getWipDataSet(curCLot.getId())
		List <WipDataDomainObject> wdDomainObjList = curLotWipDataSet.getAll(new FilterAllDomainObjects())

		for (firstLotWD in firstLotWipDataList)
		{
			def targetWD = firstLotWD.getId()
			if (firstLotWD.isHidden())
			{
				def targetFound = false
				for (curLotWD in wdDomainObjList)
				{
					if (curLotWD.getId().equalsIgnoreCase(targetWD))
					{
						targetFound = true
						curLotWD.setValue(firstLotWD.getValue())
						break
					}
				}
				if (!targetFound)
				{
					logger.error("WIP data item '$targetWD' not found in lot '" + curCLot.getId() + "'!")
				}
			}
			else
			{
				logger.info("WIP data item '$targetWD' IsHidden is 'False', skip clonning to Lot '" + curCLot.getId() + "' WIP data...")
			}
		}
	}
}