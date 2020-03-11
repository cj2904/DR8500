package EapPreCheckExistLot_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.PacUtils
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
eap check if there is still lot not track out from the eqp before new batch lot track in
''')
class EapPreCheckExistLot_Common_1
{

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="InputXml")
    private String inputXml

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02TrackInLotRequest(inputXml)
        def lotCount = PacUtils.valueOfInteger(outbound.getEquipmentLotCount(), -1)
        def eqpId = outbound.getResourceName()
        def lotId = outbound.getContainerName()

        if(lotCount > 0)
        {
            def lotList = cMaterialManager.getCLotList(new LotFilterAll())
            if(!lotList.empty)
            {
                def batchList = cEquipment.getPropertyContainer().getStringArray(eqpId + "_BatchTrackInLots", "")
                def found = false
                for(batch in batchList)
                {
                    if(lotId.equalsIgnoreCase(batch))
                    {
                        found = true
                        break
                    }
                }

                if(!found)
                {
                    def strArr = new ArrayList<String>()
                    for(lot in lotList)
                    {
                        def lotEqp = lot.getEquipmentId()
                        if(lotEqp.equalsIgnoreCase(eqpId))
                        {
                            def portList = cEquipment.getPortList()
                            if(portList.empty || portList == null)
                            {
                                logger.info("There is no port list found for eqp: '$lotEqp'")
                                strArr.add(lot.getId())
                            }
                            else
                            {
                                logger.info("These are the port list: '$portList' found for the eqp: '$lotEqp'")
                                for(port in portList)
                                {
                                    if(port.getPortId().equalsIgnoreCase(eqpId))
                                    {
                                        if(!port.getPropertyContainer().getString(W06Constants.MES_LOT_ID, "").empty || !port.getPropertyContainer().getString(lot.getId(), "").empty)
                                        {
                                            strArr.add(lot.getId())
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(strArr.size() > 0)
                    {
                        throw new Exception("There is lot not yet Track Out, lotIds: '$strArr'!! Please perform Track Out for the lot before proceed.")
                    }
                }
            }
        }
    }
}