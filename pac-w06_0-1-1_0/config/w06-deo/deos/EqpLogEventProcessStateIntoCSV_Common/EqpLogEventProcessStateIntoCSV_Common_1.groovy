package EqpLogEventProcessStateIntoCSV_Common

import java.text.SimpleDateFormat

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import de.znt.pac.PacConfig
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import de.znt.pac.deo.triggerprovider.secs.SecsEvent
import de.znt.services.secs.SecsGemService
import de.znt.services.secs.dto.S1F3SelectedEquipmentStatusRequest
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsDataItem
import de.znt.zsecs.composite.SecsDataItem.ItemName
import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import sg.znt.pac.util.EqpUtil

@CompileStatic
@Deo(description='''
generate a csv file to save specific event trigger by eqp and it process status
''')
class EqpLogEventProcessStateIntoCSV_Common_1
{

    @DeoBinding(id="SecsEvent")
    private SecsEvent secsEvent

    @DeoBinding(id="SecsControl")
    private SecsControl secsControl

    @DeoBinding(id="SecsGemService")
    private SecsGemService secsGemService

    @DeoBinding(id="CEquipment")
    private CEquipment cEquipment

    @DeoBinding(id="Logger")
    private Log logger = LogFactory.getLog(getClass())


    /**
     *
     */
    @DeoExecute
    public void execute()
    {
        //Get File Required Info
        def eventId = 0, eventName = "Not Defined", processId = 0, processName = "Not Defined"
        eventId = secsEvent.getCeid()
        eventName = secsEvent.getName()

        def portList = cEquipment.getPortList()
        if(!portList.empty)
        {
            for(port in portList)
            {
                def portId = port.getPortId()
                if(secsEvent.getName().contains(portId))
                {
                    logger.info("Inside port: '$portId'")
                    def processState = PacConfig.getStringProperty("Secs.ProcessState." + portId + ".StatusVariable.Name", "")
                    logger.info("Vid: '$processState'")
                    processId = Integer.parseInt(getValueFromEqp(portId ,processState))
                    processName = PacConfig.getStringProperty("Secs.ProcessState." + portId + "." + processState, "")
                }
                break
            }
        }

        if(processName.length() == 0)
        {
            logger.info("Get Equipment SystemId process state since no port detected!!!")
            cEquipment.updateProcessState()
            processId = cEquipment.getProcessState().getState()
            processName = cEquipment.getProcessState().getStateName()
        }

        logger.info("Jimmy Log Info: '$eventId, $eventName, $processId, $processName'")
        //Generate File Section
        def formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        def timestamp = formatter.format(new Date(System.currentTimeMillis()))
        def year = Calendar.YEAR
        def month = Calendar.MONTH
        def day = Calendar.DAY_OF_MONTH
        def filePath = PacConfig.getStringProperty("Equipment.ProcessState.FilePath", "")
        logger.info("Jimmy file path: '$filePath'")
        def eqpName = cEquipment.getSystemId()
        def datePath = "$year\\$month\\$day\\"
        def fileName = eqpName + ".csv"
        def dest = filePath + datePath + fileName
        def file = new File(dest)

        def writer = new FileWriter(file, true)

        if(file.exists())
        {
            if(file.length() == 0)
            {
                logger.info("Inside there")
                //date//eventid//eventname//alarmid//alarmname//processstate
                //Header
                writer.println("Date,EventId,EventName,AlarmId,AlarmName,ProcessStateId,ProcessStateName")
                writer.println("$timestamp,$eventId,$eventName,,,$processId,$processName")
            }
            else
            {
                logger.info("Inside here")
                //                writer = new PrintWriter(new FileWriter(file, true))
                writer.print(System.lineSeparator())
                writer.print("$timestamp,$eventId,$eventName,,,$processId,$processName")
            }
        }

        writer.flush()
        writer.close()
        logger.info("CSV generate successful...")
    }

    String getValueFromEqp(String eqp, String vidName)
    {
        def svid = secsControl.translateSvVid(vidName)
        SecsComponent< ? > svidItem = SecsDataItem.createDataItem(ItemName.VID, new Long(Long.valueOf(svid)))
        def request = new S1F3SelectedEquipmentStatusRequest(svidItem)
        def reply = secsGemService.sendS1F3SelectedEquipmentStatusRequest(request)
        def eqpValue = EqpUtil.getVariableData(reply.getData().getSV(0))

        if(eqpValue.length() > 0)
        {
            logger.info("Equipment: '$eqp' process state is '$eqpValue'")
            return eqpValue
        }
        return ""
    }
}