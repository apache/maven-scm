package org.apache.maven.scm.provider.cvslib.repository;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

import junit.framework.TestCase;

import org.apache.maven.scm.ScmException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsRepositoryTest extends TestCase
{
    public void testParseRemotePserverConnection()
        throws Exception
    {
	    CvsRepository repo = new CvsRepository();

        repo.setDelimiter( ":" );

        repo.setConnection( "pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven" );

        assertEquals( "pserver", repo.getSubType() );

        assertEquals( "anoncvs", repo.getUser() );

        assertEquals( "cvs.apache.org", repo.getHost() );

        assertEquals( "/home/cvspublic", repo.getPath() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:/home/cvspublic", repo.getCvsRoot() );

        repo = new CvsRepository();

        repo.setDelimiter( ":" );

        try
        {
            repo.setConnection( "pserver:cvs.apache.org:/home/cvspublic:maven" );

            fail( "Expected ScmException." );
        }
        catch( ScmException ex )
        {
            // ignore
        }
    }

    public void testParseLocalConnection()
        throws Exception
    {
        CvsRepository repo = new CvsRepository();

        repo.setDelimiter( ":" );

        repo.setConnection( "local:ignored:/home/cvspublic:maven" );

        assertEquals( "local", repo.getSubType() );

        assertNull( repo.getUser() );

        assertNull( repo.getHost() );

        assertEquals("/home/cvspublic", repo.getPath() );

        assertEquals("/home/cvspublic", repo.getCvsRoot() );
    }
}
