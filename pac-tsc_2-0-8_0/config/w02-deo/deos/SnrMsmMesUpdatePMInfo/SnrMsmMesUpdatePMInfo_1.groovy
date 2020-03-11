package SnrMsmMesUpdatePMInfo

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import scenario.PMMeasurementScenario
import sg.znt.pac.TscConfig
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher

@CompileStatic
@Deo(description='''
Dispatch the request to measurement scenario
''')
class SnrMsmMesUpdatePMInfo_1 {


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
        def maintenanceRequirement = TscConfig.getPMMaintenanceRequirementName()
        def toolSpcDummyLotId = TscConfig.getToolSpcDummyLotId()
        def toolSpcSpecName = TscConfig.getToolSpcSpecName()

        def sceMeas = new PMMeasurementScenario()
        sceMeas.mesUpdatPmInfo(deoEventDispatcher, equipment, camstarService, maintenanceRequirement, toolSpcDummyLotId, toolSpcSpecName)
    }
}