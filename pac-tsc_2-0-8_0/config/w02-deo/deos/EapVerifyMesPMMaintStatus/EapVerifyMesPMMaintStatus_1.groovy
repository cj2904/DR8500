package EapVerifyMesPMMaintStatus

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import scenario.PMMeasurementScenario
import sg.znt.pac.domainobject.EquipmentPMDomainObject
import sg.znt.pac.domainobject.EquipmentPMDomainObjectManager
import sg.znt.pac.exception.PMPastDueException
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.DateUtils
import sg.znt.services.camstar.CCamstarService
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.trigger.DeoEventDispatcher
import de.znt.pac.domainobject.filter.FilterAllDomainObjects

@CompileStatic
@Deo(description='''
Verify MES PM Maintenance status
''')
class EapVerifyMesPMMaintStatus_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    @DeoBinding(id="Equipment")
    private CEquipment equipment
    
    @DeoBinding(id="EquipmentPMDomainObjectManager")
    EquipmentPMDomainObjectManager equipmentPMDomainObjectManager
    
    @DeoBinding(id="DeoEventDispatcher")
    private DeoEventDispatcher deoEventDispatcher
    
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def equipmentPMSet = equipmentPMDomainObjectManager.getEquipmentPMSet(equipment.getSystemId())
        if (equipmentPMSet == null)
        {
            logger.error("Equipment PM set '" + equipment.getSystemId() + "' not found!")
        }
        else
        {
            def equipmentPMList = equipmentPMSet.getAll(new FilterAllDomainObjects())
            for (pmInfo in equipmentPMList)
            {
                if (pmInfo.getPMType().equalsIgnoreCase(EquipmentPMDomainObject.PM_TYPE_TOOL_SPC))
                {
                    def maintenanceRequirement = pmInfo.getId()
                    
                    def nextDateDue = DateUtils.convertDefaultFormatDateToLong(pmInfo.getNextDateDue())
                    if (pmInfo.isPastDue() || DateUtils.isDue(nextDateDue))
                    {
                        if (!pmInfo.isExecuted())
                        {
                            //sync with Camstar for the past due status
                            logger.info("PM '" + pmInfo.getId() + "' is due, but first sync with camstar status")
                            new PMMeasurementScenario().mesUpdatPmInfo(deoEventDispatcher, equipment, cCamstarService, maintenanceRequirement, pmInfo.getLotId(), pmInfo.getSpecName())
                        }
                    }
                    
                    if (pmInfo.isPastDue())
                    {
                        if (!pmInfo.isExecuted())
                        {
                            throw new PMPastDueException(pmInfo.getId() + " is past due!", pmInfo, equipment)
                        }
                        else
                        {
                            logger.error("PM '" + pmInfo.getId() + "' is past due but is in progress!")
                        }
                    }
                }
            }
        }        
    }
}