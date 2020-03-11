package de.znt.pac.mapping.view;


import static de.znt.ZTypes.ZConstantString.getConstant;
import de.znt.ZTypes.ZComplexType;
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

public class SchemaTypeModel
    extends ZComplexType
{

    private static final long serialVersionUID = 1L;
    private SchemaItemType _schemaType;

    /**
     * @param conversion    
     */
    public SchemaTypeModel(SchemaItemType schemaType)
    {
        super(getConstant(schemaType.name()));
        _schemaType = schemaType;
    }

    protected SchemaItemType getSchemaItemType()
    {
        return _schemaType;
    }

}
// $RCSfile: SchemaTypeModel.java,v $
