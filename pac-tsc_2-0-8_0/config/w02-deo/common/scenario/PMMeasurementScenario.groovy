package scenario

// Copyright (c) 2017, By ZNT AG.  All Rights Reserved.
//*********************************************************************
//
//                     ZNT AG
//                     Mautnerstraße 268
//                     84489 Burghausen
//                     GERMANY
//                     +49 (8677) 9880-0
//
// This software is furnished under a license and may be used
// and copied only in accordance with the terms of such license.
// This software or any other copies thereof in any form, may not be
// provided or otherwise made available, to any other person or company
// without written consent from ZNT.
//
// ZNT AG assumes no responsibility for the use or reliability of
// software which has been modified without approval.
//*********************************************************************
// Description:

import sg.znt.pac.domainobject.WipDataDomainObjectSet
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.scenario.ExecutionResult
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.DeoCategory
import de.znt.pac.deo.annotations.DeoParameter
import de.znt.pac.deo.annotations.DeoProvider
import de.znt.pac.deo.annotations.DeoTrigger
import de.znt.pac.deo.error.DeoExecutionException
import de.znt.pac.deo.trigger.DeoEventDispatcher

@DeoProvider(id=PMMeasurementScenario.PROVIDER,
icon="images/eclipse/16x16/share_project.gif",
categories=[@DeoCategory(id=PMMeasurementScenario.GROUP)])
class PMMeasurementScenario
{
    public static final String PROVIDER = "MeasurementScenario"
    public static final String GROUP = "PM"

    private static final String BINDING_EQUIPMENT = "Equipment"
    private static final String BINDING_CAMSTAR_SERVICE = "CamstarService"

    public static final String TRIGGER_MES_UPDATE_PM_INFO = "MesUpdatePMInfo"
    public static final String TRIGGER_MES_SUBMIT_ADHOC_WIP_DATA = "MesSubmitAdHocWipData"
    public static final String TRIGGER_VERIFY_PM_DUE_FOR_TOOL_SPC = "VerifyPMDueForToolSpc"
    private static final String BINDING_WIP_DATA_SET = "WipDataSet"
    private static final String BINDING_MAINTENANCE_REQUIREMENT_ID = "MaintenanceRequirementId"
    private static final String BINDING_TOOL_SPC_DUMMY_LOT_ID = "ToolSpcDummyLotId"
    private static final String BINDING_TOOL_SPC_SPEC_NAME = "ToolSpcSpecName"
    private static final String BINDING_SCENARIO_RESULT = "Result"
    
    public PMMeasurementScenario()
    {
    }

    /**
     * Update the PM info
     * Upon pac initialize, track in, track out 
     * @param deoEventDispatcher
     * @param equipment
     * @param camstarService
     * @param maintenanceRequirement
     * @throws DeoExecutionException
     */
    @DeoTrigger(id = PMMeasurementScenario.TRIGGER_MES_UPDATE_PM_INFO,
        input = [
            @DeoParameter(id = PMMeasurementScenario.BINDING_EQUIPMENT, type = CEquipment.class),
            @DeoParameter(id = PMMeasurementScenario.BINDING_CAMSTAR_SERVICE, type = CCamstarService.class),
            @DeoParameter(id = PMMeasurementScenario.BINDING_MAINTENANCE_REQUIREMENT_ID, type = String.class),
            @DeoParameter(id = PMMeasurementScenario.BINDING_TOOL_SPC_DUMMY_LOT_ID, type = String.class),
            @DeoParameter(id = PMMeasurementScenario.BINDING_TOOL_SPC_SPEC_NAME, type = String.class),
        ])
    public void mesUpdatPmInfo(DeoEventDispatcher deoEventDispatcher, CEquipment equipment, CCamstarService camstarService, 
        String maintenanceRequirement, String toolSpcDummyLotId, String toolSpcSpecName) throws DeoExecutionException
    {
        def bindingMap = [(BINDING_EQUIPMENT):equipment, (BINDING_CAMSTAR_SERVICE): camstarService, 
            (BINDING_MAINTENANCE_REQUIREMENT_ID):maintenanceRequirement,
            (BINDING_TOOL_SPC_DUMMY_LOT_ID):toolSpcDummyLotId,(BINDING_TOOL_SPC_SPEC_NAME):toolSpcSpecName]
        deoEventDispatcher.notifyEvent(PROVIDER, GROUP, TRIGGER_MES_UPDATE_PM_INFO, bindingMap)
    }
    
    /**
     * Periodically check (timer, track in, track out) if the PM is due for tool SPC, 
     * if due, pac send select recipe and wip data to gateway for adhoc wip data collection
     * Sync with Camstar for the due status when it meet next due time
     * @param deoEventDispatcher
     * @param equipment
     * @param camstarService
     * @param wipData
     * @throws DeoExecutionException
     */
    @DeoTrigger(id = PMMeasurementScenario.TRIGGER_VERIFY_PM_DUE_FOR_TOOL_SPC,
        input = [
            @DeoParameter(id = PMMeasurementScenario.BINDING_EQUIPMENT, type = CEquipment.class),
            @DeoParameter(id = PMMeasurementScenario.BINDING_CAMSTAR_SERVICE, type = CCamstarService.class)
        ])
    public void verifyPMDueForToolSpc(DeoEventDispatcher deoEventDispatcher, CEquipment equipment, CCamstarService camstarService) throws DeoExecutionException
    {
        def bindingMap = [(BINDING_EQUIPMENT):equipment, (BINDING_CAMSTAR_SERVICE): camstarService]
        deoEventDispatcher.notifyEvent(PROVIDER, GROUP, TRIGGER_VERIFY_PM_DUE_FOR_TOOL_SPC, bindingMap)
    }
    
        
    /**
     * Gateway to submit wip data to pac, pac to submit adhoc wip data, if there is no error, release the PM and allow the machine to run
     * @param deoEventDispatcher
     * @param equipment
     * @param camstarService
     * @param wipData
     * @throws DeoExecutionException
     */
    @DeoTrigger(id = PMMeasurementScenario.TRIGGER_MES_SUBMIT_ADHOC_WIP_DATA,
    input = [
        @DeoParameter(id = PMMeasurementScenario.BINDING_EQUIPMENT, type = CEquipment.class),
        @DeoParameter(id = PMMeasurementScenario.BINDING_CAMSTAR_SERVICE, type = CCamstarService.class),
        @DeoParameter(id = PMMeasurementScenario.BINDING_WIP_DATA_SET, type = WipDataDomainObjectSet.class)
    ], result = @DeoParameter(id = PMMeasurementScenario.BINDING_SCENARIO_RESULT, type = ExecutionResult.class))
    public ExecutionResult mesSubmitAdhocWipData(DeoEventDispatcher deoEventDispatcher, CEquipment equipment, CCamstarService camstarService, WipDataDomainObjectSet wipDataSet) throws DeoExecutionException
    {
        def bindingMap = [(BINDING_EQUIPMENT):equipment, (BINDING_CAMSTAR_SERVICE): camstarService, (BINDING_WIP_DATA_SET):wipDataSet]
        return deoEventDispatcher.notifyEvent(PROVIDER, GROUP, TRIGGER_MES_SUBMIT_ADHOC_WIP_DATA, bindingMap)
    }

}
