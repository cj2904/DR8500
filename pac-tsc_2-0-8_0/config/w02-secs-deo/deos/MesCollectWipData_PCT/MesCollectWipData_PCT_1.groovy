package MesCollectWipData_PCT

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.pac.domainobject.filter.FilterAllDomainObjects
import de.znt.pac.mapping.MappingManager
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.services.secs.dto.S2F13EquipmentConstantRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import groovy.transform.CompileStatic
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.EqpUtil
import sg.znt.services.camstar.outbound.TrackOutLotRequest

@CompileStatic
@Deo(description='''
MES request wip data from PCT eqp
''')
class MesCollectWipData_PCT_1
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

    @DeoBinding(id="WipDataType")
    private Integer wipDataType

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def lotId = mainEquipment.getCurrentLotId()

        def outbound = new TrackOutLotRequest(inputXmlDocument)
        def outboundLot = outbound.getContainerName()

        CLot lot = null
        try
        {
            if(lotId != null && lotId.length() > 0)
            {
                lot = cMaterialManager.getCLot(lotId)
            }
        }
        catch (Exception e)
        {
            e.printStackTrace()
        }

        if(lot == null)
        {
            logger.info("Lot '$lotId' not found in PAC!")
        }
        else
        {
            if(outboundLot.equalsIgnoreCase(lotId))
            {
                def trackOutWipData = lot.getEquipmentId() + "-" + lot.getId()
                def wipDataDO = wipDataDomainObjectManager.getWipDataSet(trackOutWipData)
                if(wipDataDO != null)
                {
                    def wipDataList = wipDataDO.getMoveOutWipDataItems()
                    def track = lot.getEquipmentId().substring(lot.getEquipmentId().length() - 1)
                    def schemaComponent

                    if(wipDataType == 1)
                    {
                        schemaComponent = mappingManager.getSchemaComponentByName("MES", track + "-TrackIn-WipData")
                    }
                    else if(wipDataType == 2)
                    {
                        schemaComponent = mappingManager.getSchemaComponentByName("MES", track + "-TrackOut-WipData")
                    }

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
                                    getWipDataValueFromEqp(trackOutWipData, wipDataName, vidName)
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
                    logger.info("No WIP Data configured in Camstar for Lot ID '$outboundLot'!")
                }
            }
            else
            {
                logger.info("Lot ID from Camstar Outbound '$outboundLot' is not match with PAC '$lotId'!")
            }
        }
    }

    void getWipDataValueFromEqp(String trackOutWipData, String wipDataName, String vidName)
    {
        def wipDataValue = null
        if (vidName.startsWith("SV@") || vidName.startsWith("DV@"))
        {
            def svid = secsControl.translateSvVid(vidName.replaceAll("SV@","").replaceAll("DV@",""))
            SecsComponent< ? > svidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svid)))
            def request = new S1F3SelectedEquipmentStatusRequest(svidItem)
            def reply = secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
            wipDataValue = EqpUtil.getVariableData(reply.getData().getSV(0))
        }
        else if (vidName.startsWith("EC@"))
        {
            def ecid = secsControl.translateEcVid(vidName.replaceAll("EC@",""))
            SecsComponent< ? > ecidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(ecid)))
            def request = new S2F13EquipmentConstantRequest(ecidItem)
            def reply = secsGemService.sendS2F13EquipmentConstantRequest(request)
            wipDataValue = EqpUtil.getVariableData(reply.getData().getECV(0))
        }
        else
        {
            //must start with SV@,DV@ or EC@
            throw new Exception("VID of $wipDataName is not defined with a valid VID format!")
        }

        if (wipDataValue != null)
        {
            def wipDataSet = wipDataDomainObjectManager.getWipDataSet(trackOutWipData)
            if (wipDataSet != null)
            {
                def wipDataList = wipDataSet.getAll(new FilterAllDomainObjects())
                for (wipData in wipDataList)
                {
                    if (wipData.getId().equalsIgnoreCase(wipDataName))
                    {
                        wipData.setValue(wipDataValue.toString())
                        logger.info("WIP Data Name: '$wipDataName'; WIP Data Value: '$wipDataValue'")
                    }
                }
            }
        }
    }
}