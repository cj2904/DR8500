package sg.znt.pac.ui.operator.presenter;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Binder;
import com.vaadin.data.Binder.Binding;
import com.vaadin.data.provider.DataChangeEvent;
import com.vaadin.data.provider.DataProviderListener;
import com.vaadin.data.ValueProvider;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

import de.znt.pac.deo.DeoManager;
import de.znt.pac.deo.error.DeoExecutionException;
import de.znt.pac.domainobject.DomainObjectManager;
import de.znt.pac.resources.i18n.PacAllI18n;
import de.znt.pac.ui.vaadin.binding.DomainObjectContainer;
import de.znt.pac.ui.vaadin.presenter.Presenter;
import de.znt.uiVaadin.VaadinUserInterface;
import de.znt.util.error.ErrorManager;
import sg.znt.pac.domainobject.WinApiWipData;
import sg.znt.pac.machine.CEquipment;
import sg.znt.pac.material.CLot;
import sg.znt.pac.ui.operator.model.WinApiWipDataModel;
import sg.znt.pac.ui.operator.view.CustomizableConfirmDialog;
import sg.znt.pac.ui.operator.view.CustomizableConfirmDialog.Callback;
import sg.znt.pac.ui.operator.view.WinApiWipDataView;
import sg.znt.pac.util.UserInterfaceUtil;

public class WinApiWipDataPresenter
    implements VaadinUserInterface, Presenter
{

    private static final String I18N_GRID_COLUMN_NUMBER = "WinApiWipDataPresenter.Grid.Column.Number";
    private static final String I18N_GRID_COLUMN_ITEM = "WinApiWipDataPresenter.Grid.Column.Item";
    private static final String I18N_GRID_COLUMN_VALUE = "WinApiWipDataPresenter.Grid.Column.Value";
    private static final String I18N_LABEL_GRID_COUNT = "WinApiWipDataPresenter.Label.ItemCount.Text";
    private static final String I18N_GRID_NO_LOT_TEXT = "WinApiWipDataPresenter.Grid.NoLot.Text";
    private static final String I18N_AUTHORIZATION_WINDOW_TITLE = "WinApiWipDataPresenter.Authorization.Window.Title";
    protected static final String I18N_AUTHORIZATION_MESSAGE = "WinApiWipDataPresenter.Authorization.Message";
    
    private final WinApiWipDataView _view;
    private WinApiWipDataModel _model;
    
    private DomainObjectManager _domainObjectManager;
    private DomainObjectContainer<WinApiWipData> _container;
    
    public WinApiWipDataPresenter(DomainObjectManager domainObjectManager, DeoManager deoManager, String equipmentId)
    {
        _domainObjectManager = domainObjectManager;
        
        _view = new WinApiWipDataView();
        _model = new WinApiWipDataModel(deoManager);
        
        try
        {
            _model.initializeData();
        }
        catch (Exception e)
        {
            ErrorManager.handleError(e, this);
        }
        
        CEquipment cEquipment = _model.getEquipment(equipmentId);
        _view.getEquipmentIdField().setValue(cEquipment.getSystemId());
        
        CLot cLot = _model.getCLot();
        if (cLot != null)
        {
            bindDataWhenLotAvailable(cLot);
        }
        else
        {
            bindDataWhenLotNotAvailable();
        }
        
        _view.getReadAllButton().addClickListener(event -> {
            try
            {
                _model.readAllWipData(cLot.getMesLotStatus());
            }
            catch (Exception e)
            {
                ErrorManager.handleError(e, this);
            }
        });
        
        _view.getClearButton().addClickListener(event -> {
            try
            {
                _model.clearAllWipData();
            }
            catch (Exception e)
            {
                ErrorManager.handleError(e, this);
            }
        });
        
        _view.getSubmitButton().addClickListener(event -> {
            try
            {
                _model.submitWipData(_view.getLotIdField().getValue(), _view.getEquipmentIdField().getValue());
            }
            catch (Exception e)
            {
                ErrorManager.handleError(e, this);
            }
        });
        
    }

    private void bindDataWhenLotNotAvailable()
    {
        _view.getWipDataGrid().setItems(new WinApiWipData("NoLot"));
        _view.getWipDataGrid().setEnabled(false);
        _view.getWipDataGrid().setHeaderVisible(false);
        _view.getWipDataGrid().setBodyRowHeight(300.0);
        _view.getWipDataGrid().addComponentColumn(event -> {
            
            Label label = new Label(PacAllI18n.localize(this.getClass(), I18N_GRID_NO_LOT_TEXT));
            label.addStyleName(ValoTheme.LABEL_FAILURE);
            label.addStyleName(ValoTheme.LABEL_HUGE);
            label.setSizeFull();
            label.setHeight(_view.getWipDataGrid().getBodyRowHeight() + "px");
            
            return label;
        });
        
        _view.getReadAllButton().setEnabled(false);
        _view.getClearButton().setEnabled(false);
        _view.getSubmitButton().setEnabled(false);
    }

    private void bindDataWhenLotAvailable(CLot cLot)
    {
        try
        {
            cLot.setMesLotStatus(_model.getLotStatus(cLot.getId()));
            _view.getLotStateField().setValue(cLot.getMesLotStatus());
        }
        catch (DeoExecutionException e)
        {
            ErrorManager.handleError(e, this);
            _view.getLotStateField().setValue("UNAVAILABLE");
        }
        String mesLotStatus = cLot.getMesLotStatus();
        
        _view.getLotIdField().setValue(cLot.getId());
        
        _container = new DomainObjectContainer<WinApiWipData>(_domainObjectManager, WinApiWipData.TYPE, _view);
        _container.setFilter(filter -> {
            if (filter.getWipDataServiceName().equals(mesLotStatus))
            {
                return true;
            }
            return false;
        });
        
        _view.getWipDataGrid().setDataProvider(_container);
        
        TextField wipDataValueField = new TextField();
        
        Binder<WinApiWipData> binder = _view.getWipDataGrid().getEditor().getBinder();
        Binding<WinApiWipData,String> binding = binder.bind(wipDataValueField, WinApiWipData::getWipDataValue, WinApiWipData::setWipDataValue);
        
        _view.getWipDataGrid()
            .addColumn(new ValueProvider<WinApiWipData, String>()
            {

                private static final long serialVersionUID = 1L;

                @Override
                public String apply(WinApiWipData source)
                {
                    return createItemCountColumn(mesLotStatus, source);
                }
            }).setWidth(60.0)
                .setSortable(false)
                    .setCaption(PacAllI18n.localize(this.getClass(), I18N_GRID_COLUMN_NUMBER));
        
        _view.getWipDataGrid()
            .addColumn(WinApiWipData::getWipDataItemName)
                .setSortable(false)
                    .setCaption(PacAllI18n.localize(this.getClass(), I18N_GRID_COLUMN_ITEM));
        
        _view.getWipDataGrid()
            .addColumn(WinApiWipData::getWipDataValue)
                .setSortable(false)
                    .setId("Value")
                        .setEditorBinding(binding)
                            .setCaption(PacAllI18n.localize(this.getClass(), I18N_GRID_COLUMN_VALUE));
        
        _view.getWipDataGrid().addItemClickListener(event -> {
            if (_model.loginRequired())
            {
                Component c = _view.getAuthorizationLayout();
                TextField id = (TextField) UserInterfaceUtil.findComponentWithId((HasComponents) c, "idField");
                PasswordField pwd = (PasswordField) UserInterfaceUtil.findComponentWithId((HasComponents) c, "passwordField");
                CustomizableConfirmDialog d = new CustomizableConfirmDialog(PacAllI18n.localize(this.getClass(), I18N_AUTHORIZATION_WINDOW_TITLE), c, new Callback()
                {
                    
                    @Override
                    public void onDialogResult(boolean resultIsYes, Window currentWindow)
                        throws Exception
                    {
                        if (resultIsYes)
                        {
                            boolean success = _model.login(id.getValue(), pwd.getValue());
                            if (!success)
                            {
                                Notification.show(PacAllI18n.localize(WinApiWipDataPresenter.class, I18N_AUTHORIZATION_MESSAGE), Type.ERROR_MESSAGE);
                            }
                            else
                            {
                                _view.getLogOutButton().setVisible(true);
                                _view.getLogOutButton().addClickListener(event -> {
                                    _model.logout();
                                    _view.getLogOutButton().setVisible(false);
                                });
                            }
                        }
                    }
                });
                ((AbstractOrderedLayout) d.getContent()).setSpacing(false);
                d.getConfirmButton().addStyleName(ValoTheme.BUTTON_FRIENDLY);
                d.getConfirmButton().addStyleName(ValoTheme.BUTTON_TINY);
                
                d.getCancelButton().addStyleName(ValoTheme.BUTTON_FRIENDLY);
                d.getCancelButton().addStyleName(ValoTheme.BUTTON_TINY);
                UI.getCurrent().addWindow(d);
            }
        });
        
        _container.addDataProviderListener(new DataProviderListener<WinApiWipData>()
        {
            
            private static final long serialVersionUID = 1L;

            @Override
            public void onDataChange(DataChangeEvent<WinApiWipData> event)
            {
                _view.getItemCountLabel().setValue(PacAllI18n.localize(WinApiWipDataPresenter.class, I18N_LABEL_GRID_COUNT, calculateRow(mesLotStatus).size()));
            }
        });
        
        _view.getItemCountLabel().setValue(PacAllI18n.localize(WinApiWipDataPresenter.class, I18N_LABEL_GRID_COUNT, calculateRow(mesLotStatus).size()));
    }
    
    protected String createItemCountColumn(String serviceName, WinApiWipData source)
    {
        List<WinApiWipData> itemList = calculateRow(serviceName);
        
        String itemNumber = (itemList.indexOf(source) + 1) + "";
        return itemNumber;
    }

    private List<WinApiWipData> calculateRow(String serviceName)
    {
        List<WinApiWipData> itemList = new ArrayList<>();
        _domainObjectManager.getAll(WinApiWipData.TYPE).forEach(item -> {
            if (((WinApiWipData) item).getWipDataServiceName().equals(serviceName))
            {
                itemList.add((WinApiWipData) item);
            }
        });
        return itemList;
    }

    @Override
    public String getId()
    {
        return this.getClass().getSimpleName();
    }

    @Override
    public Component getView()
    {
        return _view;
    }

    @Override
    public Component getRepresentation()
    {
        return _view;
    }

    @Override
    public void close()
    {
    }

}
