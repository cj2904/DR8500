package SnrRequestVariableByList

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.TypeChecked
import sg.znt.pac.exception.ValidationFailureException
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.services.camstar.outbound.RequestWipDataRequest

@Deo(description='''
Scenario dispatcher
''')
class SnrRequestVariableByList_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="TscEquipment")
    private TscEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
	
	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
    	RequestWipDataRequest request = new RequestWipDataRequest(inputXmlDocument)
		def lotId = request.getContainerName()
		def serviceName = request.getWipDataServiceName()
		def cLot = cMaterialManager.getCLot(lotId)
		if(cLot != null)
		{
			cLot.clearWipData(serviceName)
			cLot.clearWipDataValue(serviceName)
			List<String> variableList = new ArrayList<String>()
			def wipDataList = request.getWipDataList()
			String[] wipDataHeader = null
			for (wipData in wipDataList) 
			{
				if(wipDataHeader == null)
				{
					wipDataHeader = wipData.getHeader()
				}
				cLot.addWipData(serviceName, wipData.getValueAsStringArray())
				variableList.add(wipData.getWipDataNameName())
			}
			
			cLot.setWipDataHeader(serviceName, wipDataHeader);
			
			if(variableList != null)
			{
				List<String> valueList = cEquipment.getModelScenario().eqpRequestVariableByList(variableList)
				
				if(valueList != null)
				{
					for (value in valueList) 
					{
						cLot.setWipDataValue(serviceName, value)
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
}