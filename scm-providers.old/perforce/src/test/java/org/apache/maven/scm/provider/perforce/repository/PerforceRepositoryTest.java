package org.apache.maven.scm.provider.perforce.repository;

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
public class PerforceRepositoryTest extends TestCase
{
    public PerforceRepositoryTest(String name)
    {
        super(name);
    }
    
    public void setUp() throws Exception
    {
    }
    
    public void testParseConnection1()
    {
        try
        {
            PerforceRepository repo = new PerforceRepository();
            repo.setDelimiter(":");
            repo.setConnection("//depot/projects/pathname");
            assertNull(repo.getUser());
            assertNull(repo.getHost());
            assertNull(repo.getPort());
            assertEquals("//depot/projects/pathname", repo.getPath());
        }
        catch(ScmException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testParseConnection2()
    {
        try
        {
            PerforceRepository repo = new PerforceRepository();
            repo.setDelimiter(":");
            repo.setConnection("username@//depot/projects/pathname");
            repo.setPassword("myPassword");
            assertEquals("myPassword", repo.getPassword());
            assertNull(repo.getHost());
            assertNull(repo.getPort());
            assertEquals("//depot/projects/pathname", repo.getPath());
            assertEquals("username", repo.getUser());
        }
        catch(ScmException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testParseConnection3()
    {
        try
        {
            PerforceRepository repo = new PerforceRepository();
            repo.setDelimiter(":");
            repo.setConnection("host:port:username@//depot/projects/pathname");
            assertEquals("//depot/projects/pathname", repo.getPath());
            assertEquals("username", repo.getUser());
            assertEquals("host", repo.getHost());
            assertEquals("port", repo.getPort());
        }
        catch(ScmException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testParseConnection4()
    {
        try
        {
            PerforceRepository repo = new PerforceRepository();
            repo.setDelimiter(":");
            repo.setConnection("host:port://depot/projects/pathname");
            assertEquals("//depot/projects/pathname", repo.getPath());
            assertNull(repo.getUser());
            assertEquals("host", repo.getHost());
            assertEquals("port", repo.getPort());
        }
        catch(ScmException e)
        {
            fail(e.getMessage());
        }
    }
    
    public void testIncorrectConnection()
    {
        try
        {
            PerforceRepository repo = new PerforceRepository();
            repo.setDelimiter(":");
            repo.setConnection("aaa://depot/projects/pathname");
            fail();
        }
        catch(ScmException e)
        {
        }
    }
    
    public void testSetUser()
    {
        PerforceRepository repo = new PerforceRepository();
        repo.setUser("myUsername");
        assertEquals("myUsername", repo.getUser());
    }
    
    public void testSetPath()
    {
        PerforceRepository repo = new PerforceRepository();
        repo.setPath("//depot/projects/pathname");
        assertEquals("//depot/projects/pathname", repo.getPath());
    }
}
