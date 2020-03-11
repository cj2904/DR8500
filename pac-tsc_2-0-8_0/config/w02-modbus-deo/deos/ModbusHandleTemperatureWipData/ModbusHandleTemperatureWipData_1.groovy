package ModbusHandleTemperatureWipData

import groovy.transform.CompileStatic

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import sg.znt.camstar.semisuite.service.dto.SetResourceCommentRequest
import sg.znt.pac.TscConfig
import sg.znt.pac.date.CDateFormat
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.CamstarMesUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.modbus.W02ModBusService
import sg.znt.services.modbus.SgdModBusServiceImpl.ModBusEvent
import de.znt.pac.deo.annotations.*

@CompileStatic
@Deo(description='''
Read the temperature wip data during start button is pressed
''')
class ModbusHandleTemperatureWipData_1
{
    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="W02ModBusService")
    private W02ModBusService w02ModBusService

    @DeoBinding(id="MaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="CEquipment")
    private CEquipment equipment

    @DeoBinding(id="ModBusEvent")
    private ModBusEvent modBusEvent

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def chamberId = modBusEvent.getChamber()
        def chamberEqpId = TscConfig.getStringProperty(chamberId + ".SystemId", "")
        CLot lot = null
        long batchId = -1L
		HashMap<String, String> wipDataMapping = null
        def lotList = materialManager.getCLotList(new LotFilterAll())
        for (lt in lotList)
        {
            def eqId = lt.getEquipmentId()
            def childEquipments = equipment.getPropertyContainer().getStringArray(eqId + "_ChildEquipmentsId", new String[0])
            def isTrackInEqp = false
            for (ce in childEquipments)
            {
                if (ce.equalsIgnoreCase(chamberEqpId))
                {
                    isTrackInEqp = true
                    break
                }
            }
            if (isTrackInEqp)
            {
                def lotBatchId = lt.getPropertyContainer().getLong("BatchID", new Long(-1)).longValue()
                if (batchId == -1)
                {
                    batchId = lotBatchId
                }
                logger.info("Lot '" + lt.getId() + "' is running at chamber '" + chamberId + "@" + chamberEqpId + "@" + eqId + "' with batch '" + batchId + "'")

                if(batchId != lotBatchId)
                {
                    logger.info("Lot '" + lt.getId() + "' is not the same batch '" + lotBatchId  + "' as '" + batchId + "'")
                    continue
                }
                lot = lt

                def wipData = lot.getWipDataByEquipment(eqId)
                def moveOutWipData = wipData.getMoveOutWipDataItems()
                def mappingName = lot.getEquipmentId() + "-WipData"
				if (wipDataMapping == null)
				{
					wipDataMapping = w02ModBusService.readMappingValue(mappingName).getContainerMap()
				}
                if (wipDataMapping.size()>0)
                {
                    def value=""
                    def isValueSet = false;
                    WipDataDomainObject lastWipData = null;
                    def keys = wipDataMapping.keySet()
                    for (wipDataItem in moveOutWipData)
                    {
                        def pattern = ""
                        def wipDataStoreKey = ""
                        pattern = TscConfig.getStringProperty("WipDataMultipleTemperatureRegExp","T_.*[0-9]")
                        wipDataStoreKey = lot.getEquipmentId() + "_LastTemperature"

                        logger.info("Check WipData ["+ wipDataItem.getId()+"] Whether Match Pattern ["+ pattern +"]")
                        if(wipDataItem.getId().matches(pattern))
                        {
                            wipDataItem.setIsHidden(true)
                            lastWipData=wipDataItem
                            value = wipDataMapping.getOrDefault(wipDataItem.getId(), "")
                            if (value.length()==0)
                            {
                                logger.error("Missing configuration for '" + wipDataItem.getId() + "' or missing value!")
                            }
                            if(wipDataItem.getUomNotes().length()>0)
                            {
                                try
                                {
                                    double multiplyValue = Double.parseDouble(wipDataItem.getUomNotes())
                                    double conversionValue = Double.parseDouble(value)
                                    conversionValue = BigDecimal.valueOf(conversionValue).multiply(BigDecimal.valueOf(multiplyValue)).doubleValue()
                                    logger.info("Conversion applied, value="+ value + ",multiplyValue=" + multiplyValue + ",conversionValue=" + conversionValue)
                                    value = conversionValue + ""
                                }
                                catch (NumberFormatException ex)
                                {
                                    ex.printStackTrace()
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace()
                                }
                            }

                            try
                            {
                                if(wipDataStoreKey.length()>0)
                                {
                                    equipment.getPropertyContainer().setString(wipDataStoreKey, value + "")
                                }
                                logger.info("[" + equipment.getSystemId() + "] Record last value '" + value + "' to '" + wipDataStoreKey + "'")
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace()
                            }

                            if(wipDataItem.getValue()==null || wipDataItem.getValue().length()==0)
                            {
                                logger.info("Found Empty Match Pattern WipData "+ wipDataItem.getId() + " Modbus Value " + value)
                                wipDataItem.setValue(value)
                                isValueSet =true

                                def eventTime = CDateFormat.getFormatedDate(new Date())
                                def request = new SetResourceCommentRequest(chamberEqpId, "LotId: "+lot.getId()+" with "+lastWipData.getId()+": $value @ $eventTime.")
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
									}
									catch (Exception e)
									{
										e.printStackTrace()
									}
                                }

                                break;
                            }
                        }
                    }

                    if(lastWipData!=null && isValueSet==false)
                    {
                        logger.info("Empty Matched Pattern Not Found, Fill To Last Matched Pattern WipData "+ lastWipData.getId() + " with Value " + value)
                        lastWipData.setValue(value)

                        def eventTime = CDateFormat.getFormatedDate(new Date())
                        def request = new SetResourceCommentRequest(chamberEqpId, "LotId: "+lot.getId()+" with "+lastWipData.getId()+": $value @ $eventTime.")
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
							}
							catch (Exception e)
							{
								e.printStackTrace()
							}
                        }
                    }
                }
            }
            else
            {
                logger.info("Lot '" + lt.getId() + "' is running at '" + eqId + "' with child equipment '" + childEquipments + "'")
            }
        }
        if (lot ==null)
        {
            logger.error("Unable to find lot for chamber '" + chamberId + "@" + chamberEqpId + "'")
        }
    }
}