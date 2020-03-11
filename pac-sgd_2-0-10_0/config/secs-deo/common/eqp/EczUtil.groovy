// Copyright (c) 2014, By ZNT GmbH.  All Rights Reserved.
//*********************************************************************
//
//                     ZNT GmbH
//                     Mautnerstra�e 268
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

package eqp

import de.znt.zsecs.composite.SecsAsciiItem
import de.znt.zsecs.composite.SecsBinary
import de.znt.zsecs.composite.SecsBoolean
import de.znt.zsecs.composite.SecsComponent
import de.znt.zsecs.composite.SecsComposite
import de.znt.zsecs.composite.SecsF4Item
import de.znt.zsecs.composite.SecsF8Item
import de.znt.zsecs.composite.SecsI1Item
import de.znt.zsecs.composite.SecsI2Item
import de.znt.zsecs.composite.SecsI4Item
import de.znt.zsecs.composite.SecsU1Item
import de.znt.zsecs.composite.SecsU2Item
import de.znt.zsecs.composite.SecsU4Item
import de.znt.zsecs.composite.SecsU8Item

public class EczUtil
{
    public static String getSecsDataType(SecsComponent dataType)
    {
        if (dataType instanceof SecsAsciiItem)
        {
            return "A"
        }
        else if (dataType instanceof SecsBinary)
        {
            return "Binary"
        }
        else if (dataType instanceof SecsBoolean)
        {
            return "Boolean"
        }
        else if (dataType instanceof SecsU1Item)
        {
            return "U1"
        }
        else if (dataType instanceof SecsU2Item)
        {
            return "U2"
        }
        else if (dataType instanceof SecsU4Item)
        {
            return "U4"
        }
        else if (dataType instanceof SecsU8Item)
        {
            return "U8"
        }
        else if (dataType instanceof SecsF4Item)
        {
            return "F4"
        }
        else if (dataType instanceof SecsF8Item)
        {
            return "F8"
        }
        else if (dataType instanceof SecsI1Item)
        {
            return "I1"
        }
        else if (dataType instanceof SecsI2Item)
        {
            return "I2"
        }
        else if (dataType instanceof SecsI4Item)
        {
            return "I4"
        }
        else if (dataType instanceof SecsComposite)
        {
            return "L[](U4)"
        }
        return dataType.getType()
    }

    public static String removeXmlKeywork(String value)
    {
        def val = value.replaceAll("&|\"|<|>", "_")
        return val.replaceAll(" ", "")         
    }
    
    public static String getVariableData(SecsComponent<?> data)
    {
        String statusReply = ""
        List<SecsComponent> valueList = data.getValueList()
        for (var in valueList)
        {
            if(statusReply.length()>0)
            {
                statusReply = statusReply + ","
            }
            if (var instanceof SecsComposite)
            {
                statusReply = statusReply + EczUtil.getVariableData(var);
            }
            else if (var instanceof SecsComponent<?>)
            {
                def vl = var.getValueList()
                for (nv in vl)
                {
                    statusReply = statusReply + nv;
                }
            }
            else
            {
                statusReply = statusReply + var;
            }
        }
        return statusReply;
    }
}
