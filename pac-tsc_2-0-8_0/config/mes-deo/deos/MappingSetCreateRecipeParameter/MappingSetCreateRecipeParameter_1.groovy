package MappingSetCreateRecipeParameter

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager
import de.znt.pac.mapping.data.Schema
import de.znt.pac.mapping.data.SchemaComponent
import de.znt.pac.mapping.data.SchemaItem
import de.znt.pac.mapping.data.SchemaItemType
import de.znt.pac.mapping.view.MappingConfigurationModel
import de.znt.pac.models.ProcessNavigator
import de.znt.pac.users.PacUserManager
import de.znt.uiManager.Session
import de.znt.uiManager.SessionHelper
import de.znt.uiManager.UserInterface
import de.znt.zpers.transaction.ZPersSessionHelper
import groovy.transform.TypeChecked
import sg.znt.services.camstar.outbound.TrackInLotRequest

@Deo(description='''
Create adhoc wip data mapping set
''')
class MappingSetCreateRecipeParameter_1 {

	static final String CONST_MES = "MES"
	static final String CONST_RECIPE_PARAMETER = "RecipeParameter"
	
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="MappingManager")
	private MappingManager mappingManager
	
	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
	
	
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		TrackInLotRequest request = new TrackInLotRequest(inputXmlDocument)
		def recipeParams = request.getRecipeParamList()
		List<String> recipeParamList = new ArrayList<String>()
		for (var in recipeParams)
		{
			recipeParamList.add(var.getParamName())
		}
		
		createMappingSchema(recipeParamList)
		mappingManager.updateRuntimeSchemas()
		mappingManager.load()
		
		def mgr = PacUserManager.getPacUserManager().getUserInterfaceManager().getSessionManager()
		def allSession = mgr.getSessions()
		def allUi = PacUserManager.getPacUserManager().getUserInterfaceManager().getAllUserInterfaces()
		UserInterface uiProcessor = null
		
		for (var in allUi)
		{
			UserInterface obj = (UserInterface) var
			if (obj.getName() == PacUserManager.PROCESSOR_USER)
			{
				uiProcessor = obj
			}
		}

		ProcessNavigator pn = null	
		
		if (uiProcessor != null)
		{
            def uiContent = uiProcessor.getContent()
            if (uiContent != null)
            {
                def entries = uiContent.getModelEntries()
                for (var in entries)
                {
                    if (var.getModel() instanceof ProcessNavigator)
                    {
                        pn=(ProcessNavigator) var.getModel()
                    }
                }
            }			
		}

		for (s in allSession) 
		{
			def userName = s.getClientUserName()
			def uiName = s.getUserInterfaceName()
			def sName = s.getServiceName()
			
			if (uiName == PacUserManager.PROCESSOR_USER && pn !=null)
			{
				ModelPressButton runnable = new ModelPressButton(pn,s)
				Thread thread = new Thread(runnable, "Press Button")
				thread.start()
			}
		}
		
		
    }
	
	public class ModelPressButton implements Runnable
	{
		ProcessNavigator _model
		Session _session
		
		ModelPressButton(ProcessNavigator model,Session session)
		{
			_model = model
			_session = session
		}
		
		public void run()
		{
			MappingConfigurationModel mappingModel = _model.getModelByClass(MappingConfigurationModel.class)
			if (mappingModel != null)
			{
				def requireSession = ZPersSessionHelper.requireSession()
				try
				{
					def button = mappingModel.getReload()
					SessionHelper.setThreadSession(_session)
					button.fire()
				}
				catch(Exception e)
				{
					e.printStackTrace()
				}
				finally
				{
					ZPersSessionHelper.confirmIfTrue(requireSession)
				}
			}
		}
	}
	
	public void createMappingSchema(List<String> recipeParamList)
	{
		if(recipeParamList != null)
		{
			for (recipeParam in recipeParamList)
			{
				def schema = mappingManager.getSchema(CONST_MES)
				if(schema == null)
				{
					schema = new Schema(new ArrayList<SchemaComponent>(), CONST_MES, CONST_MES)
					logger.info("1 Schema [" + CONST_MES + "] Created")
					
					def schemaComponent = new SchemaComponent(new ArrayList<SchemaItem>(), CONST_RECIPE_PARAMETER, CONST_RECIPE_PARAMETER)
					logger.info("1 Schema Component [" + CONST_MES + "\\" + CONST_RECIPE_PARAMETER + "] Created")
		
					def schemaItem = new SchemaItem(recipeParam, recipeParam, SchemaItemType.STRING, "")
					schemaComponent.withSchemaItems(schemaItem)
					schema.withSchemaComponents(schemaComponent)
					
					mappingManager.addSchema(schema)
					logger.info("1 Schema Item [" + CONST_MES + "\\" + CONST_RECIPE_PARAMETER + "\\" + recipeParam + "] Created")
				}
				else
				{
					def schemaComponent = mappingManager.getSchemaComponentByName(CONST_MES, CONST_RECIPE_PARAMETER)
					if(schemaComponent == null)
					{
						schemaComponent = new SchemaComponent(new ArrayList<SchemaItem>(), CONST_RECIPE_PARAMETER, CONST_RECIPE_PARAMETER)
						logger.info("2 Schema Component [" + CONST_MES + "\\" + CONST_RECIPE_PARAMETER + "] Created")
						
						def schemaItem = new SchemaItem(recipeParam, recipeParam, SchemaItemType.STRING, "")
						schemaComponent.withSchemaItems(schemaItem)
						schema.withSchemaComponents(schemaComponent)
						
						mappingManager.save()
						logger.info("2 Schema Item [" + CONST_MES + "\\" + CONST_RECIPE_PARAMETER + "\\" + recipeParam + "] Created")
					}
					else
					{
						def schemaItem = mappingManager.getSchemaItemByName(CONST_MES, CONST_RECIPE_PARAMETER, recipeParam)
						if(schemaItem == null)
						{
							schemaItem = new SchemaItem(recipeParam, recipeParam, SchemaItemType.STRING, "")
							schemaComponent.withSchemaItems(schemaItem)
							mappingManager.save()
							logger.info("3 Schema Item [" + CONST_MES + "\\" + CONST_RECIPE_PARAMETER + "\\" + recipeParam + "] Created")
						}
					}
				}
			}
		}
	}
}