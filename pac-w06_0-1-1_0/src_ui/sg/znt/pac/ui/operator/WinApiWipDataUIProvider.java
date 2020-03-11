package sg.znt.pac.ui.operator;

import org.springframework.beans.factory.annotation.Autowired;

import de.znt.pac.deo.DeoManager;
import de.znt.pac.domainobject.DomainObjectManager;
import de.znt.pac.resources.i18n.PacAllI18n;
import de.znt.uiVaadin.UserInterfaceProvider;
import de.znt.uiVaadin.VaadinUserInterface;
import sg.znt.pac.ui.operator.presenter.WinApiWipDataPresenter;

public class WinApiWipDataUIProvider
    implements UserInterfaceProvider
{

    @Autowired(required = true)
    private DeoManager _deoManager;
    
    @Autowired(required = true)
    private DomainObjectManager _domainObjectManager;
    
    private String _equipmentId;
    
    public WinApiWipDataUIProvider(String equipmentId)
    {
        _equipmentId = equipmentId;
    }
    
    @Override
    public String getTitle()
    {
        return _equipmentId + "\\" + PacAllI18n.localize(this.getClass(), "WinApiWipDataUI.Title");
    }
    
    @Override
    public VaadinUserInterface createUserInterface()
    {
        return new WinApiWipDataPresenter(_domainObjectManager, _deoManager, _equipmentId);
    }

}
