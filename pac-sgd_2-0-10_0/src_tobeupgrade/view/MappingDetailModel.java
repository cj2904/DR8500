// Copyright (c) 2014, By ZNT GmbH.  All Rights Reserved.
//*********************************************************************
// 
//                     ZNT GmbH
//                     Mautnerstrasse 268
//                     84489 Burghausen
//                     GERMANY
//                     +49 (8677) 9880-0
//
// This software is furnished under a license and may be used
// and copied only in accordance with the terms of such license.
// This software or any other copies thereof in any form, may not be
// provided or otherwise made available, to any other person or company
// without written consent from ZNT.
//
// ZNT GmbH assumes no responsibility for the use or reliability of
// software which has been modified without approval.
//*********************************************************************

package de.znt.pac.mapping.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.znt.ZModels.ZListModel;
import de.znt.ZModels.ZModel;
import de.znt.ZModels.Event.ModelChangedEvent;
import de.znt.ZModels.Event.ModelChangedListener;
import de.znt.ZStandardDialogs.tablelist.TableList;
import de.znt.ZTypes.ZComposite;
import de.znt.ZTypes.ZConstantString;
import de.znt.ZTypes.ZList;
import de.znt.ZTypes.ZSingleSelectionListWrapper;
import de.znt.ZTypes.Event.ModelChangedEventImpl;
import de.znt.pac.mapping.SchemaItemRefComparator;
import de.znt.pac.mapping.data.Conversion;
import de.znt.pac.mapping.data.Mapping;
import de.znt.pac.mapping.data.MappingConfiguration;
import de.znt.pac.mapping.data.Schema;
import de.znt.pac.mapping.data.SchemaComponent;
import de.znt.pac.mapping.data.SchemaItem;
import de.znt.pac.mapping.data.SchemaItemRef;
import de.znt.pac.util.TableSelectionListener;
import de.znt.uiControl.UiAttribute;
import de.znt.uiManager.ModelLifecycleAware;
import de.znt.util.error.ErrorManager;
import de.znt.zutil.model.ZLoggableComposite;

public class MappingDetailModel
    extends ZLoggableComposite
    implements ModelLifecycleAware, ModelChangedListener
{
    private static final long serialVersionUID = 1L;
    private static final NullConversion NULLCONVERSION = new NullConversion();

    private MappingConfiguration _mappingConfiguration;
    private List<Schema> _runtimeSchemas;
    private Mapping _mapping;
    private final MappingSetView _mappingSetModel;
    private final ZList<SchemaWrapperModel> _schemaList = new ZList<SchemaWrapperModel>();
    private final ZList<SchemaComponentWrapperModel> _sourceSchemaComponentFilterList = new ZList<SchemaComponentWrapperModel>();
    private final ZList<SchemaComponentWrapperModel> _targetSchemaComponentFilterList = new ZList<SchemaComponentWrapperModel>();
    private final ZSingleSelectionListWrapper<SchemaWrapperModel> _sourceSchemaFilter = new ZSingleSelectionListWrapper<SchemaWrapperModel>(_schemaList);
    private final ZSingleSelectionListWrapper<SchemaComponentWrapperModel> _sourceSchemaComponentFilter = new ZSingleSelectionListWrapper<SchemaComponentWrapperModel>(_sourceSchemaComponentFilterList);
    private final ZSingleSelectionListWrapper<SchemaComponentWrapperModel> _targetSchemaComponentFilter = new ZSingleSelectionListWrapper<SchemaComponentWrapperModel>(_targetSchemaComponentFilterList);
    private final ZSingleSelectionListWrapper<SchemaWrapperModel> _targetSchemaFilter = new ZSingleSelectionListWrapper<SchemaWrapperModel>(_schemaList);
    private final ZList<SchemaItemModel> _sourceSchemaItemList = new ZList<SchemaItemModel>();
    private final ZList<SchemaItemModel> _targetSchemaItemList = new ZList<SchemaItemModel>();
    private final TableList<SchemaItemModel> _sourceSchemaItemTable;
    private final TableList<SchemaItemModel> _targetSchemaItemTable;
    private final ZList<ConversionWrapperModel> _conversionList = new ZList<ConversionWrapperModel>();
    private final ZSingleSelectionListWrapper<ConversionWrapperModel> _conversionListWrapper = new ZSingleSelectionListWrapper<ConversionWrapperModel>(_conversionList);
    private boolean _conversionListIsInitialized = false;
    private final SourceIndexesModel _sourceIndexesModel = new SourceIndexesModel(new ArrayList<Integer>(), this);;

    private final HashMap<String, Integer> _prevSourceItemScrollPosition = new HashMap<String, Integer>();
    private final HashMap<String, Integer> _prevTargetItemScrollPosition = new HashMap<String, Integer>();
    private String _previousSourceSchemaFilter = null;
    private String _previousTargetSchemaFilter = null;
    private String _previousSourceSchemaComponentFilter = null;
    private String _previousTargetSchemaComponentFilter = null;
    private final SchemaItemAssignmentListener _targetSchemaItemSelectionChangedListener = SchemaItemAssignmentListener.createListenerForTarget(this);
    private final SchemaItemAssignmentListener _sourceSchemaItemSelectionChangedListener = SchemaItemAssignmentListener.createListenerForSource(this);

    public MappingDetailModel(MappingConfiguration mappingConfiguration, List<Schema> runtimeSchemas, Mapping mapping,
                              MappingSetView mappingSetModel)
    {
        _mappingConfiguration = mappingConfiguration;
        _runtimeSchemas = runtimeSchemas;
        _mappingSetModel = mappingSetModel;

        _sourceSchemaItemTable = createSchemaItemTableList(_sourceSchemaItemList, new SourceSchemaItemTableSelectionListener());
        _targetSchemaItemTable = createSchemaItemTableList(_targetSchemaItemList, new TargetSchemaItemTableSelectionListener());

        initConversionList();

        addSchemas(_runtimeSchemas, _schemaList);
        addSchemas(_mappingConfiguration.getSchemas(), _schemaList);
        initSourceIndexesModel(null);

        _sourceSchemaFilter.addModelChangedListener(this);
        _sourceSchemaComponentFilter.addModelChangedListener(this);
        _targetSchemaFilter.addModelChangedListener(this);
        _targetSchemaComponentFilter.addModelChangedListener(this);

        updateMapping(mapping);
    }

    /**
     * update mapping configuration
     * @param mappingConfiguration the mapping configuration
     * @param runtimeSchemas the runtime schema
     */
    void mappingConfigurationChanged(MappingConfiguration mappingConfiguration, List<Schema> runtimeSchemas)
    {
        getLogger().debug("mappingConfigurationChanged():");
        String targetComponentFilterId = getTargetSchemaComponentFilterId();
        String sourceComponentFilterId = getSourceSchemaComponentFilterId();
        String targetSchemaFilterId = getTargetSchemaFilterId();
        String sourceSchemaFilterId = getSourceSchemaFilterId();

        _mappingConfiguration = mappingConfiguration;
        _runtimeSchemas = runtimeSchemas;       

        ZList<SchemaWrapperModel> schemaList = new ZList<SchemaWrapperModel>();
        addSchemas(_runtimeSchemas, schemaList);
        addSchemas(_mappingConfiguration.getSchemas(), schemaList);
        _schemaList.removeAllElements();
        _schemaList.addContentsOf(schemaList);

        selectFilterAndRefreshSchemaItemsInTable(sourceSchemaFilterId, targetSchemaFilterId, sourceComponentFilterId, targetComponentFilterId);
    }

    void conversionsChanged()
    {
        initConversionList();
    }

    /**
     * Init conversion list
     */
    private void initConversionList()
    {
        try
        {
            getLogger().debug("initConversionList():");
            _conversionListIsInitialized = true;
            ZList<ConversionWrapperModel> newElements = new ZList<ConversionWrapperModel>();
            newElements.addElement(new ConversionWrapperModel(NULLCONVERSION));
            for (Conversion conversion : _mappingConfiguration.getConversions())
            {
                newElements.addElement(new ConversionWrapperModel(conversion));
            }
            _conversionList.disableModelChangedEvent();
            _conversionList.removeAllElements();
            _conversionList.addContentsOf(newElements);
            _conversionList.enableModelChangedEvent(null);
        }
        finally
        {
            _conversionListIsInitialized = false;
        }
    }


    public synchronized void updateMapping(Mapping mapping)
    {
        getLogger().debug("updateMapping(): refresh with new mapping ...");
        _mapping = mapping;
        String sourceSchemaFilterId = null;
        String targetSchemaFilterId = null;
        String sourceSchemaComponentFilterId = null;
        String targetSchemaComponentFilterId = null;
        String conversionId = null;
        if (mapping != null)
        {
            conversionId = mapping.getConversionId();

            sourceSchemaFilterId = _previousSourceSchemaFilter;
            targetSchemaFilterId = _previousTargetSchemaFilter;
            sourceSchemaComponentFilterId = _previousSourceSchemaComponentFilter;
            targetSchemaComponentFilterId = _previousTargetSchemaComponentFilter;

            if (getMappingTarget() != null)
            {
                String targetSchemaId = getMappingTarget().getSchemaId();
                String targetSchemaComponentId = getMappingTarget().getSchemaComponentId();
                if (targetSchemaId != null)
                {
                    targetSchemaFilterId = targetSchemaId;
                }
                if (targetSchemaComponentId != null)
                {
                    targetSchemaComponentFilterId = targetSchemaComponentId;
                }
            }
            if (getMappingSource() != null)
            {
                String sourceSchemaId = getMappingSource().getSchemaId();
                String sourceSchemaComponentId = getMappingSource().getSchemaComponentId();
                if (sourceSchemaId != null)
                {
                    sourceSchemaFilterId = sourceSchemaId;
                }
                if (sourceSchemaComponentId != null)
                {
                    sourceSchemaComponentFilterId = sourceSchemaComponentId;
                }
            }
        }

        initConversionList();
        selectConversion(conversionId);

        selectFilterAndRefreshSchemaItemsInTable(sourceSchemaFilterId, targetSchemaFilterId, sourceSchemaComponentFilterId, targetSchemaComponentFilterId);
        initSourceIndexesModel(getMappingSource());

        scrollSchemaItemTableToPreviousPosition(_sourceSchemaItemTable, _prevSourceItemScrollPosition, sourceSchemaFilterId + "_" + sourceSchemaComponentFilterId);
        scrollSchemaItemTableToPreviousPosition(_targetSchemaItemTable, _prevTargetItemScrollPosition, targetSchemaFilterId + "_" + targetSchemaComponentFilterId);
    }

    /**
     * @param table the table list
     * @param scrollPositionMap the scroll position map
     * @param scrollKey the scroll key
     */
    private void scrollSchemaItemTableToPreviousPosition(TableList<SchemaItemModel> table, HashMap<String, Integer> scrollPositionMap, String scrollKey)
    {
        Integer previousScrollPosition = scrollPositionMap.get(scrollKey);
        int newPosition = -1;
        if (table.getSelectedRow() < 0 && previousScrollPosition != null)
        {
            newPosition = previousScrollPosition.intValue();
        }
        try
        {
            getLogger().debug("updateMapping(): scroll to position " + newPosition);
            table.scrollToRow(newPosition);
        }
        catch (ArrayIndexOutOfBoundsException aie)
        {
            ErrorManager.handleError(aie, this);
        }

    }

    /**
     * @param conversionId the conversion ID
     */
    void selectConversion(String conversionId)
    {
        try
        {
            _conversionListIsInitialized = true;
            selectIdInDropDownlist(conversionId, _conversionListWrapper);
        }
        finally
        {
            _conversionListIsInitialized = false;
        }
    }

    /**
     * @param elementIdToBeSelected the schema id to be selected
     * @param dropDownlist the filter list wrapper
     * @param filterList the filter list
     */
    String selectIdInDropDownlist(String elementIdToBeSelected, ZSingleSelectionListWrapper< ? extends DropDownlistModel> dropDownlist)
    {
        int selection = -1;
        String newSelection = null;
        ZListModel< ? extends DropDownlistModel> dropDownlistTargetList = dropDownlist.getTarget();
        getLogger().debug("selectElementInDropDownlist(): element to be selected = " + elementIdToBeSelected);
        if (elementIdToBeSelected != null)
        {
            for (int i = 0; i < dropDownlistTargetList.getElementCount(); i++)
            {
                DropDownlistModel element = dropDownlistTargetList.getElementAt(i);
                if (elementIdToBeSelected.equals(element.getId()) == true)
                {
                    selection = i;
                    newSelection = elementIdToBeSelected;
                    break;
                }
            }
        }
        if (dropDownlist.getSelection() != selection)
        {
            getLogger().debug("selectIdInDropDownlist(): new selection = " + selection);
            dropDownlist.setSelection(selection);
        }
        else
        {
            getLogger().debug("selectIdInDropDownlist(): element " + elementIdToBeSelected + " is already selected");
        }
        return newSelection;
    }

    void selectFilterAndRefreshSchemaItemsInTable(String sourceSchemaFilterId, String targetSchemaFilterId, String sourceComponentFilterId, String targetComponentFilterId)
    {
        getLogger().debug("selectFilterAndRefreshSchemaItemsInTable(): source schema ID:" + sourceSchemaFilterId + "; source schema component ID: " + sourceComponentFilterId + "; target schema ID: " + targetSchemaFilterId
                             + "; target schema component ID: " + targetComponentFilterId);
        String newSelection = selectIdInDropDownlist(sourceSchemaFilterId, _sourceSchemaFilter);
        if (newSelection != null)
        {
            _previousSourceSchemaFilter = newSelection;
        }
        newSelection = selectIdInDropDownlist(targetSchemaFilterId, _targetSchemaFilter);
        if (newSelection != null)
        {
            _previousTargetSchemaFilter = newSelection;
        }

        newSelection = refreshSourceSchemaComponentList(sourceComponentFilterId);
        if (newSelection != null)
        {
            _previousSourceSchemaComponentFilter = newSelection;
        }
        newSelection = refreshTargetSchemaComponentList(targetComponentFilterId);
        if (newSelection != null)
        {
            _previousTargetSchemaComponentFilter = newSelection;
        }

        initSourceSchemaItemList();
        initTargetSchemaItemList();
    }

    String getPreviousSourceSchemaFilter()
    {
        return _previousSourceSchemaFilter;
    }

    String getPreviousTargetSchemaFilter()
    {
        return _previousTargetSchemaFilter;
    }

    String getPreviousSourceSchemaComponentFilter()
    {
        return _previousSourceSchemaComponentFilter;
    }

    String getPreviousTargetSchemaComponentFilter()
    {
        return _previousTargetSchemaComponentFilter;
    }

    private SchemaItemRef getMappingTarget()
    {
        if (_mapping != null)
        {
            return _mapping.getTarget();
        }
        return null;
    }

    private SchemaItemRef getMappingSource()
    {
        if (_mapping == null)
        {
            return null;
        }
        List<SchemaItemRef> sources = _mapping.getSources();
        SchemaItemRef source = null;
        if (sources.size() > 0)
        {
            source = sources.get(0);
        }
        return source;
    }

    void initSourceIndexesModel(SchemaItemRef source)
    {
        List<Integer> sourceIndexes = null;
        if (source == null)
        {
            sourceIndexes = new ArrayList<Integer>();
        }
        else
        {
            sourceIndexes = source.getSourceIndexes();
        }

        _sourceIndexesModel.updateSourceIndexes(sourceIndexes);
    }

    /**
     * Create table list for schema items
     * @param schemaItemList the schema item list
     * @param tableSelectionListener the table selection listener
     * @return the created table list
     */
    private TableList<SchemaItemModel> createSchemaItemTableList(ZList<SchemaItemModel> schemaItemList, TableSelectionListener tableSelectionListener)
    {
        SchemaItemTableLineWrapper schemaItemTlw = new SchemaItemTableLineWrapper();
        TableList<SchemaItemModel> tableList = new TableList<SchemaItemModel>(schemaItemList, schemaItemTlw, schemaItemTlw, schemaItemTlw, tableSelectionListener);
        tableList.setScrollToSelection(true);
        tableList.setFilterable(true);
        tableList.setFilterTableInitiallyDisplayed(false);
        tableList.setClipboardEnabled(true);
        tableList.setCellEditable(true);
        return tableList;
    }

    private void initSourceSchemaItemList()
    {
        _sourceSchemaItemList.disableModelChangedEvent();
        _sourceSchemaItemList.removeAllElements();
        SchemaItemRef source = getMappingSource();
        SchemaComponentWrapperModel selectedSchemaComponentModel = _sourceSchemaComponentFilter.getSelectedElement();

        String filteredSchemaId = getSourceSchemaFilterId();

        ZList<SchemaItemModel> newElements = new ZList<SchemaItemModel>();
        addSchemaItems(_runtimeSchemas, newElements, filteredSchemaId, selectedSchemaComponentModel, true);
        addSchemaItems(_mappingConfiguration.getSchemas(), newElements, filteredSchemaId, selectedSchemaComponentModel, true);
        _sourceSchemaItemList.addContentsOf(newElements);
        _sourceSchemaItemList.enableModelChangedEvent(new ModelChangedEventImpl(_sourceSchemaItemList));
        _sourceSchemaItemTable.selectRow(indexOf(_sourceSchemaItemList, source), false);

        _sourceSchemaItemSelectionChangedListener.updateMappingAndSchemaItemModels(_mapping, _sourceSchemaItemList);
    }

    private void initTargetSchemaItemList()
    {
        _targetSchemaItemList.disableModelChangedEvent();
        _targetSchemaItemList.removeAllElements();
        SchemaComponentWrapperModel selectedSchemaComponentModel = _targetSchemaComponentFilter.getSelectedElement();

        String filteredTargetSchemaId = getTargetSchemaFilterId();
        ZList<SchemaItemModel> newElements = new ZList<SchemaItemModel>();
        addSchemaItems(_runtimeSchemas, newElements, filteredTargetSchemaId, selectedSchemaComponentModel, false);
        addSchemaItems(_mappingConfiguration.getSchemas(), newElements, filteredTargetSchemaId, selectedSchemaComponentModel, false);
        _targetSchemaItemList.addContentsOf(newElements);
        _targetSchemaItemList.enableModelChangedEvent(new ModelChangedEventImpl(_targetSchemaItemList));
        _targetSchemaItemTable.selectRow(indexOf(_targetSchemaItemList, getMappingTarget()), false);

        _targetSchemaItemSelectionChangedListener.updateMappingAndSchemaItemModels(_mapping, _targetSchemaItemList);
    }

    /**
     * @param schemas the schemas to be added to the ZList
     * @param schemaList the ZList
     */
    private void addSchemas(List<Schema> schemas, ZList<SchemaWrapperModel> schemaList)
    {
        ZList<SchemaWrapperModel> newElements = new ZList<SchemaWrapperModel>();
        for (Schema schema : schemas)
        {
            newElements.addElement(new SchemaWrapperModel(schema));
        }
        schemaList.addContentsOf(newElements);
    }

    /**
     * @return  the selected source schema ID
     */
    String getSourceSchemaFilterId()
    {
        return getFilteredSchemaId(_sourceSchemaFilter);
    }

    /**
     * @return the selected schema component id
     */
    String getSourceSchemaComponentFilterId()
    {
        return getFilteredComponentId(_sourceSchemaComponentFilter);
    }

    /**
     * @return the selected schema component id
     */
    String getTargetSchemaComponentFilterId()
    {
        return getFilteredComponentId(_targetSchemaComponentFilter);
    }

    /**
     * Determines the selected filtered component ID
     * @param filter the filter
     * @return the selected component ID, null if list is empty or no selection
     */
    private String getFilteredComponentId(ZSingleSelectionListWrapper<SchemaComponentWrapperModel> filter)
    {
        int selection = filter.getSelection();
        ZListModel<SchemaComponentWrapperModel> filterList = filter.getTarget();
        if (filterList.getElementCount() > 0 && selection >= 0)
        {
            return filterList.getElementAt(selection).getSchemaComponent().getId();
        }
        else
        {
            return null;
        }
    }

    /**
     * @return the selected target schema ID
     */
    String getTargetSchemaFilterId()
    {
        return getFilteredSchemaId(_targetSchemaFilter);
    }

    /**
     * @param filter the filter
     * @return the selected schema ID, null if list is empty of no selection
     */
    private String getFilteredSchemaId(ZSingleSelectionListWrapper<SchemaWrapperModel> filter)
    {
        ZListModel<SchemaWrapperModel> target = filter.getTarget();
        int selection = filter.getSelection();
        if (target.getElementCount() > 0 && selection >= 0)
        {
            return target.getElementAt(selection).getSchema().getId();
        }
        else
        {
            return null;
        }
    }

    private void addSchemaItems(List<Schema> schemaList, ZList<SchemaItemModel> schemaItemList, String filteredSchemaId, SchemaComponentWrapperModel selectedComponentModel, boolean includeSchemaItemsForListParameters)
    {
        SchemaComponent selectedSchemaComponent = null;
        if (selectedComponentModel != null)
        {
            selectedSchemaComponent = selectedComponentModel.getSchemaComponent();
        }

        for (Schema schema : schemaList)
        {
            if (schema.getId().equals(filteredSchemaId) == true)
            {
                List<SchemaComponent> schemaComponents = schema.getSchemaComponents();
                for (SchemaComponent schemaComponent : schemaComponents)
                {
                    if (selectedComponentIsNullOrMatches(selectedSchemaComponent, schemaComponent) == true)
                    {
                        List<SchemaItem> schemaItems = schemaComponent.getSchemaItems();
                        for (SchemaItem schemaItem : schemaItems)
                        {
                            SchemaItemModel element = new SchemaItemModel(schema, schemaComponent, schemaItem);
                            if (schemaItem.getName().contains("#"))
                            {
                                if (includeSchemaItemsForListParameters == true)
                                {
                                    schemaItemList.addElement(element);
                                }
                            }
                            else
                            {
                                schemaItemList.addElement(element);
                            }
                    }
                    }
                }
            }
        }
    }

    private boolean selectedComponentIsNullOrMatches(SchemaComponent selectedComponent, SchemaComponent schemaComponent)
    {
        return selectedComponent == null || selectedComponent.getId().equals(schemaComponent.getId());
    }

    private int indexOf(ZList<SchemaItemModel> schemaItemList, SchemaItemRef ref)
    {
        if (ref != null)
        {
            getLogger().debug("indexOf(): schema item ID " + ref.getSchemaItemId());
            SchemaItemRefComparator comparator = new SchemaItemRefComparator();
            int elementCount = schemaItemList.getElementCount();
            for (int index = 0; index < elementCount; index++)
            {
                SchemaItemModel schemaItemModel = schemaItemList.getElementAt(index);
                SchemaItemRef schemaRef = schemaItemModel.getSchemaItemRef();
                if (comparator.compare(ref, schemaRef) == 0)
                {
                    return index;
                }
            }
        }
        return -1;
    }

    @UiAttribute
    public ZSingleSelectionListWrapper<SchemaWrapperModel> getSourceSchemaFilter()
    {
        return _sourceSchemaFilter;
    }

    @UiAttribute
    public ZSingleSelectionListWrapper<SchemaComponentWrapperModel> getSourceSchemaComponentFilter()
    {
        return _sourceSchemaComponentFilter;
    }

    @UiAttribute
    public TableList<SchemaItemModel> getSourceSchemaItemTable()
    {
        return _sourceSchemaItemTable;
    }

    @UiAttribute
    public ZSingleSelectionListWrapper<ConversionWrapperModel> getConversion()
    {
        return _conversionListWrapper;
    }

    @UiAttribute
    public ZSingleSelectionListWrapper<SchemaWrapperModel> getTargetSchemaFilter()
    {
        return _targetSchemaFilter;
    }

    @UiAttribute
    public ZSingleSelectionListWrapper<SchemaComponentWrapperModel> getTargetSchemaComponentFilter()
    {
        return _targetSchemaComponentFilter;
    }

    @UiAttribute
    public TableList<SchemaItemModel> getTargetSchemaItemTable()
    {
        return _targetSchemaItemTable;
    }

    @UiAttribute
    public SourceIndexesModel getSourceIndexesModel()
    {
        return _sourceIndexesModel;
    }

    @Override
    public void modelInitialize()
    {
        addModelChangedListenerIfRequired(_conversionListWrapper, this);
        addModelChangedListenersToFilters();
        _sourceSchemaItemSelectionChangedListener.updateMappingAndSchemaItemModels(_mapping, _sourceSchemaItemList);
        _targetSchemaItemSelectionChangedListener.updateMappingAndSchemaItemModels(_mapping, _targetSchemaItemList);
    }

    @Override
    public void modelCleanup()
    {
        _sourceSchemaItemSelectionChangedListener.updateMappingAndSchemaItemModels(null, null);
        _targetSchemaItemSelectionChangedListener.updateMappingAndSchemaItemModels(null, null);
        _conversionListWrapper.removeModelChangedListener(this);
        removeModelChangedListenersFromFilters();
    }


    /**
     * Removes the model changed listeners from the drop down lists
     */
    private void removeModelChangedListenersFromFilters()
    {
        _sourceSchemaFilter.removeModelChangedListener(this);
        _sourceSchemaComponentFilter.removeModelChangedListener(this);
        _targetSchemaFilter.removeModelChangedListener(this);
        _targetSchemaComponentFilter.removeModelChangedListener(this);
    }

    /**
     * Add model changed listeners to filters
     */
    private void addModelChangedListenersToFilters()
    {
        addModelChangedListenerIfRequired(_sourceSchemaFilter, this);
        addModelChangedListenerIfRequired(_targetSchemaFilter, this);
        addModelChangedListenerIfRequired(_sourceSchemaComponentFilter, this);
        addModelChangedListenerIfRequired(_targetSchemaComponentFilter, this);
    }

    private void addModelChangedListenerIfRequired(ZModel model, ModelChangedListener listener)
    {
        if (model.isModelChangedListener(listener) == false)
        {
            model.addModelChangedListener(listener);
        }
    }

    @Override
    public void modelChanged(ModelChangedEvent e)
    {
        if (e.getSource() == _sourceSchemaFilter)
        {
            getLogger().debug("modelChanged(): source schema filter changed");
            if (getSourceSchemaFilterId() != null)
            {
                _previousSourceSchemaFilter = getSourceSchemaFilterId();
            }
            refreshSourceSchemaComponentList(null);
        }
        else if (e.getSource() == _sourceSchemaComponentFilter)
        {
            getLogger().debug("modelChanged(): source schema component filter changed");
            if (getSourceSchemaComponentFilterId() != null)
            {
                _previousSourceSchemaComponentFilter = getSourceSchemaComponentFilterId();
            }
            initSourceSchemaItemList();
        }
        else if (e.getSource() == _targetSchemaFilter)
        {
            getLogger().debug("modelChanged(): target schema filter changed");
            if (getTargetSchemaFilterId() != null)
            {
                _previousTargetSchemaFilter = getTargetSchemaFilterId();
            }
            refreshTargetSchemaComponentList(null);
        }
        else if (e.getSource() == _targetSchemaComponentFilter)
        {
            getLogger().debug("modelChanged(): target schema component filter changed");
            if (getTargetSchemaComponentFilterId() != null)
            {
                _previousTargetSchemaComponentFilter = getTargetSchemaComponentFilterId();
            }
            initTargetSchemaItemList();
        }
        else if (e.getSource() == _conversionListWrapper)
        {
            handleConversionSelectionChanged();
        }
    }

    private void handleConversionSelectionChanged()
    {
        if (_conversionListIsInitialized == true)
        {
            return;
        }
        ConversionWrapperModel conversionWrapperModel = _conversionListWrapper.getSelectedElement();
        if (_mapping != null)
        {
            String conversionId = null;
            if (conversionWrapperModel != null)
            {
                conversionId = conversionWrapperModel.getConversion().getId();
            }
            if (isConversionModified(conversionId))
            {
                _mapping.setConversionId(conversionId);
                currentMappingChanged();
            }
        }
    }

    /**
     * @param newConversionId the new conversion id
     * @return true if conversion is modified
     */
    boolean isConversionModified(String newConversionId)
    {
        String mappingConversionId = _mapping.getConversionId();
        if (mappingConversionId == null && newConversionId == null)
        {
            return false;
        }
        else if (newConversionId != null)
        {
            return !newConversionId.equals(mappingConversionId);
        }
        return !mappingConversionId.equals(newConversionId);
    }

    /**
     * Refresh schema component list for source filter
     * @param componentIdToBeSelected the schema component ID to be selected
     * @return 
     */
    String refreshSourceSchemaComponentList(String componentIdToBeSelected)
    {
        getLogger().debug("refreshSourceSchemaComponentList(): start");
        return refreshSchemaComponentFilterList(_sourceSchemaFilter, _sourceSchemaComponentFilter, componentIdToBeSelected);
    }

    /**
     * Refresh schema component list for target filter
     * @param componentIdToBeSelected the schema component ID to be selected
     * @return 
     */
    private String refreshTargetSchemaComponentList(String componentIdToBeSelected)
    {
        getLogger().debug("refreshTargetSchemaComponentList(): start");
        return refreshSchemaComponentFilterList(_targetSchemaFilter, _targetSchemaComponentFilter, componentIdToBeSelected);
    }

    /**
     * Refresh given schema component list and select first element
     * @param schemaFilter
     * @param schemaComponentFilter
     * @param schemaComponentFilterList
     */
    private String refreshSchemaComponentFilterList(ZSingleSelectionListWrapper<SchemaWrapperModel> schemaFilter, ZSingleSelectionListWrapper<SchemaComponentWrapperModel> schemaComponentFilter, String componentIdToBeSelected)
    {
        ZList<SchemaComponentWrapperModel> schemaComponentFilterList = (ZList<SchemaComponentWrapperModel>) schemaComponentFilter.getTarget();
        
        schemaComponentFilterList.removeAllElements();
        ZList<SchemaComponentWrapperModel> newElements = new ZList<SchemaComponentWrapperModel>();
        SchemaWrapperModel selectedSchemaModel = schemaFilter.getSelectedElement();
        if (selectedSchemaModel != null)
        {
            Schema selectedSchema = selectedSchemaModel.getSchema();
            List<SchemaComponent> schemaComponents = selectedSchema.getSchemaComponents();
            for (int index = 0; index < schemaComponents.size(); index++)
            {
                SchemaComponent schemaComponent = schemaComponents.get(index);
                newElements.addElement(new SchemaComponentWrapperModel(schemaComponent));
            }
        }

        if (newElements.getElementCount() > 0 && componentIdToBeSelected == null)
        {
            componentIdToBeSelected = newElements.getElementAt(0).getId();
        }
        schemaComponentFilterList.addContentsOf(newElements);

        return selectIdInDropDownlist(componentIdToBeSelected, schemaComponentFilter);
    }

    void currentMappingChanged()
    {
        _mappingSetModel.currentMappingChanged();
    }

    private static class NullConversion
        extends Conversion
    {
    }

    private class SourceSchemaItemTableSelectionListener
        implements TableSelectionListener
    {
        @Override
        public void selectionChanged(@SuppressWarnings("rawtypes") TableList source, byte type, int row, int column)
        {
            SchemaItemRef schemaRef = null;
            if (row >= 0)
            {
                SchemaItemModel schemaItemModel = _sourceSchemaItemList.getElementAt(row);
                schemaRef = schemaItemModel.getSchemaItemRef();
                if (_mapping.getSources().size() > 0)
                {
                    SchemaItemRef currentSchemaItem = _mapping.getSources().get(0);
                    if (currentSchemaItem != null && currentSchemaItem.getSchemaItemId().equals(schemaRef.getSchemaItemId()) == true)
                    {
                        schemaRef = currentSchemaItem;
                    }
                }
            }
            initSourceIndexesModel(schemaRef);
            _prevSourceItemScrollPosition.clear();
            _prevSourceItemScrollPosition.put(getSourceSchemaFilterId() + "_" + getSourceSchemaComponentFilterId(), row);
        }
    }

    private class TargetSchemaItemTableSelectionListener
        implements TableSelectionListener
    {
        @Override
        public void selectionChanged(@SuppressWarnings("rawtypes") TableList source, byte type, int row, int column)
        {
            getLogger().debug("selectionChanged(): current selected row = " + row);
            _prevTargetItemScrollPosition.clear();
            _prevTargetItemScrollPosition.put(getTargetSchemaFilterId() + "_" + getTargetSchemaComponentFilterId(), row);
        }
    }

    static abstract class DropDownlistModel
        extends ZComposite
    {
        private static final long serialVersionUID = 1L;
        private final String _id;

        public DropDownlistModel(String name, String id)
        {
            super(ZConstantString.getConstant(name));
            _id = id;
        }

        public String getId()
        {
            return _id;
        }
    }

    static class SchemaWrapperModel
        extends DropDownlistModel
    {
        private static final long serialVersionUID = 1L;
        private final Schema _schema;

        public SchemaWrapperModel(Schema schema)
        {
            super(schema.getName(), schema.getId());
            _schema = schema;
        }

        public Schema getSchema()
        {
            return _schema;
        }
    }

    static class SchemaComponentWrapperModel
        extends DropDownlistModel
    {
        private static final long serialVersionUID = 1L;
        private final SchemaComponent _schemaComponent;

        public SchemaComponentWrapperModel(SchemaComponent schemaComponent)
        {
            super(schemaComponent.getName(), schemaComponent.getId());
            _schemaComponent = schemaComponent;
        }

        public SchemaComponent getSchemaComponent()
        {
            return _schemaComponent;
        }
    }

    static class ConversionWrapperModel
        extends DropDownlistModel
    {
        private static final long serialVersionUID = 1L;
        private final Conversion _conversion;

        public ConversionWrapperModel(Conversion conversion)
        {
            super(conversion.getName(), conversion.getId());
            _conversion = conversion;
        }

        public Conversion getConversion()
        {
            return _conversion;
        }
    }
}