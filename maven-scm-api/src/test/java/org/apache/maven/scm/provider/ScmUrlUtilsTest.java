package org.apache.maven.scm.provider;

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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:dennisl@apache.org">Dennis Lundberg</a>
 *
 */
public class ScmUrlUtilsTest
    extends TestCase
{
    private static final String SCM_URL_CVS_COLON = "scm:cvs:local:repository:module";

    private static final String SCM_URL_CVS_PIPE = "scm:cvs|local|repository|module";

    private static final String SCM_URL_CVS_INVALID = "scm|cvs|local|repository|module";

    private static final String SCM_URL_INVALID_1 = null;

    private static final String SCM_URL_INVALID_2 = "scm";

    private static final String SCM_URL_INVALID_3 = "scm:a";

    private static final String SCM_URL_INVALID_4 = "scm:a-";

    private static final String SCM_URL_VALID_1 = "scm:a:";

    private static final String SCM_URL_VALID_2 = "scm:a|";

    private static final String SCM_URL_VALID_3 = "scm:a:provider-specific-part";

    private static final String SCM_URL_VALID_4 = "scm:a|provider-specific-part";

    public void testGetDelimiter()
        throws Exception
    {
        assertEquals( ":", ScmUrlUtils.getDelimiter( SCM_URL_CVS_COLON ) );
        assertEquals( "|", ScmUrlUtils.getDelimiter( SCM_URL_CVS_PIPE ) );
        assertEquals( "|", ScmUrlUtils.getDelimiter( SCM_URL_CVS_INVALID ) );
    }

    public void testGetProvider()
        throws Exception
    {
        assertEquals( "cvs", ScmUrlUtils.getProvider( SCM_URL_CVS_COLON ) );
        assertEquals( "cvs", ScmUrlUtils.getProvider( SCM_URL_CVS_PIPE ) );
        assertEquals( "cvs", ScmUrlUtils.getProvider( SCM_URL_CVS_INVALID ) );

        assertEquals( "a", ScmUrlUtils.getProvider( SCM_URL_VALID_1 ) );
        assertEquals( "a", ScmUrlUtils.getProvider( SCM_URL_VALID_2 ) );
        assertEquals( "a", ScmUrlUtils.getProvider( SCM_URL_VALID_3 ) );
        assertEquals( "a", ScmUrlUtils.getProvider( SCM_URL_VALID_4 ) );
    }

    public void testGetProviderSpecificPart()
        throws Exception
    {
        assertEquals( "local:repository:module", ScmUrlUtils.getProviderSpecificPart( SCM_URL_CVS_COLON ) );
        assertEquals( "local|repository|module", ScmUrlUtils.getProviderSpecificPart( SCM_URL_CVS_PIPE ) );
        assertEquals( "local|repository|module", ScmUrlUtils.getProviderSpecificPart( SCM_URL_CVS_INVALID ) );

        assertEquals( "", ScmUrlUtils.getProviderSpecificPart( SCM_URL_VALID_1 ) );
        assertEquals( "", ScmUrlUtils.getProviderSpecificPart( SCM_URL_VALID_2 ) );
        assertEquals( "provider-specific-part", ScmUrlUtils.getProviderSpecificPart( SCM_URL_VALID_3 ) );
        assertEquals( "provider-specific-part", ScmUrlUtils.getProviderSpecificPart( SCM_URL_VALID_4 ) );
    }

    public void testIsValid()
        throws Exception
    {
        assertTrue( ScmUrlUtils.isValid( SCM_URL_CVS_COLON ) );
        assertTrue( ScmUrlUtils.isValid( SCM_URL_CVS_PIPE ) );
        assertFalse( ScmUrlUtils.isValid( SCM_URL_CVS_INVALID ) );

        assertTrue( ScmUrlUtils.isValid( SCM_URL_VALID_1 ) );
        assertTrue( ScmUrlUtils.isValid( SCM_URL_VALID_2 ) );
        assertTrue( ScmUrlUtils.isValid( SCM_URL_VALID_3 ) );
        assertFalse( ScmUrlUtils.isValid( SCM_URL_INVALID_1 ) );
        assertFalse( ScmUrlUtils.isValid( SCM_URL_INVALID_2 ) );
        assertFalse( ScmUrlUtils.isValid( SCM_URL_INVALID_3 ) );
        assertFalse( ScmUrlUtils.isValid( SCM_URL_INVALID_4 ) );
    }
}
