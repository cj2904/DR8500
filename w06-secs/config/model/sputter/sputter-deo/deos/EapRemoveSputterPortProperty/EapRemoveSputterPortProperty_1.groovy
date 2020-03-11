package EapRemoveSputterPortProperty

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import OutboundRequest.CommonOutboundRequest
import de.znt.pac.deo.annotations.*
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.domainobject.WipDataDomainObjectManager
import sg.znt.pac.machine.CEquipment

@CompileStatic
@Deo(description='''
eap remove port property container
''')
class EapRemoveSputterPortProperty_1
{

    @DeoBinding(id="WipDataDomainObjectManager")
    private WipDataDomainObjectManager wipDataManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

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
        def outbound = new CommonOutboundRequest(inputXml)
        def eqpId = outbound.getResourceName()
        def lotId = outbound.getContainerName()
        def lotList = outbound.getLotList()

        def wipDataDO = wipDataManager.getDomainObject(eqpId + "-" + lotId)
        def portNum = wipDataDO.getElement("Port").getValue()
        logger.info("Wip Data Port: '$portNum'")

        def portList = cEquipment.getPortList()
        for(port in portList)
        {
            if(lotList.size() == 0 || lotList == null)
            {
                logger.info("Lot list is empty or null!!!")
                port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_ID)
                port.getPropertyContainer().removeProperty("LotLoaded")
            }
            else if (lotList.size() > 0)
            {
                logger.info("Lot list: '$lotList'")
                for(lot in lotList)
                {
                    if(port.getPropertyContainer().getString(W06Constants.MES_LOT_ID, "").equalsIgnoreCase(lot))
                    {
                        port.getPropertyContainer().removeProperty(W06Constants.MES_LOT_ID)
                        port.getPropertyContainer().removeProperty("LotLoaded")
                    }
                }
            }
        }
    }
}