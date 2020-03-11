package de.znt.pac.mapping.view;


import static de.znt.ZTypes.ZConstantString.getConstant;
import de.znt.ZModels.ZHomogeneousListModel;
import de.znt.ZModels.ZModel;
import de.znt.ZStandardDialogs.tablelist.AbstractTableSetting;
import de.znt.ZStandardDialogs.tablelist.TableColumnSetting;
import de.znt.ZStandardDialogs.tablelist.TableLineWrapper;


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

public class SchemaItemReducedTableLineWrapper
    extends AbstractTableSetting
    implements TableLineWrapper, TableColumnSetting
{

    @Override
    public ZModel getHeader(int columnIndex)
    {
        switch (columnIndex)
        {
        case 0:
            return getConstant(SchemaItemModel.localizedAttributeName());

        case 1:
            return getConstant(SchemaItemModel.localizedAttributeType());
        case 2:
            return getConstant(SchemaItemModel.localizedAttributeUnit());
        }
        return null;
    }

    @Override
    public boolean isHideable(int columnIndex)
    {
        return true;
    }

    @Override
    public boolean isEditable(int columnIndex)
    {
        return false;
    }

    @Override
    public byte getHeaderSize(int columnIndex)
    {
        switch (columnIndex)
        {
        case 0:
            return 25;

        case 1:
            return 25;
        case 2:
            return 25;
        }
        return 0;
    }

    @Override
    public int getHeaderPixelSize(int columnIndex)
    {
        return 0;
    }

    @Override
    public boolean isHandlingColumnChanges(int columnIndex)
    {
        return true;
    }

    @Override
    public int getElementCount()
    {
        return 3;
    }

    /**
     * @see de.znt.ZStandardDialogs.tablelist.TableLineWrapper#getElementAt(de.znt.ZModels.ZModel, int, int)
     */
    @Override
    public ZModel getElementAt(ZModel element, int pos, int row)
    {
        SchemaItemModel model = (SchemaItemModel) element;
        switch (pos)
        {
        case 0:
            return model.getSchemaItemNameModel();

        case 1:
            return model.getSchemaItemTypeModel();
        case 2:
            return model.getSchemaItemUnitModel();
        }
        return null;
    }
    
    @Override
    public byte getSelectionMode()
    {
        return ZHomogeneousListModel.ROW_SELECTION;
    }

    @Override
    public boolean isScrollingToSelection()
    {
        return true;
    }
}
// $RCSfile: SchemaItemReducedTableLineWrapper.java,v $
