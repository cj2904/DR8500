package MesGetEquipmentMaint

import groovy.transform.TypeChecked

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintRequest
import sg.znt.camstar.semisuite.service.dto.GetEquipmentMaintResponse
import sg.znt.pac.AppProcessAutomationController
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.machine.CEquipmentImpl
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.machine.ProcessMachine2

@Deo(description='''
Get MES equipment maintenance
''')
class MesGetEquipmentMaint_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
	@TypeChecked
    public void execute()
    {
		def machines = AppProcessAutomationController.getProcessAutomationController().getMachineManager().getAllMachines()
		for (machine in machines) 
		{
			if(machine instanceof ProcessMachine2)
			{
			    CEquipment cEquipment = new CEquipmentImpl((ProcessMachine2) machine)
                def systemId = cEquipment.getSystemId()
                if (systemId != null & systemId.length()>0)
                {
                    GetEquipmentMaintRequest request = new GetEquipmentMaintRequest(systemId)
                    GetEquipmentMaintResponse reply = cCamstarService.getEquipmentMaint(request)
                    if(reply.isSuccessful())
                    {
                        cEquipment.setEquipmentFamily(reply.getResponseData().getObjectChanges().getResourceFamily().getName())
                    }
                    else
                    {
                        logger.error(reply.getExceptionData().getErrorDescription())
                    }
                }				
			}
		}
    }
}