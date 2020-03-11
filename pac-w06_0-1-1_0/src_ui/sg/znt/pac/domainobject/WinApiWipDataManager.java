package sg.znt.pac.domainobject;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import de.znt.pac.DuplicateItemException;
import de.znt.pac.domainobject.DomainObjectManager;

public class WinApiWipDataManager
{
    @Autowired(required=true)
    private DomainObjectManager _domainObjectManager;
  
    @PostConstruct
    private void initialize()
    {
        _domainObjectManager.addType(WinApiWipData.TYPE);
    }
    
    public WinApiWipData createNewDomainObject(String id) throws DuplicateItemException
    {
        WinApiWipData winApiWipData = new WinApiWipData(id);
        _domainObjectManager.add(winApiWipData);
        
        return winApiWipData;
    }
    
    public List<WinApiWipData> getAll()
    {
        return _domainObjectManager.getAll(WinApiWipData.TYPE);
    }
    
    public List<WinApiWipData> getAllDomainObject()
    {
        return getAll();
    }
    
    public void removeAllDomainObject()
    {
        List<WinApiWipData> allItems = getAll();
        List<WinApiWipData> removeList = new ArrayList<WinApiWipData>();
        for (WinApiWipData itm : allItems)
        {
            removeList.add(itm);
        }
        if(removeList.size() > 0 )
        {
            for (WinApiWipData remove : removeList) 
            {
                _domainObjectManager.remove(remove);
            }
        }
    }
    
    public WinApiWipData getDomainObject(String id)
    {
        return _domainObjectManager.get(WinApiWipData.TYPE, id);
    }

}
