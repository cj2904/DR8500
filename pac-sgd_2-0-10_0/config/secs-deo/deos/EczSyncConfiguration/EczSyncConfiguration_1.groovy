package EczSyncConfiguration

import de.znt.pac.deo.annotations.*
import de.znt.pac.deo.triggerprovider.secs.SecsControl
import sg.znt.pac.machine.CEquipment

@Deo(description='''Synchronize pac configuration with equipment. Send configuration to tool.''')
class EczSyncConfiguration_1
{

    private SecsControl secsControl

    @DeoBinding(id="Equipment")
    private CEquipment equipment

    /**
     * Downloads the secs configuration to the tool
     * @param atOnce do the synchronization with single SF
     * @param deleteAllReports delete all tool reports
     */
    @DeoExecute(input=["AtOnce", "DeleteAllReports"])
    public void downloadConfiguration(Boolean atOnce, Boolean deleteAllReports)
    {
        if(equipment.isSecs())
        {
            try
            {
                uploadEquipmentState()
            }
            catch (Exception e)
            {
                de.znt.util.error.ErrorManager.handleError(e, this)
            }
            if (equipment.getControlState().isRemote())
            {
                secsControl = equipment.getSecsControl()
                secsControl.downloadSecsConfiguration(atOnce, deleteAllReports)
            }
            else
            {
                secsControl = equipment.getSecsControl()
                secsControl.downloadSecsConfiguration(false, false)
            }
        }
    }

    private void uploadEquipmentState()
    {
        equipment.updateControlState()
        equipment.updateProcessState()
    }
}