package sg.znt.pac.ui.operator.view;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.themes.ValoTheme;

import de.znt.pac.resources.i18n.PacAllI18n;
import de.znt.pac.ui.vaadin.component.HeaderComponent;
import sg.znt.pac.domainobject.WinApiWipData;

public class WinApiWipDataView
    extends CustomComponent
{

    private static final long serialVersionUID = 1L;
    private static final String I18N_VIEW_TITLE = "WinApiWipDataView.View.Title";
    private static final String I18N_BUTTON_READ_ALL_CAPTION = "WinApiWipDataView.Button.ReadAll.Caption";
    private static final String I18N_BUTTON_CLEAR_CAPTION = "WinApiWipDataView.Button.Clear.Caption";
    private static final String I18N_BUTTON_SUBMIT_CAPTION = "WinApiWipDataView.Button.Submit.Caption";
    private static final String I18N_TEXTFIELD_LOT_ID = "WinApiWipDataView.TextField.LotId.Caption";
    private static final String I18N_TEXTFIELD_LOT_STATE = "WinApiWipDataView.TextField.LotState.Caption";
    private static final String I18N_TEXTFIELD_EQUIPMENT_ID = "WinApiWipDataView.TextField.EquipmentId.Caption";
    private static final String I18N_TEXTFIELD_AUTHORIZATION_LOGIN_ID_CAPTION = "WinApiWipDataView.Authorization.TextField.LoginId.Caption";
    private static final String I18N_TEXTFIELD_AUTHORIZATION_PASSWORD_CAPTION = "WinApiWipDataView.Authorization.TextField.Password.Caption";
    private static final String I18N_BUTTON_LOGOUT_CAPTION = "WinApiWipDataView.Button.Logout.Caption";
    private static final String I18N_LABEL_AUTHORIZATION_MESSAGE = "WinApiWipDataView.Authorization.Label.Text";

    private Grid<WinApiWipData> _grid;
    private Button _readAllButton;
    private Button _clearButton;
    private Button _submitButton;
    private Label _itemCountLabel;
    private TextField _lotIdField;
    private TextField _lotStateField;
    private TextField _equipmentIdField;
    private Button _logoutButton;
    
    public WinApiWipDataView()
    {
        Label headerLabel = new Label(PacAllI18n.localize(this.getClass(), I18N_VIEW_TITLE));
        headerLabel.addStyleName(ValoTheme.LABEL_HUGE);
        headerLabel.addStyleName(ValoTheme.LABEL_BOLD);
        HeaderComponent header = new HeaderComponent(headerLabel);
        
        _lotIdField = new TextField(PacAllI18n.localize(this.getClass(), I18N_TEXTFIELD_LOT_ID));
        _lotIdField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        _lotIdField.setEnabled(false);
        FormLayout lotIdLayout = new FormLayout(_lotIdField);
        lotIdLayout.setMargin(false);
        
        _lotStateField = new TextField(PacAllI18n.localize(this.getClass(), I18N_TEXTFIELD_LOT_STATE));
        _lotStateField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        _lotStateField.setEnabled(false);
        FormLayout lotStateLayout = new FormLayout(_lotStateField);
        lotStateLayout.setMargin(false);
        
        _equipmentIdField = new TextField(PacAllI18n.localize(this.getClass(), I18N_TEXTFIELD_EQUIPMENT_ID));
        _equipmentIdField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        _equipmentIdField.setEnabled(false);
        FormLayout equipmentIdLayout = new FormLayout(_equipmentIdField);
        equipmentIdLayout.setMargin(false);
        
        HorizontalLayout infoLayout = new HorizontalLayout(lotIdLayout, lotStateLayout, equipmentIdLayout);
        
        UI.getCurrent().getPage().getStyles().add(".disable-text-selection "
            + "{ "
            + "user-select: none !important; "
            + "-o-user-select:none !important; "
            + "-moz-user-select: none !important; "
            + "-khtml-user-select: none !important; "
            + "-webkit-user-select: none !important; "
            + "}");
        
        _grid = new Grid<>();
        _grid.getEditor().setEnabled(true);
        _grid.setSelectionMode(SelectionMode.SINGLE);
        _grid.addStyleName(ValoTheme.TABLE_SMALL);
        _grid.addStyleName("disable-text-selection");
        _grid.setSizeFull();
        
        String width = "80px";

        _readAllButton = new Button(PacAllI18n.localize(this.getClass(), I18N_BUTTON_READ_ALL_CAPTION));
        _readAllButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        _readAllButton.addStyleName(ValoTheme.BUTTON_TINY);
        _readAllButton.setWidth(width);
        
        _clearButton = new Button(PacAllI18n.localize(this.getClass(), I18N_BUTTON_CLEAR_CAPTION));
        _clearButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        _clearButton.addStyleName(ValoTheme.BUTTON_TINY);
        _clearButton.setWidth(width);
        
        _submitButton = new Button(PacAllI18n.localize(this.getClass(), I18N_BUTTON_SUBMIT_CAPTION));
        _submitButton.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        _submitButton.addStyleName(ValoTheme.BUTTON_TINY);
        _submitButton.setWidth(width);
        
        _logoutButton = new Button(PacAllI18n.localize(this.getClass(), I18N_BUTTON_LOGOUT_CAPTION));
        _logoutButton.setCaptionAsHtml(true);
        _logoutButton.addStyleName(ValoTheme.BUTTON_DANGER);
        _logoutButton.addStyleName(ValoTheme.BUTTON_QUIET);
        _logoutButton.addStyleName(ValoTheme.BUTTON_TINY);
        _logoutButton.setWidth(width);
        _logoutButton.setVisible(false);
        
        VerticalLayout buttonLayout = new VerticalLayout(_readAllButton, _clearButton, _submitButton, new Label(""), _logoutButton);
        buttonLayout.setComponentAlignment(_logoutButton, Alignment.BOTTOM_LEFT);
        
        _itemCountLabel = new Label();
        
        HorizontalLayout gridLayout = new HorizontalLayout(_grid, buttonLayout);
        gridLayout.setExpandRatio(_grid, 0.9f);
        gridLayout.setExpandRatio(buttonLayout, 0.1f);
        gridLayout.setSizeFull();
        
        HorizontalLayout statusLayout = new HorizontalLayout(_itemCountLabel, new Label(" "));
        statusLayout.setComponentAlignment(_itemCountLabel, Alignment.MIDDLE_LEFT);
        statusLayout.setSizeFull();
        
        VerticalLayout mainLayout = new VerticalLayout(header, infoLayout, gridLayout, statusLayout);
        mainLayout.setComponentAlignment(infoLayout, Alignment.MIDDLE_CENTER);
        mainLayout.setComponentAlignment(gridLayout, Alignment.MIDDLE_CENTER);
        mainLayout.setComponentAlignment(statusLayout, Alignment.MIDDLE_CENTER);
        mainLayout.setMargin(new MarginInfo(true, true, false, true));
        mainLayout.setSizeFull();

        setCompositionRoot(mainLayout);
    }
    
    public Grid<WinApiWipData> getWipDataGrid()
    {
        return _grid;
    }
    
    public Button getReadAllButton()
    {
        return _readAllButton;
    }
    
    public Button getClearButton()
    {
        return _clearButton;
    }
    
    public Button getSubmitButton()
    {
        return _submitButton;
    }
    
    public Button getLogOutButton()
    {
        return _logoutButton;
    }
    
    public Label getItemCountLabel()
    {
        return _itemCountLabel;
    }
    
    public TextField getLotIdField()
    {
        return _lotIdField;
    }
    
    public TextField getLotStateField()
    {
        return _lotStateField;
    }
    
    public TextField getEquipmentIdField()
    {
        return _equipmentIdField;
    }

    public Component getAuthorizationLayout()
    {
        Label message = new Label(PacAllI18n.localize(this.getClass(), I18N_LABEL_AUTHORIZATION_MESSAGE));
        message.addStyleName(ValoTheme.LABEL_TINY);
        
        TextField userIdField = new TextField(PacAllI18n.localize(this.getClass(), I18N_TEXTFIELD_AUTHORIZATION_LOGIN_ID_CAPTION));
        userIdField.setId("idField");
        userIdField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        userIdField.setCaptionAsHtml(true);
        userIdField.setPlaceholder("Required");
        CssLayout userIdLayout = new CssLayout(userIdField);
        
        PasswordField passwordField = new PasswordField(PacAllI18n.localize(this.getClass(), I18N_TEXTFIELD_AUTHORIZATION_PASSWORD_CAPTION));
        passwordField.setId("passwordField");
        passwordField.addStyleName(ValoTheme.TEXTFIELD_TINY);
        passwordField.setCaptionAsHtml(true);
        passwordField.setPlaceholder("Required");
        CssLayout passwordLayout = new CssLayout(passwordField);
        
        VerticalLayout layout = new VerticalLayout(message, userIdLayout, passwordLayout);

        return layout;
    }
}
