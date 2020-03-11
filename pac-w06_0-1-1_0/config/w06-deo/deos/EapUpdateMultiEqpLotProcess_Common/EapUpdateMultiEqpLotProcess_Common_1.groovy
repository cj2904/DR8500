package EapUpdateMultiEqpLotProcess_Common

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll

@CompileStatic
@Deo(description='''
eap update process lot for eqp with multiple port
''')
class EapUpdateMultiEqpLotProcess_Common_1
{

    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

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
        def reports = secsEvent.getAssignedReports()
        String portValue

        if(reports.size() > 0)
        {
            for(report in reports)
            {
                def key = PacConfig.getStringProperty("Secs.Event.PortId.VID", "")
                portValue = report.getPropertyContainer().getValueAsString(key, "")
            }
        }
        else
        {
            def eventId = secsEvent.getCeid().toString()
            portValue = PacConfig.getStringProperty("Secs.Event.PortId.CEID." + eventId, "")
        }

        if(portValue.length() > 0)
        {
            def portList = cEquipment.getPortList()
            for(port in portList)
            {
                if(port.getNumber().toString().equalsIgnoreCase(portValue))
                {
                    def portId = port.getPortId()
                    def lotList = cMaterialManager.getCLotList(new LotFilterAll())
                    for(lot in lotList)
                    {
                        if(lot.getEquipmentId().equalsIgnoreCase(portId))
                        {
                            lot.getPropertyContainer().setBoolean("LotProcessed", true)
                        }
                    }
                }
            }
        }
        else
        {
            logger.info("Equipment pre-defined properties key for <report / event name> is not found in SecsEvent: '" + secsEvent.getName() + "'")
        }
    }
}