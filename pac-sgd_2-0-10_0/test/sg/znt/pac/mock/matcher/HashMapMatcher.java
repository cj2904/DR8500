package sg.znt.pac.mock.matcher;


import static org.easymock.EasyMock.reportMatcher;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.easymock.IArgumentMatcher;


// Copyright (c) 2013, By ZNT AG.  All Rights Reserved.
// *********************************************************************
//
// ZNT AG
// Mautnerstraﬂe 268
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
// ZNT AG assumes no responsibility for the use or reliability of
// software which has been modified without approval.
// *********************************************************************
// Description: 
// Author:    Liat Su

public class HashMapMatcher
    implements IArgumentMatcher
{
    private HashMap<String, String> _expected;

    public HashMapMatcher(HashMap<String, String> expected)
    {
        _expected = expected;
    }

    /**
     * @see org.easymock.IArgumentMatcher#matches(java.lang.Object)
     **/
    @Override
    public boolean matches(Object actual)
    {
        if (!(actual instanceof HashMap))
        {
            return false;
        }

        HashMap<String, String> actualHashMap = (HashMap<String, String>) actual;
        Set<Entry<String, String>> entrySet = _expected.entrySet();
        for (Entry<String, String> entry : entrySet)
        {
            Object expectValue = entry.getValue();
            Object actualValue = actualHashMap.get(entry.getKey());
            if (!expectValue.toString().equalsIgnoreCase(actualValue.toString()))
            {
                System.err.println("Expected value : '" + expectValue + "', actual: '" + actualValue + "' for key '" + entry.getKey() + ".");
                return false;
            }
        }
        return true;
    }

    /**
     * @see org.easymock.IArgumentMatcher#appendTo(java.lang.StringBuffer)
     **/
    @Override
    public void appendTo(StringBuffer buffer)
    {
        buffer.append("HashMap with value : ");
        buffer.append(_expected.toString());
    }
    
    public static HashMap<String, String> compareHashMap(HashMap<String, String> runtimeParameters)
    {
        reportMatcher(new HashMapMatcher(runtimeParameters));
        return null;
    }
}
// $RCSfile: HashMapMatcher.java,v $
