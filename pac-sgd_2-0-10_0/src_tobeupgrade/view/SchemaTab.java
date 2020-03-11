package de.znt.pac.mapping.view;


import static de.znt.pac.resources.i18n.PacI18n.localize;

import java.util.List;
import java.util.UUID;

import de.znt.ZModels.ZModel;
import de.znt.ZStandardDialogs.tablelist.TableList;
import de.znt.ZTypes.ZList;
import de.znt.pac.PacConfig;
import de.znt.pac.mapping.NameGenerator;
import de.znt.pac.mapping.NameGenerator.NameValidChecker;
import de.znt.pac.mapping.data.Mapping;
import de.znt.pac.mapping.data.MappingConfiguration;
import de.znt.pac.mapping.data.MappingSet;
import de.znt.pac.mapping.data.Schema;
import de.znt.pac.mapping.data.SchemaComponent;
import de.znt.pac.mapping.data.SchemaItem;
import de.znt.pac.mapping.data.SchemaItemRef;
import de.znt.pac.mapping.data.SchemaItemType;
import de.znt.pac.users.PacUser;
import de.znt.pac.util.ImageUtil;
import de.znt.pac.util.TableSelectionListener;
import de.znt.pac.views.tab.DirtyModel;
import de.znt.uiControl.Button;
import de.znt.uiControl.ButtonBase.ActionEvent;
import de.znt.uiControl.UiAttribute;
import de.znt.util.data.event.EventListener;


// Copyright (c) 2014, By ZNT GmbH.  All Rights Reserved.
// *********************************************************************
//
// ZNT GmbH
// Mautnerstrasse 268
// 84489 Burghausen
// GERMANY
// +49 (8677) 9880-0
//
// This software is furnished under a license and may be used
// and copied only in accordance with the terms of such license.
// This software or any other copies thereof in any form, may not be
// provided or otherwise made available, to any other person or company
// without written consent from ZNT.
//
// ZNT GmbH assumes no responsibility for the use or reliability of
// software which has been modified without approval.
// *********************************************************************
// Description:

public class SchemaTab
    extends DirtyModel
{
    private static final long serialVersionUID = 1L;
    private MappingConfiguration _configuration;
    private final MappingConfigurationModel _mappingConfigurationModel;

    private final ZList<SchemaModel> _schemaList = new ZList<SchemaModel>();
    private final TableList<SchemaModel> _schemaTable;

    private final ZList<SchemaComponentModel> _schemaComponentList = new ZList<SchemaComponentModel>();
    private final TableList<SchemaComponentModel> _schemaComponentTable;

    private final ZList<SchemaItemModel> _schemaItemList = new ZList<SchemaItemModel>();
    private final TableList<SchemaItemModel> _schemaItemTable;

    private final Button _addSchemaButton;
    private final Button _deleteSchemaButton;

    private final Button _addSchemaComponentButton;
    private final Button _deleteSchemaComponentButton;

    private final Button _addSchemaItemButton;
    private final Button _deleteSchemaItemButton;

    private final NameGenerator _schemaNameGenerator;
    private final NameGenerator _schemaComponentNameGenerator;
    private final NameGenerator _schemaItemNameGenerator;

    private SchemaModel _selectedSchemaModel;
    private SchemaComponentModel _selectedSchemaComponentModel;
    private SchemaItemModel _selectedSchemaItemModel;

    /**
     * @param configuration
     * @param mappingConfigurationModel
     * @param model
     */
    public SchemaTab(PacUser user, MappingConfiguration configuration, MappingConfigurationModel mappingConfigurationModel)
    {
        super(user, "SchemaModel", SchemaModel.localizedTabTitle());
        _mappingConfigurationModel = mappingConfigurationModel;

        SchemaTableLineWrapper schemaTlw = new SchemaTableLineWrapper();
        _schemaTable = new TableList<SchemaModel>(_schemaList, schemaTlw, schemaTlw, schemaTlw, new SchemaTableSelectionListener());

        SchemaComponentTableLineWrapper schemaComponentTlw = new SchemaComponentTableLineWrapper();
        _schemaComponentTable = new TableList<SchemaComponentModel>(_schemaComponentList, schemaComponentTlw, schemaComponentTlw, schemaComponentTlw, new SchemaComponentTableSelectionListener());

        SchemaItemReducedTableLineWrapper schemaItemTlw = new SchemaItemReducedTableLineWrapper();
        _schemaItemTable = new TableList<SchemaItemModel>(_schemaItemList, schemaItemTlw, schemaItemTlw, schemaItemTlw, new SchemaItemTableSelectionListener());

        _addSchemaButton = createAddButton(new AddSchemaButtonClickHandler());
        _deleteSchemaButton = createDeleteButton(new RemoveSchemaButtonClickHandler());

        _addSchemaComponentButton = createAddButton(new AddSchemaComponentButtonClickHandler());
        _deleteSchemaComponentButton = createDeleteButton(new RemoveSchemaComponentButtonClickHandler());

        _addSchemaItemButton = createAddButton(new AddSchemaItemButtonClickHandler());
        _deleteSchemaItemButton = createDeleteButton(new RemoveSchemaItemButtonClickHandler());

        _schemaNameGenerator = new NameGenerator(new NameValidChecker()
        {
            @Override
            public boolean isNameValid(String name)
            {
                return getSchema(name) == null;
            }
        });
        _schemaComponentNameGenerator = new NameGenerator(new NameValidChecker()
        {
            @Override
            public boolean isNameValid(String name)
            {
                return getSchemaComponent(name) == null;
            }
        });
        _schemaItemNameGenerator = new NameGenerator(new NameValidChecker()
        {
            @Override
            public boolean isNameValid(String name)
            {
                return getSchemaItem(name) == null;
            }
        });
        initialize(configuration);
    }

    private Button createDeleteButton(EventListener<ActionEvent> handler)
    {
        Button button = new Button();
        button.setIcon(ImageUtil.loadImageIcon("images/openicon/16x16/actions/list-remove-4.png"));
        button.setOnAction(handler);
        return button;
    }

    private Button createAddButton(EventListener<ActionEvent> handler)
    {
        Button button = new Button();
        button.setIcon(ImageUtil.loadImageIcon("images/openicon/16x16/actions/list-add-4.png"));
        button.setOnAction(handler);
        return button;
    }

    /**
     * Update the configuration
     * @param configuration reference
     */
    protected void initialize(MappingConfiguration configuration)
    {
        _configuration = configuration;
        updateSchemaList();
    }

    private void updateSchemaList()
    {
        _schemaList.removeAllElements();
        List<Schema> schemas = _configuration.getSchemas();
        for (Schema schema : schemas)
        {
            _schemaList.addElement(new SchemaModel(schema));
        }
    }

    private void selectedSchema(SchemaModel selectedSchemaModel)
    {
        _selectedSchemaModel = selectedSchemaModel;
        _schemaComponentList.removeAllElements();

        int newSelectionIndex = -1;
        if (selectedSchemaModel != null)
        {
            List<SchemaComponent> schemaComponents = selectedSchemaModel.getSchema().getSchemaComponents();
            ZList<SchemaComponentModel> elementsToAdd = new ZList<SchemaComponentModel>();
            for (SchemaComponent schemaComponent : schemaComponents)
            {
                elementsToAdd.addElement(new SchemaComponentModel(selectedSchemaModel.getSchema(), schemaComponent));
            }
            if (elementsToAdd.getElementCount() > 0)
            {
                _schemaComponentList.addContentsOf(elementsToAdd);
                newSelectionIndex = 0;
            }
        }

        _schemaComponentTable.selectRow(newSelectionIndex, true);
        updateAttrMethods("SchemaDetailModel");
    }

    private void selectedSchemaComponent(SchemaComponentModel selectedSchemaComponentModel)
    {
        _selectedSchemaComponentModel = selectedSchemaComponentModel;
        _schemaItemList.removeAllElements();

        int newSelectionIndex = -1;
        if (selectedSchemaComponentModel != null)
        {
            List<SchemaItem> schemaItems = selectedSchemaComponentModel.getSchemaComponent().getSchemaItems();
            ZList<SchemaItemModel> elementsToAdd = new ZList<SchemaItemModel>();
            for (SchemaItem schemaItem : schemaItems)
            {
                elementsToAdd.addElement(new SchemaItemModel(selectedSchemaComponentModel.getSchema(), selectedSchemaComponentModel.getSchemaComponent(), schemaItem));
            }
            if (elementsToAdd.getElementCount() > 0)
            {
                _schemaItemList.addContentsOf(elementsToAdd);
                newSelectionIndex = 0;
            }
        }
        _schemaItemTable.selectRow(newSelectionIndex,true);
        updateAttrMethods("SchemaComponentDetailModel");
    }

    private void selectedSchemaItem(SchemaItemModel selectedSchemaItemModel)
    {
        _selectedSchemaItemModel = selectedSchemaItemModel;
        updateAttrMethods("SchemaItemDetailModel");
    }

    private class SchemaTableSelectionListener
        implements TableSelectionListener
    {

        @Override
        public void selectionChanged(@SuppressWarnings("rawtypes") TableList source, byte type, int row, int column)
        {
            int selectedRow = _schemaTable.getSelectedRow();
            logDebug("SchemaTableSelectionListener.selectionChanged(): selected row = " + selectedRow + " no. of elements = " + _schemaList.getElementCount());
            if (selectedRow >= 0)
            {
                selectedSchema(_schemaList.getElementAt(selectedRow));
            }
            else
            {
                selectedSchema(null);
            }
        }
    }


    private class SchemaComponentTableSelectionListener
        implements TableSelectionListener
    {

        @Override
        public void selectionChanged(@SuppressWarnings("rawtypes") TableList source, byte type, int row, int column)
        {
            int selectedRow = _schemaComponentTable.getSelectedRow();
            logDebug("SchemaComponentTableSelectionListener.selectionChanged(): selected row = " + selectedRow + " no. of elements = " + _schemaComponentList.getElementCount());
            if (selectedRow >= 0)
            {
                selectedSchemaComponent(_schemaComponentList.getElementAt(selectedRow));
            }
            else
            {
                selectedSchemaComponent(null);
            }
        }
    }


    private class SchemaItemTableSelectionListener
        implements TableSelectionListener
    {

        @Override
        public void selectionChanged(@SuppressWarnings("rawtypes") TableList source, byte type, int row, int column)
        {
            int selectedRow = _schemaItemTable.getSelectedRow();
            logDebug("SchemaItemTableSelectionListener.selectionChanged(): selected row = " + selectedRow + " no. of elements = " + _schemaItemList.getElementCount());
            if (selectedRow >= 0)
            {
                selectedSchemaItem(_schemaItemList.getElementAt(selectedRow));
            }
            else
            {
                selectedSchemaItem(null);
            }
        }
    }


    private class AddSchemaButtonClickHandler
        implements EventListener<ActionEvent>
    {

        /**
         * @see de.znt.uiControl.data.event.EventListener#handle(de.znt.uiControl.data.event.Event)
         */
        @Override
        public void handle(ActionEvent event)
        {
            String name = _schemaNameGenerator.getNewName();

//          Version: pac_16-0-0
//          PAC-2839    Task    Use Java's UUID instead of PAC's IdGenerator    Removed IdGenerator class. Instead static methods from java.util.UUID are used.
//          Schema newSchema = new Schema(null, String.valueOf(IdGenerator.getNewTimeBasedId()), name);
            Schema newSchema = new Schema(null, UUID.randomUUID().toString(), name);
            _configuration.withSchemas(newSchema);
            SchemaModel newSchemaModel = new SchemaModel(newSchema);
            _schemaList.addElement(newSchemaModel);
            int row = _schemaList.indexOf(newSchemaModel);
            _schemaTable.selectRow(row, true);
            getDirtyFlag().setDirty();
            _mappingConfigurationModel.schemasChanged();
        }
    }


    private class AddSchemaComponentButtonClickHandler
        implements EventListener<ActionEvent>
    {

        /**
         * @see de.znt.uiControl.data.event.EventListener#handle(de.znt.uiControl.data.event.Event)
         */
        @Override
        public void handle(ActionEvent event)
        {
            String name = _schemaComponentNameGenerator.getNewName();
            
//          Version: pac_16-0-0
//          PAC-2839    Task    Use Java's UUID instead of PAC's IdGenerator    Removed IdGenerator class. Instead static methods from java.util.UUID are used.
//          SchemaComponent newSchemaComponent = new SchemaComponent(null, String.valueOf(IdGenerator.getNewTimeBasedId()), name);
            SchemaComponent newSchemaComponent = new SchemaComponent(null, UUID.randomUUID().toString(), name);
            Schema schema = _selectedSchemaModel.getSchema();
            schema.withSchemaComponents(newSchemaComponent);
            SchemaComponentModel newSchemaComponentModel = new SchemaComponentModel(schema, newSchemaComponent);
            _schemaComponentList.addElement(newSchemaComponentModel);
            int row = _schemaComponentList.indexOf(newSchemaComponentModel);
            _schemaComponentTable.selectRow(row, true);
            getDirtyFlag().setDirty();
            _mappingConfigurationModel.schemasChanged();
        }
    }


    private class AddSchemaItemButtonClickHandler
        implements EventListener<ActionEvent>
    {

        /**
         * @see de.znt.uiControl.data.event.EventListener#handle(de.znt.uiControl.data.event.Event)
         */
        @Override
        public void handle(ActionEvent event)
        {
            String name = _schemaItemNameGenerator.getNewName();

//          Version: pac_16-0-0
//          PAC-2839    Task    Use Java's UUID instead of PAC's IdGenerator    Removed IdGenerator class. Instead static methods from java.util.UUID are used.
//          SchemaItem newSchemaItem = new SchemaItem(String.valueOf(IdGenerator.getNewTimeBasedId()), name, SchemaItemType.STRING, null);
            SchemaItem newSchemaItem = new SchemaItem(UUID.randomUUID().toString(), name, SchemaItemType.STRING, null);
            Schema schema = _selectedSchemaComponentModel.getSchema();
            SchemaComponent schemaComponent = _selectedSchemaComponentModel.getSchemaComponent();
            schemaComponent.withSchemaItems(newSchemaItem);
            SchemaItemModel newSchemaItemModel = new SchemaItemModel(schema, schemaComponent, newSchemaItem);
            _schemaItemList.addElement(newSchemaItemModel);
            int row = _schemaItemList.indexOf(newSchemaItemModel);
            _schemaItemTable.selectRow(row, true);
            getDirtyFlag().setDirty();
            _mappingConfigurationModel.schemasChanged();
        }
    }


    private class RemoveSchemaItemButtonClickHandler
        implements EventListener<ActionEvent>
    {

        /**
         * @see de.znt.uiControl.data.event.EventListener#handle(de.znt.uiControl.data.event.Event)
         */
        @Override
        public void handle(ActionEvent event)
        {
            int selectedRow = _schemaItemTable.getSelectedRow();
            if (selectedRow >= 0)
            {
                SchemaItem schemaItem = _schemaItemList.getElementAt(selectedRow).getSchemaItem();
                SchemaComponent schemaComponent = _selectedSchemaComponentModel.getSchemaComponent();
                String schemaItemId = schemaItem.getId();
                for (MappingSet mappingSet : _configuration.getMappingSets())
                {
                    for (Mapping mapping : mappingSet.getMappings())
                    {
                        for (SchemaItemRef schemaItemRef : mapping.getSources())
                        {
                            if (schemaItemId.equals(schemaItemRef.getSchemaItemId()))
                            {
                                getUser().showErrorMessage(SchemaModel.localizedSchemaItemStillUsedInMappingSet(schemaItem.getName(), mappingSet.getName()), false, true);
                                return;
                            }
                        }
                        if (mapping.getTarget() != null && schemaItemId.equals(mapping.getTarget().getSchemaItemId()))
                        {
                            getUser().showErrorMessage(SchemaModel.localizedSchemaItemStillUsedInMappingSet(schemaItem.getName(), mappingSet.getName()), false, true);
                            return;
                        }
                    }
                }
                if (getUser().showAreYouSureRequest(localize(PacConfig.ARE_YOU_SURE), SchemaModel.localizedDeleteSchemaItemRequest(schemaItem.getName(), schemaComponent.getName())))
                {
                    schemaComponent.getSchemaItems().remove(schemaItem);
                    _schemaItemList.removeElementAt(selectedRow);
                    getDirtyFlag().setDirty();
                    _mappingConfigurationModel.schemasChanged();
                }
            }
            else
            {
                getUser().showWarningMessage(localize(PacConfig.NO_ELEMENT_SELECT), false, true);
            }
        }
    }


    private class RemoveSchemaComponentButtonClickHandler
        implements EventListener<ActionEvent>
    {

        /**
         * @see de.znt.uiControl.data.event.EventListener#handle(de.znt.uiControl.data.event.Event)
         */
        @Override
        public void handle(ActionEvent event)
        {
            int selectedRow = _schemaComponentTable.getSelectedRow();
            if (selectedRow >= 0)
            {
                SchemaComponent schemaComponent = _schemaComponentList.getElementAt(selectedRow).getSchemaComponent();
                Schema schema = _selectedSchemaModel.getSchema();
                String schemaComponentId = schemaComponent.getId();
                for (MappingSet mappingSet : _configuration.getMappingSets())
                {
                    for (Mapping mapping : mappingSet.getMappings())
                    {
                        for (SchemaItemRef schemaItemRef : mapping.getSources())
                        {
                            if (schemaComponentId.equals(schemaItemRef.getSchemaComponentId()))
                            {
                                getUser().showErrorMessage(SchemaModel.localizedSchemaComponentStillUsedInMappingSet(schemaComponent.getName(), mappingSet.getName()), false, true);
                                return;
                            }
                        }
                        if (mapping.getTarget() != null && schemaComponentId.equals(mapping.getTarget().getSchemaComponentId()))
                        {
                            getUser().showErrorMessage(SchemaModel.localizedSchemaComponentStillUsedInMappingSet(schemaComponent.getName(), mappingSet.getName()), false, true);
                            return;
                        }
                    }
                }
                if (getUser().showAreYouSureRequest(localize(PacConfig.ARE_YOU_SURE), SchemaModel.localizedDeleteSchemaComponentRequest(schemaComponent.getName(), schema.getName())))
                {
                    schema.getSchemaComponents().remove(schemaComponent);
                    _schemaComponentList.removeElementAt(selectedRow);
                    getDirtyFlag().setDirty();
                    _mappingConfigurationModel.schemasChanged();
                }
            }
            else
            {
                getUser().showWarningMessage(localize(PacConfig.NO_ELEMENT_SELECT), false, true);
            }
        }
    }


    private class RemoveSchemaButtonClickHandler
        implements EventListener<ActionEvent>
    {

        /**
         * @see de.znt.uiControl.data.event.EventListener#handle(de.znt.uiControl.data.event.Event)
         */
        @Override
        public void handle(ActionEvent event)
        {
            int selectedRow = _schemaTable.getSelectedRow();
            if (selectedRow >= 0)
            {
                Schema schema = _schemaList.getElementAt(selectedRow).getSchema();
                String schemaId = schema.getId();
                for (MappingSet mappingSet : _configuration.getMappingSets())
                {
                    for (Mapping mapping : mappingSet.getMappings())
                    {
                        for (SchemaItemRef schemaItemRef : mapping.getSources())
                        {
                            if (schemaId.equals(schemaItemRef.getSchemaId()))
                            {
                                getUser().showErrorMessage(SchemaModel.localizedSchemaStillUsedInMappingSet(schema.getName(), mappingSet.getName()), false, true);
                                return;
                            }
                        }
                        if (mapping.getTarget() != null && schemaId.equals(mapping.getTarget().getSchemaId()))
                        {
                            getUser().showErrorMessage(SchemaModel.localizedSchemaStillUsedInMappingSet(schema.getName(), mappingSet.getName()), false, true);
                            return;
                        }
                    }
                }
                if (getUser().showAreYouSureRequest(localize(PacConfig.ARE_YOU_SURE), SchemaModel.localizedDeleteSchemaRequest(schema.getName())))
                {
                    _configuration.getSchemas().remove(schema);
                    _schemaList.removeElementAt(selectedRow);
                    getDirtyFlag().setDirty();
                    _mappingConfigurationModel.schemasChanged();
                }
            }
            else
            {
                getUser().showWarningMessage(localize(PacConfig.NO_ELEMENT_SELECT), false, true);
            }
        }
    }

    @UiAttribute
    public ZModel getSchemaTable()
    {
        return _schemaTable;
    }

    @UiAttribute
    public ZModel getSchemaComponentTable()
    {
        return _schemaComponentTable;
    }

    @UiAttribute
    public ZModel getSchemaItemTable()
    {
        return _schemaItemTable;
    }

    @UiAttribute
    public Button getCreateSchema()
    {
        return _addSchemaButton;
    }

    @UiAttribute
    public Button getDeleteSchema()
    {
        return _deleteSchemaButton;
    }

    @UiAttribute
    public Button getCreateSchemaComponent()
    {
        return _addSchemaComponentButton;
    }

    @UiAttribute
    public Button getDeleteSchemaComponent()
    {
        return _deleteSchemaComponentButton;
    }

    @UiAttribute
    public Button getCreateSchemaItem()
    {
        return _addSchemaItemButton;
    }

    @UiAttribute
    public Button getDeleteSchemaItem()
    {
        return _deleteSchemaItemButton;
    }

    @UiAttribute
    public ZModel getSchemaDetailModel()
    {
        if (_selectedSchemaModel != null)
        {
            return new SchemaDetailModel(_selectedSchemaModel, _configuration.getSchemas(), getUser(), this);
        }
        return null;
    }

    @UiAttribute
    public ZModel getSchemaComponentDetailModel()
    {
        if (_selectedSchemaComponentModel != null)
        {
            return new SchemaComponentDetailModel(_selectedSchemaComponentModel, getUser(), this);
        }
        return null;
    }

    @UiAttribute
    public ZModel getSchemaItemDetailModel()
    {
        if (_selectedSchemaItemModel != null)
        {
            return new SchemaItemDetailModel(_selectedSchemaItemModel, getUser(), this);
        }
        return null;
    }

    public void currentSchemaChanged()
    {
        getDirtyFlag().setDirty();
        _mappingConfigurationModel.schemasChanged();
    }

    public void currentSchemaComponentChanged()
    {
        getDirtyFlag().setDirty();
        _mappingConfigurationModel.schemasChanged();
    }

    public void currentSchemaItemChanged()
    {
        getDirtyFlag().setDirty();
        _mappingConfigurationModel.schemasChanged();
    }

    public Schema getSchema(String schemaName)
    {
        for (Schema schema : _configuration.getSchemas())
        {
            if (schemaName.equals(schema.getName()))
            {
                return schema;
            }
        }
        return null;
    }

    public SchemaComponent getSchemaComponent(String schemaComponentName)
    {
        if (_selectedSchemaModel != null)
        {
            for (SchemaComponent schemaComponent : _selectedSchemaModel.getSchema().getSchemaComponents())
            {
                if (schemaComponentName.equals(schemaComponent.getName()))
                {
                    return schemaComponent;
                }
            }
        }
        return null;
    }

    public SchemaItem getSchemaItem(String schemaItemName)
    {
        if (_selectedSchemaComponentModel != null)
        {
            for (SchemaItem schemaItem : _selectedSchemaComponentModel.getSchemaComponent().getSchemaItems())
            {
                if (schemaItemName.equals(schemaItem.getName()))
                {
                    return schemaItem;
                }
            }
        }
        return null;
    }
}
// $RCSfile: SchemaTab.java,v $
