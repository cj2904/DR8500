package sg.znt.pac.mock.matcher;


import org.easymock.IArgumentMatcher;

import de.znt.services.secs.dto.S2F41HostCommandSend;


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

public class HostCommandMatcher
    implements IArgumentMatcher
{
    private S2F41HostCommandSend _expected;

    public HostCommandMatcher(S2F41HostCommandSend expected)
    {
        _expected = expected;
    }

    /**
     * @see org.easymock.IArgumentMatcher#matches(java.lang.Object)
     **/
    @Override
    public boolean matches(Object actual)
    {
        if (!(actual instanceof S2F41HostCommandSend))
        {
            return false;
        }

        S2F41HostCommandSend actualHost = (S2F41HostCommandSend) actual;
        String expectedSml = _expected.buildMessage().getSML();

        String actualHostSml = actualHost.buildMessage().getSML();

        if (!expectedSml.equalsIgnoreCase(actualHostSml))
        {
            System.err.println("Expected host command : '" + expectedSml + "', actual: '" + actualHostSml + "'.");
            return false;
        }
        return true;
    }

    /**
     * @see org.easymock.IArgumentMatcher#appendTo(java.lang.StringBuffer)
     **/
    @Override
    public void appendTo(StringBuffer buffer)
    {
        buffer.append("HostCommand with command : ");
        buffer.append(_expected.buildMessage().getSML());
    }
}
// $RCSfile: HostCommandMatcher.java,v $
