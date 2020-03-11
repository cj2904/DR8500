package SputterDprStartEquipment

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll
import sg.znt.pac.util.EqpUtil

@CompileStatic
@Deo(description='''
Sputter specific function:<br/>
<b>Dispatch to start equipment model scenario trigger</b>
''')
class SputterDprStartEquipment_1 {


    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment
    
    @DeoBinding(id="CMaterialManager")
    private CMaterialManager cMaterialManager
	
	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService
    /**
     *
     */
    @DeoExecute
    public void execute()
    {
		def lotList = cMaterialManager.getCLotList(new LotFilterAll())
		if (lotList.size() == 0)
		{
			def message = "No lot found in pac Material Manager!"
			secsGemService.sendTerminalMessage((byte) 0, message)
			throw new Exception(message)
		}
		
		def eqpModel = PacConfig.getStringProperty("Equipment1.Name", "")
		
		def svidPortsState = PacConfig.getStringArrayProperty("Secs.PortState." + eqpModel + ".StatusVariable.VID", "", ",")
		def allowableStartPortState = PacConfig.getStringArrayProperty("Secs.PortState." + eqpModel + ".States.Ready2Start", "", ",")
		def maxAttempt = PacConfig.getIntProperty("TimeWaitForPortsReadyInSec", 120)
		int i=0
		def portsReady = false
		for (svid in svidPortsState)
		{
			while (i<maxAttempt)
			{
				def portStateReady = false
				SecsComponent< ? > svidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svid)))
				def s1f3 = new S1F3SelectedEquipmentStatusRequest(svidItem)
				def replyS1F3 = secsGemService.sendS1F3SelectedEquipmentStatusRequest(s1f3)
				def value = EqpUtil.getVariableData(replyS1F3.getData().getSV(0))
				for (portState in allowableStartPortState)
				{
					if (portState.trim().equalsIgnoreCase(value))
					{
						portStateReady = true
					}
				}
				if (!portStateReady)
				{
					logger.trace("Port State with SVID '$svid' is not ready, current port state='$value'")
					Thread.sleep(1000)
					i++
					portsReady = false
					continue
				}
				else
				{
					portsReady = true
					break
				}
			}
		}
        
		if (portsReady)
		{
			logger.info("All ports are ready for processing...")
			cEquipment.getModelScenario().eqpStartEquipment(new HashMap<String, Object>())			
		}
		else
		{
			def message = "Not all ports are ready for processing, skip send Host Command 'START' to equipment!"
			logger.warn(message)
			secsGemService.sendTerminalMessage((byte)0, message)
		}
    }
}