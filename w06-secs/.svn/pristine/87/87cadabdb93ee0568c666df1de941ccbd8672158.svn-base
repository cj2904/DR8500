package EapCollectWipData_Semco

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import de.znt.pac.domainobject.filter.FilterAllDomainObjects
import de.znt.pac.mapping.MappingManager
import de.znt.services.secs.SecsGemService
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll

@CompileStatic
@Deo(description='''
semco furnace collect wip data RUN_ID by event trigger
''')
class EapCollectWipData_Semco_1
{

    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager

    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def report = secsEvent.getReport("ProcessFinished")

        if(report != null)
        {
            def portList = mainEquipment.getPortList()
            def lotList = cMaterialManager.getCLotList(new LotFilterAll())
            def tubeId = report.getPropertyContainer().getValueAsString("DV@TubeID", "")
            def batchId = report.getPropertyContainer().getValueAsString("DV@BatchID", "")
            def runId = ""
            def found = false

            for(port in portList)
            {
                if(port.getNumber().toString().equalsIgnoreCase(tubeId))
                {
                    found = true
                    runId = report.getPropertyContainer().getValueAsString("DV@RunID", "")
                    logger.info("Process Finsihed RunID value: '$runId'")
                }
            }

            if(found)
            {
                for(lot in lotList)
                {
                    if(lot.getPropertyContainer().getString("EqpBatchId", "").equalsIgnoreCase(batchId))
                    {
                        def trackOutWipData = lot.getEquipmentId() + "-" + lot.getId()
                        logger.info("the wip data domain do: '$trackOutWipData'")
                        def wipDataDO = wipDataDomainObjectManager.getWipDataSet(trackOutWipData)
                        if(wipDataDO != null)
                        {
                            def wipDataList = wipDataDO.getMoveOutWipDataItems()

                            def schemaComponent = mappingManager.getSchemaComponentByName("MES", "EventWipData")
                            if(schemaComponent != null)
                            {
                                def itemList = schemaComponent.getSchemaItems()
                                for(item in itemList)
                                {
                                    def wipDataFound = false
                                    for(wipData in wipDataList)
                                    {
                                        def wipDataName = wipData.getId()
                                        if(item.getName().trim().equalsIgnoreCase(wipDataName))
                                        {
                                            wipDataFound = true
                                            if(!item.getUnit()?.trim())
                                            {
                                                logger.info("VID is not defined for " + item.getName())
                                                continue
                                            }

                                            def vidName = item.getUnit().trim()
                                            setWipDataValue(trackOutWipData, runId, vidName, wipDataName)
                                            break
                                        }
                                    }
                                    if(wipDataFound == false)
                                    {
                                        throw new Exception("WIP Data Name '" + item.getName() + "' in Schema 'MES' Component 'WipData' is not configured in Camstar!")
                                    }
                                }
                            }
                            else
                            {
                                logger.info("Schema 'MES' with Component 'WipData' has no item configured!")
                            }
                        }
                        else
                        {
                            logger.info("No WIP Data configured in Camstar for Lot ID '" + lot.getId() + "'!")
                        }
                    }
                }
            }
            else
            {
                logger.info("Receive Process Finsihed Event with runId: '$runId'.")
            }
        }
    }

    void setWipDataValue(String trackOutWipData, String runId, String vidName, String wipDataName)
    {
        if (runId != null || runId.length() > 0)
        {
            def wipDataSet = wipDataDomainObjectManager.getWipDataSet(trackOutWipData)
            if (wipDataSet != null)
            {
                def wipDataList = wipDataSet.getAll(new FilterAllDomainObjects())
                for (wipData in wipDataList)
                {
                    if (wipData.getId().equalsIgnoreCase(wipDataName))
                    {
                        wipData.setValue(runId)
                        logger.info("WIP Data Name: '$wipDataName'; WIP Data Value: '$runId'")
                    }
                }
            }
        }
    }
}