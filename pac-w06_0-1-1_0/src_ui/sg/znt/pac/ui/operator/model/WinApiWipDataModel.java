package sg.znt.pac.ui.operator.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.znt.pac.deo.DeoManager;
import de.znt.pac.deo.annotations.DeoParameter;
import de.znt.pac.deo.annotations.DeoTrigger;
import de.znt.pac.deo.error.DeoExecutionException;
import de.znt.pac.deo.trigger.DeoEventDispatcher;
import sg.znt.pac.AppProcessAutomationController;
import sg.znt.pac.TscConfig;
import sg.znt.pac.machine.CEquipment;
import sg.znt.pac.material.CLot;
import sg.znt.pac.material.CMaterialManager;
import sg.znt.pac.material.LotFilterAll;

public class WinApiWipDataModel
{

    public static final String DEO_TRIGGER_PROVIDER = "GUI";
    public static final String DEO_TRIGGER_CATEGORY = "WipData";
    public static final String DEO_TRIGGER_ID_READ_ALL = "ReadAll";
    public static final String DEO_TRIGGER_ID_CLEAR = "Clear";
    public static final String DEO_TRIGGER_ID_SUBMIT = "Submit";
    public static final String DEO_TRIGGER_ID_INITIALIZE = "Initialize";
    private static final String DEO_TRIGGER_PARAMETER_LOT_ID = "LotId";
    private static final String DEO_TRIGGER_PARAMETER_EQP_ID = "EqpId";
    private static final String DEO_TRIGGER_ID_UPDATE_LOT_STATUS = "UpdateLotStatus";
    private static final String DEO_TRIGGER_PARAMETER_WIP_FLAG = "WIPFlag";
    private static final String DEO_TRIGGER_PARAMETER_SERVICE_NAME = "ServiceName";
    
    private DeoManager _deoManager;
    private DeoEventDispatcher _deoEventDispatcher;
    private boolean _loginRequired = true;
    
    public WinApiWipDataModel(DeoManager deoManager)
    {
        _deoManager = deoManager;
        _deoEventDispatcher = deoManager.getDeoEventDispatcher();
    }

    @DeoTrigger(provider = DEO_TRIGGER_PROVIDER, category = DEO_TRIGGER_CATEGORY, id = DEO_TRIGGER_ID_READ_ALL, 
        input = {@DeoParameter(id = DEO_TRIGGER_PARAMETER_SERVICE_NAME, type = String.class)})
    public void readAllWipData(String serviceName) throws DeoExecutionException
    {
        Map<String, Object> runtimeParameters = new HashMap<String, Object>();
        runtimeParameters.put(DEO_TRIGGER_PARAMETER_SERVICE_NAME, serviceName);
        
        _deoEventDispatcher.notifyEvent(DEO_TRIGGER_PROVIDER, DEO_TRIGGER_CATEGORY, DEO_TRIGGER_ID_READ_ALL, runtimeParameters);
    }

    @DeoTrigger(provider = DEO_TRIGGER_PROVIDER, category = DEO_TRIGGER_CATEGORY, id = DEO_TRIGGER_ID_CLEAR, input = {})
    public void clearAllWipData() throws DeoExecutionException
    {
        Map<String, Object> runtimeParameters = new HashMap<String, Object>();
        
        _deoEventDispatcher.notifyEvent(DEO_TRIGGER_PROVIDER, DEO_TRIGGER_CATEGORY, DEO_TRIGGER_ID_CLEAR, runtimeParameters);
    }

    @DeoTrigger(provider = DEO_TRIGGER_PROVIDER, category = DEO_TRIGGER_CATEGORY, id = DEO_TRIGGER_ID_SUBMIT, 
        input = {@DeoParameter(id = DEO_TRIGGER_PARAMETER_LOT_ID, type = String.class),
                 @DeoParameter(id = DEO_TRIGGER_PARAMETER_EQP_ID, type = String.class)})
    public void submitWipData(String lotId, String eqpId) throws DeoExecutionException
    {
        Map<String, Object> runtimeParameters = new HashMap<String, Object>();
        runtimeParameters.put(DEO_TRIGGER_PARAMETER_LOT_ID, lotId);
        runtimeParameters.put(DEO_TRIGGER_PARAMETER_EQP_ID, eqpId);
        
        _deoEventDispatcher.notifyEvent(DEO_TRIGGER_PROVIDER, DEO_TRIGGER_CATEGORY, DEO_TRIGGER_ID_SUBMIT, runtimeParameters);
    }

    @DeoTrigger(provider = DEO_TRIGGER_PROVIDER, category = DEO_TRIGGER_CATEGORY, id = DEO_TRIGGER_ID_INITIALIZE, input = {})
    public void initializeData() throws DeoExecutionException
    {
        Map<String, Object> runtimeParameters = new HashMap<String, Object>();
        
        _deoEventDispatcher.notifyEvent(DEO_TRIGGER_PROVIDER, DEO_TRIGGER_CATEGORY, DEO_TRIGGER_ID_INITIALIZE, runtimeParameters);
    }
    
    @DeoTrigger(provider = DEO_TRIGGER_PROVIDER, category = DEO_TRIGGER_CATEGORY, id = DEO_TRIGGER_ID_UPDATE_LOT_STATUS, 
        input = {@DeoParameter(id = DEO_TRIGGER_PARAMETER_LOT_ID, type = String.class)},
        result = @DeoParameter(id = DEO_TRIGGER_PARAMETER_WIP_FLAG, type = String.class))
    public String getLotStatus(String lotId) throws DeoExecutionException
    {
        Map<String, Object> runtimeParameters = new HashMap<String, Object>();
        runtimeParameters.put(DEO_TRIGGER_PARAMETER_LOT_ID, lotId);
        
        String wipFlagSelection = (String) _deoEventDispatcher.notifyEvent(DEO_TRIGGER_PROVIDER, DEO_TRIGGER_CATEGORY, DEO_TRIGGER_ID_UPDATE_LOT_STATUS, runtimeParameters);
        
        if (wipFlagSelection != null)
        {
            switch (wipFlagSelection)
            {
                case "1": return "TrackInLot";
                case "3": return "TrackOutLot";
                case "4": return "LotMoveOut";
                default: return "";
            }
        }
        
        return "";
    }
    
    public CEquipment getEquipment(String equipmentId)
    {
        CEquipment equipment = AppProcessAutomationController.getAppProcessAutomationController().getCEquipment(equipmentId);
        
        return equipment;
    }

    public CLot getCLot()
    {
        CMaterialManager cMaterialManager = _deoManager.getGlobalBindingParameter(CMaterialManager.class);
        List<CLot> lotList = cMaterialManager.getCLotList(new LotFilterAll());
        if (lotList.size() > 0)
        {
            return lotList.get(0);
        }
        
        return null;
    }

    public boolean loginRequired()
    {
        return _loginRequired;
    }

    public boolean login(String userId, String password)
    {
        String id = TscConfig.getStringProperty("WinApiWipDataView.Authorization.LoginID", "op");
        String pwd = TscConfig.getStringProperty("WinApiWipDataView.Authorization.Password", "12345abcdE");
        if (id.equals(userId) && password.equals(pwd))
        {
            _loginRequired = false;
            
            return true;
        }
        else
        {
            _loginRequired = true;
            
            return false;
        }
    }

    public void logout()
    {
        _loginRequired = true;
    }

}
