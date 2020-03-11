package EapValidateTrackInPortWIP_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment
import sg.znt.services.camstar.outbound.W02TrackInLotRequest

@CompileStatic
@Deo(description='''
eap check different lot if track in to same port
If yes, then throw error
''')
class EapValidateTrackInPortWIP_Common_1
{

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="ImputXml")
    private String inputXml

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())

    static final String LOT_LOADED = "LotLoaded"


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        def outbound = new W02TrackInLotRequest(inputXml)
        def eqpId = outbound.getResourceName()
        def lotId = outbound.getContainerName()
        def portList = cEquipment.getPortList()
        def wipDataDO = wipDataManager.getDomainObject(eqpId + "-" + lotId)
        def portNum = wipDataDO.getElement("Port").getValue()
        logger.info("Wip Data Port: '$portNum'")
        def found = false

        for(port in portList)
        {
            logger.info("concat eqp: '" + eqpId + "-" + portNum + "'")
            if(port.getPortId().equalsIgnoreCase(eqpId + "-" + portNum))
            {
                found = true
                def firstLot = port.getPropertyContainer().getBoolean(LOT_LOADED, false)

                if(firstLot)
                {
                    def lot = port.getPropertyContainer().getString(W06Constants.MES_LOT_ID, "")
                    throw new Exception("Lot: '$lot' already track in to the eqp: '$eqpId' port: '$portNum', Please check the track in eqp port for lot: '$lotId'!!!")
                }
                else
                {
                    // continue
                    port.getPropertyContainer().setBoolean(LOT_LOADED, true)
                    port.getPropertyContainer().setString(W06Constants.MES_LOT_ID, lotId)
                    logger.info("Successful set lot loaded key: 'true' and lot id: '$lotId'")
                }
            }
        }

        if(!found)
        {
            throw new Exception("Track In Lot: '$lotId' for Eqp: '$eqpId' with Port: '$portNum' is not defined correctly!!!!")
        }
    }
}