package org.apache.maven.scm.provider.svn;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmTestCase;

/**
 * @author <a href="mailto:jerome@coffeebreaks.org">Jerome Lacoste</a>
 * @version $Id$
 */
public class SvnCommandUtilsTest
    extends ScmTestCase
{
    // ----------------------------------------------------------------------
    // appendPath
    // ----------------------------------------------------------------------

    public void testFixUrlHttpUrlsAreIgnored()
        throws Exception
    {
        String unchanged = "http://foo.com/svn/myproject/tags/foo";
        assertEquals( unchanged, SvnCommandUtils.fixUrl( unchanged, null ) );
        assertEquals( unchanged, SvnCommandUtils.fixUrl( unchanged, "" ) );
        assertEquals( unchanged, SvnCommandUtils.fixUrl( unchanged, "user" ) );
    }

    public void testFixUrlNPEifNullURL()
        throws Exception
    {
        try
        {
            SvnCommandUtils.fixUrl( null, "user" );
            fail( "expected NPE" );
        }
        catch ( NullPointerException e )
        {
            assertTrue( true ); // expected
        }
    }

    public void testFixUrlSvnSshUrlsUsernameIsAddedWhenUserSpecified()
        throws Exception
    {
        assertEquals( "svn+ssh://foo.com/svn/myproject",
                      SvnCommandUtils.fixUrl( "svn+ssh://foo.com/svn/myproject", null ) );
        assertEquals( "svn+ssh://foo.com/svn/myproject",
                      SvnCommandUtils.fixUrl( "svn+ssh://foo.com/svn/myproject", "" ) );
        assertEquals( "svn+ssh://user@foo.com/svn/myproject",
                      SvnCommandUtils.fixUrl( "svn+ssh://foo.com/svn/myproject", "user" ) );
    }

    public void testFixUrlSvnSshUrlsUsernameIsOverridenWhenUserSpecified()
        throws Exception
    {
        assertEquals( "svn+ssh://user1@foo.com/svn/myproject",
                      SvnCommandUtils.fixUrl( "svn+ssh://user1@foo.com/svn/myproject", null ) );
        assertEquals( "svn+ssh://user1@foo.com/svn/myproject",
                      SvnCommandUtils.fixUrl( "svn+ssh://user1@foo.com/svn/myproject", "" ) );
        assertEquals( "svn+ssh://user2@foo.com/svn/myproject",
                      SvnCommandUtils.fixUrl( "svn+ssh://user1@foo.com/svn/myproject", "user2" ) );
    }
}
