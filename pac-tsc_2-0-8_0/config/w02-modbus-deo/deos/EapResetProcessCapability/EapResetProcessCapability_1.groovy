package EapResetProcessCapability

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintResponse
import sg.znt.camstar.semisuite.service.dto.SetEqpProcessCapabilityRequest
import sg.znt.camstar.semisuite.service.dto.SetEquipmentMaintRequest
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.property.PropertyFilterSearchByParameterRegExp
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.ZTypes.ZStringArray
import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.parameter.ParameterSet
import de.znt.pac.parameter.ParameterSetContainer
import de.znt.pac.parameter.ParameterSetUtilities;

@CompileStatic
@Deo(description='''
Describe your DEO here.<br/>
<b>You can use HTML tags.</b>
''')
class EapResetProcessCapability_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modBusEvent

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
	
	@DeoBinding(id="MainEquipment")
	private CEquipment mainEquipment

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def eqLogicalId = PacConfig.getStringProperty(modBusEvent.getChamber() + ".SystemId", "")

        def req = new GetEquipmentMaintRequest()
        req.getInputData().getObjectToChange().setName(eqLogicalId)

        GetEquipmentMaintResponse res = new GetEquipmentMaintResponse()
        res = cCamstarService.getEquipmentMaint(req)
        def parenteqp = res.getResponseData().getObjectChanges().getParentResource().getName()
        logger.info("EapResetProcessCapability parenteqp " + parenteqp )

        def request = new GetEquipmentMaintRequest(parenteqp)
        def reply = cCamstarService.getEquipmentMaint(request)
        List<String> capabilityList = new ArrayList<String>()
        if (reply.isSuccessful())
        {
            def items = reply.getResponseData().getObjectChanges().getEqpProcessCapability().getEqpProcessCapabilityItems()
            while(items.hasNext())
            {
                def item = items.next()
                capabilityList.add(item.getProcessCapability().getName())
            }
        }

        if(capabilityList.size()>0)
        {
            def request2 = new SetEqpProcessCapabilityRequest()
            request2.getInputData().setResource(parenteqp)
            for (var in capabilityList)
            {
                logger.info("EapResetProcessCapability SetEqpProcessCapabilityRequest var " + var )
                def item = request2.getInputData().getDetails().addDetailsItem()
                item.getProcessCapability().setName(var)
                item.setAvailability("TRUE")
                item.setActivationStatus("TRUE")
            }
            def reply2=cCamstarService.setEqpProcessCapability(request2)
        }
		
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
				if (eqLogicalId.equalsIgnoreCase(values.getStringValue(j)))
				{
					mainEqp = ParameterSetUtilities.getParameterValue(pvsContainer, i).getId()
					break
				}
			}
			if (mainEqp.length()>0)
			{
				break
			}
		}
		
		if (mainEqp.length()>0)
		{
			mainEqp = removeExpression(mainEqp, "_ChildEquipmentsId")
			mainEqp = removeExpression(mainEqp, "Properties@")
			
			def request3 = new SetEquipmentMaintRequest(mainEqp)
			request3.getInputData().getObjectChanges().initChildParameter("tscLastRunCapability")
			request3.getInputData().getObjectChanges().getChildParameter("tscLastRunCapability").setValue("")
			
			def reply3 = cCamstarService.setEquipmentMaint(request3)
			if (reply3.isSuccessful())
			{
				logger.info(reply3.getResponseData().getCompletionMsg())
			}
			else
			{
				throw new Exception(reply3.getExceptionData().getErrorDescription())
			}
		}
		else
		{
			throw new Exception("Could not resolve Main Equipment from Machine Equipment")
		}
    }
	
	String removeExpression(String key, String exp)
	{
		return key.replace(exp, "")
	}
}