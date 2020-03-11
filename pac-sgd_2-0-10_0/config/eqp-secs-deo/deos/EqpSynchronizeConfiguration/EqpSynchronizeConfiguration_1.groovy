package EqpSynchronizeConfiguration

import groovy.transform.CompileStatic
import sg.znt.pac.machine.CEquipment
import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl

@CompileStatic
@Deo(description='''Synchronize pac configuration with equipment. Send configuration to tool.''')
class EqpSynchronizeConfiguration_1
{

    private SecsControl secsControl

    @DeoBinding(id="A01Equipment")
    private CEquipment cEquipment

    /**
     * Downloads the secs configuration to the tool
     * @param atOnce do the synchronization with single SF
     * @param deleteAllReports delete all tool reports
     */
    @DeoExecute(input=["AtOnce", "DeleteAllReports"])
    public void downloadConfiguration(Boolean atOnce, Boolean deleteAllReports)
    {
        if(cEquipment.isSecs())
        {
            try
            {
                uploadEquipmentState()
            }
            catch (Exception e)
            {
                de.znt.util.error.ErrorManager.handleError(e, this)
            }
            if (cEquipment.getControlState().isRemote())
            {
                secsControl = cEquipment.getSecsControl()
                secsControl.downloadSecsConfiguration(atOnce, deleteAllReports)
            }
            else
            {
                secsControl = cEquipment.getSecsControl()
                secsControl.downloadSecsConfiguration(false, true)
            }
        }
    }

    private void uploadEquipmentState()
    {
        cEquipment.updateControlState()
        cEquipment.updateProcessState()
    }
}