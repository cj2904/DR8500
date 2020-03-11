package MesTrackInOutboundNotifySucceed

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import de.znt.pac.mapping.MappingManager
import groovy.transform.CompileStatic
import sg.znt.pac.EquipmentIdentifyService
import sg.znt.pac.TscConfig
import sg.znt.pac.TscConstants
import sg.znt.pac.domainobject.WipDataDomainObject
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.TscEquipment
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.util.TscWinApiEqpUtil
import sg.znt.services.camstar.CCamstarService
import sg.znt.services.camstar.outbound.W02TrackInLotRequest
import sg.znt.services.camstar.outbound.W02TrackInLotRequest.TrackInWafer


@CompileStatic
@Deo(description='''
Notify track in succeed
''')
class MesTrackInOutboundNotifySucceed_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    @DeoBinding(id="InputXmlDocument")
    private String inputXmlDocument

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager materialManager

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataDomainObjectManager
    
    @DeoBinding(id="EquipmentIdentifyService")
    private EquipmentIdentifyService equipmentIdentifyService
    
    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService
    
    @DeoBinding(id="MappingManager")
    private MappingManager mappingManager
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outboundRequest = new W02TrackInLotRequest(inputXmlDocument)
        def waferScribeNumber = ""
        String waferNumber = ""
        List<WipDataDomainObject> wipDataItems = null
        def lotId = outboundRequest.getContainerName()
        def lot = materialManager.getCLot(lotId)
        TscEquipment equipment = (TscEquipment) equipmentIdentifyService.getEquipmentBySystemId(outboundRequest.getResourceName())

        if(!lot.getPropertyContainer().getBoolean(TscConstants.MATERIAL_ATTR_FIRST_LOT_IN_BATCH, false))
        {
            logger.info("Skip eqpMesTrackInSucceed for lot '" + lotId + "' since it's not first batch!")
        }
        else
        {
            def eqpid = (lot.getEquipmentId().equalsIgnoreCase(equipment.getVirtualSystemId())?equipment.getVirtualSystemId():equipment.getRealSystemId())            
            if (TscConfig.useBatchWaferDataCollection())
            {
                handleMultipleWaferDataCollection(lot, outboundRequest.getLotTrackInWaferList(), equipment)
            }
            else
            {
                if (outboundRequest.getLotTrackInWaferList().size()==1)
                {
                    def trackInWafer = outboundRequest.getLotTrackInWaferList().get(0)
                    waferScribeNumber = trackInWafer.getWaferScribeNumber()
                    waferNumber = trackInWafer.getWaferNumber()
                    def wafer = lot.getWafer(waferNumber)
                    def waferWipdata = wafer.getWipData()
                    if (waferWipdata != null)
                    {
                        wipDataItems = waferWipdata.getTrackOutWipDataItems()
                    }
                    else
                    {
                        def wds = lot.getWipDataByEquipment(eqpid)
                        if (wds == null)
                        {
                            logger.error("Wip data not found for '" + eqpid + "'")
                        }
                        else
                        {
                            wipDataItems = wds.getTrackOutWipDataItems()
                        }
                    }
                }
                else
                {
                    def wds = lot.getWipDataByEquipment(eqpid)
                    if (wds == null)
                    {
                        logger.error("Wip data not found for '" + eqpid + "'")
                    }
                    else
                    {
                        wipDataItems = wds.getTrackOutWipDataItems()
                    }
                }
                
                def msg = ""
                
                def mapTrackIn = TscWinApiEqpUtil.createMesTrackInSucceedRequest(lotId, wipDataItems, WipDataDomainObject.SERVICE_TYPE_TRACK_OUT_WIP_DATA,
                    eqpid, msg,
                    waferScribeNumber, waferNumber)
                equipment.getModelScenario().eqpMesTrackInSucceed(mapTrackIn, cCamstarService, inputXmlDocument)
            }
        }
    }
    
    void handleMultipleWaferDataCollection(CLot lot, List<TrackInWafer> trackInWaferList, TscEquipment equipment)
    {
        def eqpid = (lot.getEquipmentId().equalsIgnoreCase(equipment.getVirtualSystemId())?equipment.getVirtualSystemId():equipment.getRealSystemId())
        
        String wipDataList = "";
        
        for (trackInWafer in trackInWaferList) 
        {
            try 
            {
                def wafer = lot.getWafer(trackInWafer.getWaferNumber())
                def waferWipdata = wafer.getWipData()
                if (waferWipdata != null)
                {
                    def wipDataItems = waferWipdata.getTrackOutWipDataItems()
                    if (wipDataItems != null && wipDataItems.size()>0)
                    {
                        TscWinApiEqpUtil.removeNonAutoItem(wipDataItems);
                        for (WipDataDomainObject wipDataDomainObject : wipDataItems)
                        {
                            if (wipDataDomainObject.isHidden())
                            {
                                if(wipDataList.length()>0)
                                {
                                    wipDataList = wipDataList + ";";
                                }
                                wipDataList = wipDataList + wafer.getWaferScribeID() + "@" + wipDataDomainObject.getId() + "=";
                            }
                        }
                    }
                }               
            } 
            catch (Exception e) 
            {
                e.printStackTrace()
            }
        }
        if (wipDataList.length()==0)
        {            
            def wds = lot.getWipDataByEquipment(eqpid)
            if (wds == null)
            {
                logger.error("Wip data not found for '" + eqpid + "'")
            }
            else
            {
                def wipDataItems = wds.getTrackOutWipDataItems()
                if (wipDataItems != null && wipDataItems.size()>0)
                {
                    TscWinApiEqpUtil.removeNonAutoItem(wipDataItems);
                    for (WipDataDomainObject wipDataDomainObject : wipDataItems)
                    {
                        if (wipDataDomainObject.isHidden())
                        {
                            if(wipDataList.length()>0)
                            {
                                wipDataList = wipDataList + ";";
                            }
                            wipDataList = wipDataList + wipDataDomainObject.getId() + "=";
                        }
                    }
                }
            }        
        }
        
        def dataToCollect = ""
        if (lot.getPropertyContainer().getBoolean("IsIQCFlow", false))
        {
            def schemaItems = mappingManager.getSchemaComponentByName("Measurement", "IQC").getSchemaItems()
            if (schemaItems != null)
            {
                dataToCollect = schemaItems.get(0).getName()
            }
            else
            {
                logger.error("Schema items is empty for Schema 'Measurement' Schema Components 'IQC'!")
            }
        }
        else if (lot.getPropertyContainer().getBoolean("IsFirstPieceFlow", false))
        {
            def schemaItems = mappingManager.getSchemaComponentByName("Measurement", "FirstPiece").getSchemaItems()
            if (schemaItems != null)
            {
                dataToCollect = schemaItems.get(0).getName()
            }
            else
            {
                logger.error("Schema items is empty for Schema 'Measurement' Schema Components 'FirstPiece'!")
            }
        }
        else
        {
            /*
             * IPQC
             */
            def schemaItems = mappingManager.getSchemaComponentByName("Measurement", "IPQC").getSchemaItems()
            if (schemaItems != null)
            {
                dataToCollect = schemaItems.get(0).getName()
            }
            else
            {
                logger.error("Schema items is empty for Schema 'Measurement' Schema Components 'IPQC'!")
            }
        }
        
        HashMap<String, Object> mapTrackIn = new HashMap<String, Object>();
        mapTrackIn.put("LotId", lot.getId());
        mapTrackIn.put("WipDataList", wipDataList);
        mapTrackIn.put("ServiceType", WipDataDomainObject.SERVICE_TYPE_TRACK_OUT_WIP_DATA);
        mapTrackIn.put("EquipmentId", eqpid);
        mapTrackIn.put("Message", "");
        mapTrackIn.put("WaferId", "");
        mapTrackIn.put("WaferNo", "");
        mapTrackIn.put("DataToCollect", dataToCollect)
        
        equipment.getModelScenario().eqpMesTrackInSucceed(mapTrackIn, cCamstarService, inputXmlDocument)
    }
}