package EapSetTestLotWorkflow

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.pac.TscConstants;
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.property.PropertyFilterSearchByParameterRegExp
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.modbus.W02ModBusService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.ZTypes.ZStringArray
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.parameter.ParameterSet
import de.znt.pac.parameter.ParameterSetContainer
import de.znt.pac.parameter.ParameterSetUtilities

@CompileStatic
@Deo(description='''
set test workflow when add acid event is trigger
''')
class EapSetTestLotWorkflow_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="TestWorkFlow")
	private String testWorkFlow
	
	@DeoBinding(id="CCamstarService")
	private CCamstarService cCamstarService
	
	@DeoBinding(id="ModbusEvent")
	private ModBusEvent modbusEvent
	
	@DeoBinding(id="MainEquipment")
	private CEquipment mainEquipment
	
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	if (testWorkFlow.length() > 0)
		{
			def chamber = modbusEvent.getChamber()
			def childEqpSystemId = PacConfig.getStringProperty(chamber + ".SystemId", "")
			if (childEqpSystemId.length() > 0)
			{
				def eqpContainer = mainEquipment.getPropertyContainer()
				def regExp = ".*_ChildEquipmentsId"
				
				ParameterSet pvs = new ParameterSet()
				ParameterSetContainer pvsContainer = new ParameterSetContainer(pvs)
				
				PropertyFilterSearchByParameterRegExp filter = new PropertyFilterSearchByParameterRegExp(regExp)
				
				eqpContainer.copyPropertiesTo(pvsContainer, filter)
				
				def mainEqp = ""
				def containerSize = ParameterSetUtilities.getParameterCount(pvsContainer)
				for (def i=0; i < containerSize; i++)
				{
					ZStringArray values = (ZStringArray) ParameterSetUtilities.getParameterValue(pvsContainer, i).getAttributeValue()
					for (int j=0; j<values.getArrayLength(); j++)
					{
						if (childEqpSystemId.equalsIgnoreCase(values.getStringValue(j)))
						{
							mainEqp = ParameterSetUtilities.getParameterValue(pvsContainer, i).getId()
							mainEqp = removeExpression(mainEqp, "_ChildEquipmentsId")
							mainEqp = removeExpression(mainEqp, "Properties@")
							break
						}
					}
					if (mainEqp.length()>0)
					{
						break
					}
				}
				
				def request = new SetEquipmentMaintRequest(mainEqp)
				request.getInputData().getObjectChanges().initChildParameter(TscConstants.EQP_ATTR_TEST_LOT_WORKFLOW)
				request.getInputData().getObjectChanges().getChildParameter(TscConstants.EQP_ATTR_TEST_LOT_WORKFLOW).setValue(testWorkFlow)
				
				def reply = cCamstarService.setEquipmentMaint(request)
				if (reply.isSuccessful())
				{
					logger.info(reply.getResponseData().getCompletionMsg())
				}
				else
				{
					logger.error(reply.getExceptionData().getErrorDescription())
				}
			}
			else
			{
				logger.error("Could not resolve Main Equipment from Machine Manager")				
			} 
		}
		else
		{
			logger.warn("No WorkFlow is configured to set in Camstar Eqp Test Lot Workflow...")
		}
    }
	
	String removeExpression(String key, String exp)
	{
		return key.replace(exp, "")
	}
}