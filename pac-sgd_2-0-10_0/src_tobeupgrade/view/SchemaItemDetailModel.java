package de.znt.pac.mapping.view;


import java.util.List;

import de.znt.ZModels.Event.ModelChangedEvent;
import de.znt.ZModels.Event.ModelChangedListener;
import de.znt.ZTypes.ZList;
import de.znt.ZTypes.ZSingleSelectionListWrapper;
import de.znt.ZTypes.ZString;
import de.znt.pac.mapping.data.SchemaItem;
import de.znt.pac.mapping.data.SchemaItemType;
import de.znt.pac.users.PacUser;
import de.znt.uiControl.UiAttribute;
import de.znt.uiManager.ModelLifecycleAware;
import de.znt.zutil.model.ZLoggableComposite;


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

public class SchemaItemDetailModel
    extends ZLoggableComposite
    implements ModelLifecycleAware, ModelChangedListener
{
    private static final long serialVersionUID = 1L;

    private final SchemaTab _schemaTab;

    private final ZString _name = new ZString();
    private final SchemaItemModel _schemaItemModel;

    private final ZSingleSelectionListWrapper<SchemaTypeModel> _schemaTypesWrapper;

    private final PacUser _user;

	private ZString _unit = new ZString();

    /**
     * @param schemaItemModel
     * @param observer
     */
    protected SchemaItemDetailModel(SchemaItemModel schemaItemModel, PacUser user, SchemaTab schemaTab)
    {
        _schemaItemModel = schemaItemModel;
        _user = user;
        _name.setStringValue(schemaItemModel.getSchemaItem().getName());
        _schemaTab = schemaTab;

        SchemaItemType type = _schemaItemModel.getSchemaItem().getType();
        
        int selectIndex = 0;
        ZList<SchemaTypeModel> schemaTypeList = new ZList<SchemaTypeModel>();
        SchemaItemType[] values = SchemaItemType.values();
        for (int index = 0; index < values.length; index++)
        {
            schemaTypeList.addElement(new SchemaTypeModel(values[index]));
            if (values[index].equals(type))
            {
                selectIndex = index;
            }
        }
        _schemaTypesWrapper = new ZSingleSelectionListWrapper<SchemaTypeModel>(schemaTypeList);
        _schemaTypesWrapper.setSelection(selectIndex);
        _unit.setStringValue(schemaItemModel.getSchemaItem().getUnit());
    }

    @UiAttribute
    public ZString getUnit()
    {
        return _unit ;
    }

    @UiAttribute
    public ZString getName()
    {
        return _name;
    }

    @UiAttribute
    public ZSingleSelectionListWrapper<SchemaTypeModel> getType()
    {
        return _schemaTypesWrapper;
    }

    @Override
    public void modelInitialize()
    {
        _name.addModelChangedListener(this);
        _unit.addModelChangedListener(this);
        _schemaTypesWrapper.addModelChangedListener(this);
    }

    @Override
    public void modelCleanup()
    {
        _name.removeModelChangedListener(this);
        _unit.removeModelChangedListener(this);
        _schemaTypesWrapper.removeModelChangedListener(this);
    }

    /**
     * @param name the name to find
     * @return found schema item, null if nothing found
     */
    private SchemaItem findExistingSchemaItem(String name)
    {
        List<SchemaItem> schemaItems = _schemaItemModel.getSchemaComponent().getSchemaItems();
        for (SchemaItem schemaItem2 : schemaItems)
        {
            if (name.equals(schemaItem2.getName()))
            {
                return schemaItem2;
            }
        }
        return null;
    }

    @Override
    public void modelChanged(ModelChangedEvent e)
    {
        if (e.getSource() == _name)
        {
            SchemaItem schemaItem = _schemaItemModel.getSchemaItem();
            String name = _name.getStringValue();

            SchemaItem existingSchemaItem = findExistingSchemaItem(name);
            if (existingSchemaItem != null && !existingSchemaItem.getId().equals(schemaItem.getId()))
            {
                _user.showErrorMessage(MappingConfigurationModel.localizedAlreadyExists(name), false, true);
                _name.setStringValue(schemaItem.getName());
            }
            else if (!name.equals(schemaItem.getName()))
            {
                _schemaItemModel.getSchemaItemNameModel().setStringValue(name);
                _schemaTab.currentSchemaItemChanged();
            }
        }
        else if (e.getSource() == _schemaTypesWrapper)
        {
            SchemaTypeModel selectedType = _schemaTypesWrapper.getSelectedElement();
            _schemaItemModel.getSchemaItemTypeModel().setStringValue(selectedType.getSchemaItemType().value());
            _schemaTab.currentSchemaItemChanged();
        }
        else if (e.getSource() == _unit)
        {
        	String unit = _unit.getStringValue();
            _schemaItemModel.getSchemaItemUnitModel().setStringValue(unit);
            _schemaTab.currentSchemaItemChanged();
        }
    }
}
// $RCSfile: SchemaItemDetailModel.java,v $
