package EapVerifyTestWorkflow

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.pac.TscConstants;
import sg.znt.services.camstar.outbound.TrackInLotRequest
import sg.znt.services.modbus.W02ModBusService;
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent;
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Verify if there is any test workflow to be completed after add acid, if only verifySpecFlag is 1 then check 9-9-12-7 acid
''')
class EapVerifyTestWorkflow_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

	@DeoBinding(id="InputXmlDocument")
	private String inputXmlDocument
	
	@DeoBinding(id="VerifySpecFlag")
	private boolean verifySpecFlag = false
	
	@DeoBinding(id="W02ModBusService")
	private W02ModBusService w02ModBusService
	
	@DeoBinding(id="AcidTypeAddress")
	private int acidTypeAddress = 6876
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
    	def outbound = new TrackInLotRequest(inputXmlDocument)
		def lotWorkflow = outbound.getParamValue("Workflow")
		def testWorkflow = outbound.getParamValue(TscConstants.EQP_ATTR_TEST_LOT_WORKFLOW)
		if (testWorkflow.length()>0)
		{
			if (!lotWorkflow.equalsIgnoreCase(testWorkflow))
			{
				if (verifySpecFlag)
				{
					def shortVal = w02ModBusService.readHoldingRegisterIntValue(acidTypeAddress)
					if (shortVal == 1)
					{
						throw new Exception("Acid Type '$shortVal' at modbus address '$acidTypeAddress' require TestWorkFlow '$testWorkflow' to be completed! Please perform test workflow before proceed with Lot Track In!")
					}
				}
				else
				{
					throw new Exception("TestWorkFlow '$testWorkflow' has not been completed. Please perform test workflow before proceed with Lot Track In!")
				}
			}
			else
			{
				logger.info("Lot ID '" + outbound.getContainerName() + "' workflow match with TestWorkflow '$testWorkflow'.")
			}
		}
    }
}