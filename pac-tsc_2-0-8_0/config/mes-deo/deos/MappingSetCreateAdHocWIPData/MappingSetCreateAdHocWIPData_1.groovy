package MappingSetCreateAdHocWIPData

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.ZModels.ZModel
import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager
import de.znt.pac.mapping.data.Schema
import de.znt.pac.mapping.data.SchemaComponent
import de.znt.pac.mapping.data.SchemaItem
import de.znt.pac.mapping.data.SchemaItemType
import de.znt.pac.mapping.view.MappingConfigurationModel
import de.znt.pac.users.PacUserManager
import de.znt.zpers.transaction.ZPersSessionHelper
import groovy.transform.TypeChecked
import sg.znt.camstar.semisuite.service.dto.GetAdHocWIPDataRequest
import sg.znt.camstar.semisuite.service.dto.GetAdHocWIPDataResponse
import sg.znt.pac.TscConfig
import sg.znt.pac.machine.TscEquipment
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.TrackInLotRequest

@Deo(description='''
Create adhoc wip data mapping set
''')
class MappingSetCreateAdHocWIPData_1 {

	static final String CONST_MES = "MES"

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService
	
	@DeoBinding(id="MappingManager")
	private MappingManager mappingManager
	
	@DeoBinding(id="TscEquipment")
	private TscEquipment cEquipment
	
	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		def equipmentId = cEquipment.getSystemId()
		TrackInLotRequest outbound = new TrackInLotRequest(inputXmlDocument)
		
		String[] adHocWipDataPoint = TscConfig.getAdHocWipDataPoint(outbound.getStep())
		if(adHocWipDataPoint.length > 0)
		{
			for (point in adHocWipDataPoint)
			{
				def wipDataName = TscConfig.getAdHocWipDataName(point)
				createMappingSchema(equipmentId, wipDataName)
			}
			
			mappingManager.updateRuntimeSchemas()
			mappingManager.load()
			
			def user = PacUserManager.getPacUserManager().getUser(PacUserManager.PROCESSOR_USER)
			def modelEntries = user.getUserInterface().getAllModelEntries()
			for (entry in modelEntries)
			{
				if(entry.getName().equals("ProcessNavigator"))
				{
					ModelPressButton runnable = new ModelPressButton((ZModel) entry.getModel())
					Thread thread = new Thread(runnable, "Press Button")
					thread.start()
				}
			}
		}
    }
	
	public class ModelPressButton implements Runnable
	{
		ZModel _model
		
		ModelPressButton(ZModel model)
		{
			_model = model
		}
		
		public void run()
		{
			MappingConfigurationModel mappingModel = _model.getModelByClass(MappingConfigurationModel.class)
			if (mappingModel != null)
			{
				def requireSession = ZPersSessionHelper.requireSession()
				try
				{
					mappingModel.getReload().fire()
				}
				finally
				{
					ZPersSessionHelper.confirmIfTrue(requireSession)
				}
			}
		}
	}
	
	public void createMappingSchema(String equipmentId, String adHocWIPData)
	{
		List<String> wipDataList = getAdHocWIPDataList(equipmentId, adHocWIPData)
		if(wipDataList != null)
		{
			for (wipData in wipDataList) 
			{
				def schema = mappingManager.getSchema(CONST_MES)
				if(schema == null)
				{
					schema = new Schema(new ArrayList<SchemaComponent>(), CONST_MES, CONST_MES)
					logger.info("1 Schema [" + CONST_MES + "] Created")
					
					def schemaComponent = new SchemaComponent(new ArrayList<SchemaItem>(), adHocWIPData, adHocWIPData)
					logger.info("1 Schema Component [" + CONST_MES + "\\" + adHocWIPData + "] Created")

					def schemaItem = new SchemaItem(wipData, wipData, SchemaItemType.STRING, "")
					schemaComponent.withSchemaItems(schemaItem)
					schema.withSchemaComponents(schemaComponent)
					
					mappingManager.addSchema(schema)
					logger.info("1 Schema Item [" + CONST_MES + "\\" + adHocWIPData + "\\" + wipData + "] Created")
				}
				else
				{
					def schemaComponent = mappingManager.getSchemaComponentByName(CONST_MES, adHocWIPData)
					if(schemaComponent == null)
					{
						schemaComponent = new SchemaComponent(new ArrayList<SchemaItem>(), adHocWIPData, adHocWIPData)
						logger.info("2 Schema Component [" + CONST_MES + "\\" + adHocWIPData + "] Created")
						
						def schemaItem = new SchemaItem(wipData, wipData, SchemaItemType.STRING, "")
						schemaComponent.withSchemaItems(schemaItem)
						schema.withSchemaComponents(schemaComponent)
						
						mappingManager.save()
						logger.info("2 Schema Item [" + CONST_MES + "\\" + adHocWIPData + "\\" + wipData + "] Created")
					}
					else
					{
						def schemaItem = mappingManager.getSchemaItemByName(CONST_MES, adHocWIPData, wipData)
						if(schemaItem == null)
						{
							schemaItem = new SchemaItem(wipData, wipData, SchemaItemType.STRING, "")
							schemaComponent.withSchemaItems(schemaItem)
							mappingManager.save()
							logger.info("3 Schema Item [" + CONST_MES + "\\" + adHocWIPData + "\\" + wipData + "] Created")
						}
					}
				}
			}
		}
	}
	
	public List<String> getAdHocWIPDataList(String equipmentId, String adHocWIPData)
	{
		GetAdHocWIPDataRequest request = new GetAdHocWIPDataRequest()
		request.getInputData().getObjectType().setName("Equipment")
		request.getInputData().setObjectName(equipmentId)
		request.getInputData().getWIPDataSetup().setName(adHocWIPData)
		
		GetAdHocWIPDataResponse reply = cCamstarService.GetAdHocWIPData(request)
		if(reply.isSuccessful())
		{
			List<String> adHocWIPDataList = new ArrayList<String>()
			def detailsSelectionItems = reply.getResponseData().getDetailsSelection().getDetailsSelectionItems()
			while(detailsSelectionItems.hasNext())
			{
				def item = detailsSelectionItems.next()
				adHocWIPDataList.add(item.getWIPDataName().getName())
			}
			
			return adHocWIPDataList
		}
		else
		{
			logger.error("Error occurred: " + reply.getExceptionData().getErrorDescription())
		}
		
		return null
	}
}