package de.znt.pac.mapping.view;

import static de.znt.uiManager.SessionHelper.getLocalizedString;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

import de.znt.ZModels.Event.ModelChangedEvent;
import de.znt.ZModels.Event.ModelChangedListener;
import de.znt.ZTypes.ZBoolean;
import de.znt.ZTypes.ZComplexType;
import de.znt.ZTypes.ZString;
import de.znt.pac.mapping.data.Schema;
import de.znt.pac.mapping.data.SchemaComponent;
import de.znt.pac.mapping.data.SchemaItem;
import de.znt.pac.mapping.data.SchemaItemRef;
import de.znt.pac.mapping.data.SchemaItemType;


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

public class SchemaItemModel
    extends ZComplexType
{
    private static final long serialVersionUID = 1L;
    private final SchemaItem _schemaItem;
    private final Schema _schema;
    private final SchemaComponent _schemaComponent;
    private final ZBoolean _selected = new ZBoolean(false);
    private SchemaItemRef _schemaItemRef = null;
    private final ZString _schemaItemNameModel;
    private final ZString _schemaItemTypeModel;
    private final ZString _schemaItemUnitModel;

    /**
     * Constructor
     * @param schema the schema
     * @param schemaComponent the schema component
     * @param schemaItem the schema item
     */
    public SchemaItemModel(Schema schema, SchemaComponent schemaComponent, SchemaItem schemaItem)
    {
        _schema = schema;
        _schemaComponent = schemaComponent;
        _schemaItem = schemaItem;
        _schemaItemRef = new SchemaItemRef(createIndezesListForName(), _schema.getId(), _schemaComponent.getId(), _schemaItem.getId());
        _schemaItemNameModel = new ZString(schemaItem.getName());
        SchemaItemType type = schemaItem.getType();
        String typeString = "";
        if (type != null)
        {
            typeString = type.name();
        }
        _schemaItemTypeModel = new ZString(typeString);
        _schemaItemUnitModel = new ZString(schemaItem.getUnit());
        _schemaItemNameModel.addModelChangedListener(new NameChangedListener());
        _schemaItemTypeModel.addModelChangedListener(new TypeChangedListener());
        _schemaItemUnitModel.addModelChangedListener(new UnitChangedListener());
    }

    /**
     * @return
     */
    public SchemaItem getSchemaItem()
    {
        return _schemaItem;
    }

    public SchemaItemRef getSchemaItemRef()
    {
        return _schemaItemRef;
    }

    protected Schema getSchema()
    {
        return _schema;
    }

    protected SchemaComponent getSchemaComponent()
    {
        return _schemaComponent;
    }
    
    public static String localizedAttributeID()
    {
        return getLocalizedString(SchemaItemModel.class, "SchemaItemModel.Attribute.ID");
    }

    public static String localizedAttributeName()
    {
        return getLocalizedString(SchemaItemModel.class, "SchemaItemModel.Attribute.Name");
    }
    
    public static String localizedAttributeSchema()
    {
        return getLocalizedString(SchemaItemModel.class, "SchemaItemModel.Attribute.Schema");
    }
    
    public static String localizedAttributeSchemaComponent()
    {
        return getLocalizedString(SchemaItemModel.class, "SchemaItemModel.Attribute.SchemaComponent");
    }
    
    public static String localizedAttributeType()
    {
        return getLocalizedString(SchemaItemModel.class, "SchemaItemModel.Attribute.Type");
    }
    
    public static String localizedAttributeUnit()
    {
        return getLocalizedString(SchemaItemModel.class, "SchemaItemModel.Attribute.Unit");
    }

    public ZBoolean getSelected()
    {
        return _selected;
    }

    List<Integer> createIndezesListForName()
    {
        String schemaItemName = _schemaItem.getName();
        int size = StringUtils.countOccurrencesOf(schemaItemName, ".#");
        if (size > 0)
        {
            ArrayList<Integer> indezes = new ArrayList<Integer>();
            for (int i = 0; i < size; i++)
            {
                indezes.add(0);
            }
            return indezes;
        }
        return null;
    }

    /**
     * @return the model for the schema item name
     */
    public ZString getSchemaItemNameModel()
    {
        return _schemaItemNameModel;
    }

    /**
     * @return the model for the schema item type
     */
    public ZString getSchemaItemTypeModel()
    {
        return _schemaItemTypeModel;
    }

    public ZString getSchemaItemUnitModel()
	{
		return _schemaItemUnitModel;
	}

	/**
     * Handle changes of name
     */
    private class NameChangedListener
        implements ModelChangedListener
    {
        @Override
        public void modelChanged(ModelChangedEvent e)
        {
            _schemaItem.setName(_schemaItemNameModel.getStringValue());
        }
    }


    /**
     * handle changes of type
     */
    private class TypeChangedListener
        implements ModelChangedListener
    {
        @Override
        public void modelChanged(ModelChangedEvent e)
        {
            SchemaItemType newType = SchemaItemType.fromValue(_schemaItemTypeModel.getStringValue());
            _schemaItem.setType(newType);
        }
    }
    
    /**
     * handle changes of type
     */
    private class UnitChangedListener
        implements ModelChangedListener
    {
        @Override
        public void modelChanged(ModelChangedEvent e)
        {
            _schemaItem.setUnit(_schemaItemUnitModel.getStringValue());
        }
    }

}
// $RCSfile: SchemaItemModel.java,v $
