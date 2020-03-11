package EqpCollectWipData_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.ItemNotFoundException
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.pac.mapping.MappingManager
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.services.secs.dto.S2F13EquipmentConstantRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.EqpUtil
import sg.znt.services.camstar.outbound.TrackOutLotRequest

@CompileStatic
@Deo(description='''
W06 common function:<br/>
<b>Collect Wip Data base on mapping manager using SECS S1F3 Selected Equipment Status Request and store in PAC persistence</b>
''')
class EqpCollectWipData_Common_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml
	
	@DeoBinding(id="SchemaComponentName")
	private String schemaCompName = "WipData"
	
	private static String SCHEMA_NAME = "MES"

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	TrackOutLotRequest outbound = new TrackOutLotRequest(inputXml)
		def eqpId = outbound.getResourceName()
		def lotId = outbound.getContainerName()
		CLot cLot = null
		try
		{
			cLot = cMaterialManager.getCLot(lotId)
		}
		catch (ItemNotFoundException e)
		{
			throw new Exception("Lot '$lotId' is not found in pac Material Manager!")
//			List<CLot> cLotList = cMaterialManager.getCLotList(new LotFilterAll())
//			if (cLotList.size() > 0)
//			{
//				cLot = cLotList.get(0)
//			}
		}
		
		def schemaComp = mappingManager.getSchemaComponentByName(SCHEMA_NAME, schemaCompName)
		if (schemaComp == null)
		{
			logger.info("Schema '$SCHEMA_NAME' with Component '$schemaCompName' is not found in Mapping Manager, skip WIP Data Collection")
			return
		}
		
		def wipDataDomainObject = wipDataDomainObjectManager.getDomainObject(eqpId + "-" + lotId)
		if (wipDataDomainObject != null)
		{
			def moveOutWipDataItems = wipDataDomainObject.getMoveOutWipDataItems()
			
			def itemList = schemaComp.getSchemaItems()
			
			for (item in itemList)
			{
				def mappingMesWipDataName = item.getName().trim()
				def wipDataMapped = false
				for (moveOutWDItem in moveOutWipDataItems)
				{
					if (moveOutWDItem.isHidden())
					{
						if (moveOutWDItem.getId().equalsIgnoreCase(mappingMesWipDataName))
						{
							wipDataMapped = true
							def wipDataValue = getWipDataValueFromEqp(mappingMesWipDataName, item.getUnit().trim())
							moveOutWDItem.setValue(wipDataValue)
							break
						}
					}
				}
				if (!wipDataMapped)
				{
					throw new Exception("WIP Data Name '$mappingMesWipDataName' in Schema '$SCHEMA_NAME' Component '$schemaCompName' is not modelled in MES Camstar WIP Data Setup!")
				}
			}
		}
		else
		{
			logger.info("WIP Data Domain Object is null, skip collect WIP Data...")
		}
    }
	
	private String getWipDataValueFromEqp(String wipDataName, String vidName)
	{
		def wipDataValue = null
		if (vidName.startsWith("SV@") || vidName.startsWith("DV@"))
		{
			def svid = secsControl.translateSvVid(vidName.replaceAll("SV@","").replaceAll("DV@",""))
			SecsComponent< ? > svidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svid)))
			def s1f3 = new S1F3SelectedEquipmentStatusRequest(svidItem)
			def replyS1F3 = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3)
			wipDataValue = EqpUtil.getVariableData(replyS1F3.getData().getSV(0))
		}
		else if (vidName.startsWith("EC@"))
		{
			def ecid = secsControl.translateEcVid(vidName.replaceAll("EC@",""))
			SecsComponent< ? > ecidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(ecid)))
			def s2f13 = new S2F13EquipmentConstantRequest(ecidItem)
			def replyS2F13 = secsGemService.sendS2F13EquipmentConstantRequest(s2f13)
			wipDataValue = EqpUtil.getVariableData(replyS2F13.getData().getECV(0))
		}
		else
		{
			/*
			 * must start with SV@,DV@ or EC@
			 */
			throw new Exception("VID of '$wipDataName' is not defined with a valid VID format!")
		}
		return wipDataValue
	}
}