package EqpStartMachine_LSM

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S2F41HostCommandSend
import de.znt.services.secs.dto.S2F42HostCommandAcknowledge
import de.znt.zsecs.composite.SecsAsciiItem
import groovy.transform.CompileStatic
import sg.znt.pac.W06Constants
import sg.znt.pac.material.CLot
import sg.znt.pac.material.CMaterialManager
import sg.znt.pac.material.LotFilterAll

@CompileStatic
@Deo(description='''
eap send start equipment command to eqp
''')
class EqpStartMachine_LSM_1
{

	@DeoBinding(id="SecsGemService")
	private SecsGemService secsGemService

	@DeoBinding(id="CMaterialManager")
	private CMaterialManager cMaterialManager

	@DeoBinding(id="Logger")
	private Log logger = LogFactory.getLog(getClass())


	/**
	 *
	 */
	@DeoExecute
	public void execute()
	{
		List<CLot> cLotList = cMaterialManager.getCLotList(new LotFilterAll())
		if (cLotList.size() > 0)
		{
			CLot cLot = cLotList.get(0)
			int i=0
			def waitToStartInSec = PacConfig.getIntProperty("WaitToStartInSec", 10)
			while (i<waitToStartInSec)
			{
				Thread.sleep (1000)
				def readyForStart = cLot.getPropertyContainer().getBoolean(W06Constants.PAC_LOT_PROPERTY_CONTAINER_RECIPE_VALIDATION_COMPLETED, false)
				if (!readyForStart)
				{
					i++
					continue
				}
				else
				{
					def request =  new S2F41HostCommandSend(new SecsAsciiItem("Process_Start"))
					request.addParameter(new SecsAsciiItem("Status") , new SecsAsciiItem(""))

					S2F42HostCommandAcknowledge reply = secsGemService.sendS2F41HostCommandSend(request)
					logger.info "PPSelect command : " + reply.getHCAckMessage()
					if (!reply.isCommandAccepted())
					{
						throw new Exception("Fail to start equipment with error: " + reply.getHCAckMessage())
					}
					break
				}
			}
		}
	}
}