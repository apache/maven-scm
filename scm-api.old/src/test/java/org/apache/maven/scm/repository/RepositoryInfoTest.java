package org.apache.maven.scm.repository;

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

public class RepositoryInfoTest extends TestCase
{
    private RepositoryInfo repoInfo;
    /**
     * @param testName
     */
    public RepositoryInfoTest( final String testName )
    {
        super( testName );
    }

    /*
     * @see TestCase#setUp()
     */
    public void setUp(  ) throws Exception
    {
        super.setUp(  );
        repoInfo = new RepositoryInfo();
    }
    
    public void testCheckConnectionWithNullUrl()
    {
        try
        {
            repoInfo.setUrl(null);
            fail("Exception must be thrown");
        }
        catch ( Exception e )
        {
        }
    }
    
    public void testCheckConnectionWithIncorrectUrlStarter()
    {
        try
        {
            repoInfo.setUrl("badscmurl");
            fail("Exception must be thrown");
        }
        catch ( Exception e )
        {
        }
    }
    
    public void testCheckConnectionWithTooShortUrl()
    {
        try
        {
            repoInfo.setUrl("scm");
            fail("Exception must be thrown for url = scm");
        }
        catch ( Exception e )
        {
        }
        try
        {
            repoInfo.setUrl("scm:cvs");
            fail("Exception must be thrown for url = scm:cvs");
        }
        catch ( Exception e )
        {
        }
    }
    
    public void testCheckConnection()
    {
        try
        {
            repoInfo.setUrl("scm:cvs:an_url");
            assertEquals("scm:cvs:an_url", repoInfo.getUrl());
            assertEquals(":", repoInfo.getDelimiter());
            assertEquals("cvs", repoInfo.getType());
            assertEquals("an_url", repoInfo.getConnection());
        }
        catch ( Exception e )
        {
            fail(e.getMessage());
        }
    }
    
    public void testCheckPassword()
    {
        repoInfo.setPassword("mypassword");
        assertEquals("mypassword", repoInfo.getPassword());
    }
    
}
