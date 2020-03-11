package EapVerifyLotInWip_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.camstar.semisuite.service.dto.EquipmentWIPQueryRequest
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.CCamstarService

@CompileStatic
@Deo(description='''
Verify if existing lot is in the wip, if it's not remove them from material manager
''')
class EapVerifyLotInWip_Common_1
{


    @DeoBinding(id="MainEquipment")
    private CEquipment mainEquipment

    @DeoBinding(id="InputXmlDocument")
    private String inputXml

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="CCamstarService")
    private CCamstarService cCamstarService

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new CommonOutboundRequest(inputXml)
        def equipmentLotCount = PacUtils.valueOfInteger(outbound.getEquipmentLotCount(), -1)
        logger.info("eqp Lot Count: '$equipmentLotCount'")
        def lotId = outbound.getContainerName()
        def eqpId = outbound.getResourceName()

        def lotList = cMaterialManager.getCLotList(new LotFilterAll())
        if(lotList.size() > 0)
        {
            if (equipmentLotCount > 0)
            {
                List<String> replyLotList = new ArrayList<String>()
                def wipRequest = new EquipmentWIPQueryRequest(eqpId)
                def wipReply = cCamstarService.equipmentWIPQuery(wipRequest)

                if(wipReply.isSuccessful())
                {
                    replyLotList = wipReply.getLotIdList()
                    logger.info("Query Reply lot list: '$replyLotList'")
                }
                else
                {
                    throw new Exception(wipReply.getExceptionData().getErrorDescription())
                }

                for(lot in lotList)
                {
                    if(!eqpId.equalsIgnoreCase(lot.getEquipmentId()))
                    {
                        logger.info(lot.getId() + "is not belongs to track in equipment: '$eqpId'.")
                        continue
                    }

                    if(!lotId.equalsIgnoreCase(lot.getId()))
                    {
                        def found = false
                        if(!replyLotList.isEmpty())
                        {
                            for(replyLot in replyLotList)
                            {
                                if(replyLot.equalsIgnoreCase(lot.getId()))
                                {
                                    logger.info("Lot is found for '$replyLot' and " + lot.getId())
                                    found = true
                                    break
                                }
                            }
                            if(!found)
                            {
                                logger.info("Remove lot: '" + lot.getId() + "' from Material Manager since it's not in Camstar WIP Main!!")
                                cMaterialManager.removeCLot(lot)
                            }
                        }
                    }
                }
            }
            else
            {
                for(lot in lotList)
                {
                    if(lot.getEquipmentId().equalsIgnoreCase(eqpId))
                    {
                        logger.info("Remove lot:'" + lot.getId() + "' from Material Manager since it's not in Camstar WIP Main!!")
                        cMaterialManager.removeCLot(lot)
                    }
                }
            }
        }
    }
}