package SnrRequestVariableByListWithMapping

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.SgdConfig
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.TscEquipment;
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.RequestWipDataRequest
import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager

@Deo(description='''
Scenario dispatcher
''')
class SnrRequestVariableByListWithMapping_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="TscEquipment")
    private CEquipment equipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
	
	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
    
    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		TscEquipment cEquipment = (TscEquipment) equipment
    	RequestWipDataRequest request = new RequestWipDataRequest(inputXmlDocument)
		def lotId = request.getContainerName()
		def serviceName = request.getWipDataServiceName()
		def cLot = cMaterialManager.getCLot(lotId)
		if(cLot != null)
		{
			cLot.getPropertyContainer().setString("NotSupport_WIP_Data", "")
			cLot.clearWipData(serviceName)
			cLot.clearWipDataValue(serviceName)
			List<String> variableList = new ArrayList<String>()
			def wipDataList = request.getWipDataList()
			String[] wipDataHeader = null
            def wipDataRow = new LinkedHashMap<String, String>()
            List<MessWipDataValue> wipDataItems = new ArrayList<MessWipDataValue>()
			for (wipData in wipDataList) 
			{
				if(wipDataHeader == null)
				{
					wipDataHeader = wipData.getHeader()
				}
				//cLot.addWipData(serviceName, wipData.getValueAsStringArray())
                def lotResolveName = getLotMappingName(wipData.getWipDataNameName())
                def value = ""
                if (lotResolveName.length()>0)
                {
                    value = wipData.getWipDataNameName() + ":" + cLot.getPropertyContainer().getString(lotResolveName, "")
                    logger.info("Resolving '" + wipData.getWipDataNameName() + "' to '" + lotResolveName + "' with value '" + value + "'")
                }
                else
                {
                    variableList.add(wipData.getWipDataNameName())
                }
                //wipDataRow.put(wipData.getWipDataNameName(), value)
                wipDataItems.add(new MessWipDataValue(wipData.getValueAsStringArray(), wipData.getWipDataNameName(), value))
			}
			
			cLot.setWipDataHeader(serviceName, wipDataHeader);
			
			if(variableList != null)
			{
				List<String> valueList = cEquipment.getModelScenario().eqpRequestVariableByList(variableList)
				
				if(valueList != null)
				{
                    for (int i=0; i<valueList.size();i++)
                    {
                        def t_wipName = variableList.get(i);
                        def t_wipValue = valueList.get(i);
						if(t_wipValue.endsWith("NotSupport"))
						{
							def notSupportWipData = cLot.getPropertyContainer().getString("NotSupport_WIP_Data", "")
							cLot.getPropertyContainer().setString("NotSupport_WIP_Data", notSupportWipData + "\n" + t_wipValue)
							continue
						}
						
                        for (wipDataItem in wipDataItems)
                        {
							if (wipDataItem._wipDataName.equalsIgnoreCase(t_wipName))
                            {
                                wipDataItem.setWipDataValue(t_wipValue)
                            }
                        }
                    }
                    
                    for (wipDataItem in wipDataItems)
                    {
                        cLot.addWipData(serviceName, wipDataItem._wipDataInfo)
                        cLot.setWipDataValue(serviceName, wipDataItem._wipDataValue)
                    }
				}
				else
				{
					//empty value list return, 
					//do not throw exception, 
					//because operator can still enter data collection manually
				}
			}
			else
			{
				//no data collection is required, skip
			}
		}
		else
		{
			throw new ValidationFailureException("", "Could not find " + lotId + " in pac")
		}
    }
    
    private String getLotMappingName(String camstarDataCollectionName)
    {
        def mappingName = SgdConfig.getStringProperty("Mapping_LotDataCollection","Mapping_LotDataCollection")
        if (mappingName.length()==0)
        {
            return ""
        }
        def mappingSet = mappingManager.getMappingSet(mappingName)
        if (mappingSet == null)
        {
            return ""
        }
        def mappingList = mappingSet.getMappings()
        for (mapping in mappingList)
        {
            def sourceList = mapping.getSources()
            for (src in sourceList)
            {
                def srcSchemaItem = mappingManager.getSchemaItem(src.getSchemaId(), src.getSchemaComponentId(), src.getSchemaItemId())
                def target = mapping.getTarget()
                def tgtSchemaItem = mappingManager.getSchemaItem(target.getSchemaId(), target.getSchemaComponentId(), target.getSchemaItemId())
                
                if (camstarDataCollectionName.equalsIgnoreCase(srcSchemaItem.getName()))
                {
                    def targetLotName = tgtSchemaItem.getName()
                    return targetLotName
                }
            }
        }
        return ""
    }
    
    class MessWipDataValue
    {
        String[] _wipDataInfo;
        String _wipDataName;
        String _wipDataValue;
        
        public MessWipDataValue(String[] wipDataInfo, String wipDataName, String wipDataValue)
        {
            _wipDataInfo = wipDataInfo;
            _wipDataName = wipDataName;
            _wipDataValue= wipDataValue;
        }
        
        public void setWipDataValue(String wipDataValue)
        {
            _wipDataValue = wipDataValue;
        }
    }
}