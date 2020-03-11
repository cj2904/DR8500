package SnrMsmEapVerifyMesPMMaintStatus

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import scenario.PMMeasurementScenario
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher

@CompileStatic
@Deo(description='''
Dispatch the request to measurement scenario
''')
class SnrMsmEapVerifyMesPMMaintStatus_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="DeoEventDispatcher")
    private DeoEventDispatcher deoEventDispatcher

    @DeoBinding(id="Equipment")
    private CEquipment equipment
    
    @DeoBinding(id="CamstarService")
    private CCamstarService camstarService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def sceMeas = new PMMeasurementScenario()
        sceMeas.verifyPMDueForToolSpc(deoEventDispatcher, equipment, camstarService)

    }
}