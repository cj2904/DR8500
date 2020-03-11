package sg.znt.pac.domainobject;

import de.znt.pac.domainobject.DomainObjectImpl;
import de.znt.pac.domainobject.DomainObjectType;
import de.znt.pac.domainobject.DomainObjects;

public class WinApiWipData
    extends DomainObjectImpl
{

    private static final long serialVersionUID = 1L;

    public static final DomainObjectType TYPE = new DomainObjectType("WinApiWipData");
    
    private static final String CONST_WIP_DATA_ITEM_NAME = "WIPDataItemName";
    private static final String CONST_WIP_DATA_VALUE = "WIPDataValue";
    private static final String CONST_WIP_DATA_SERVICE_NAME = "ServiceName";
    
    /**
     * 
     */
    public WinApiWipData()
    {
    }
    
    /**
     * @param id
     */
    public WinApiWipData(String id)
    {
        super(id, TYPE);
    }
    
    /**
     * @return
     */
    public String getWipDataItemName()
    {
        return getPropertyContainer().getString(CONST_WIP_DATA_ITEM_NAME, null);
    }
    
    /**
     * @param wipDataItemName
     */
    public void setWipDataItemName(String wipDataItemName)
    {
        getPropertyContainer().setString(CONST_WIP_DATA_ITEM_NAME, wipDataItemName);
    }
    
    /**
     * @return
     */
    public String getWipDataValue()
    {
        return getPropertyContainer().getString(CONST_WIP_DATA_VALUE, null);
    }
    
    /**
     * @param wipDataValue
     */
    public void setWipDataValue(String wipDataValue)
    {
        DomainObjects.access(() ->
        {
            getPropertyContainer().setString(CONST_WIP_DATA_VALUE, wipDataValue);
        });
    }
    
    /**
     * @return
     */
    public String getWipDataServiceName()
    {
        return getPropertyContainer().getString(CONST_WIP_DATA_SERVICE_NAME, null);
    }
    
    /**
     * @param serviceName
     */
    public void setWipDataServiceName(String serviceName)
    {
        getPropertyContainer().setString(CONST_WIP_DATA_SERVICE_NAME, serviceName);
    }
}
