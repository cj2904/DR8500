package ModbusHandleAlnWipData

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.SetResourceCommentRequest
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.modbus.W02ModBusService

@CompileStatic
@Deo(description='''
<b>To update wip data object for X,Y, and Theta</b><br/>
''')
class ModbusHandleAlnWipData_1
{


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="MaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDO

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def lotList = materialManager.getCLotList(new LotFilterAll())

        def lot = lotList.get(0)
        def lotId = lot.getId()
        def eqpId = lot.getEquipmentId()
        boolean wipDataMatch = false

        logger.info("Lot '$lotId' is running at '$eqpId'")

        //        def wipData = lot.getWipDataByEquipment(eqpId)
        //        def moveOutWipData = wipData.getMoveOutWipDataItems()
        def wipData = eqpId + "-" + lotId
        def moveOutWipDataSet = wipDataDO.getWipDataSet(wipData)

        if(moveOutWipDataSet != null)
        {
            def eventTime = CDateFormat.getFormatedDate(new Date())
            def moveOutWipData = moveOutWipDataSet.getMoveOutWipDataItems()
            def mappingName = eqpId + "-WipData"
            def wipDataMapping = mappingManager.getMappingSet(mappingName)
            def mappings = wipDataMapping.getMappings()
            for (mapping in mappings)
            {
                def source = mapping.getSources().get(0)
                def sourceItem = mappingManager.getSchemaItem(source)
                def sourceAddr = Integer.parseInt(sourceItem.getUnit())
                def modBusValue = w02ModBusService.readHoldingRegisterIntValue(sourceAddr).toString()

                def target = mapping.getTarget()
                def targetItem = mappingManager.getSchemaItem(target).getName()

                for (wipDataItem in moveOutWipData)
                {
                    def wipDataItemKey = wipDataItem.getId()
                    if(wipDataItemKey.equalsIgnoreCase(targetItem))
                    {
                        wipDataMatch = true
                        wipDataItem.setValue(modBusValue)
                        logger.info("Set WIP data '$wipDataItemKey' value '$modBusValue' for lot '$lotId' at Equipment '$eqpId' @ '$eventTime'.")
                        def request = new SetResourceCommentRequest(eqpId, "Set WIP data '$wipDataItemKey' value '$modBusValue' for lot '$lotId' at Equipment '$eqpId' @ '$eventTime'.")
                        def reply = cCamstarService.setResourceComment(request)
                        if(reply.isSuccessful())
                        {
                            logger.info(reply.getResponseData().getCompletionMsg())
                        }
                        else
                        {
                            try
                            {
                                CamstarMesUtil.handleNoChangeError(reply)
                            }catch(Exception e)
                            {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }

            if(wipDataMatch==false)
            {
                logger.error("No WIP data is found in Mapping Sets for lot '$lotId' at Equipment '$eqpId' @ '$eventTime'")
            }
        }
    }
}