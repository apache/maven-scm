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

public class RepositoryTest extends TestCase
{
    private TestRepository repo;
    /**
     * @param testName
     */
    public RepositoryTest(final String testName)
    {
        super(testName);
    }

    /*
     * @see TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        super.setUp();
        repo = new TestRepository();
    }
    
    public void testSetConnectionWithoutDelimiter()
    {
        try
        {
            repo.setConnection("myConnection");
            fail();
        }
        catch(Exception e)
        {
        }
    }
    
    public void testSetConnectionWithDelimiter()
    {
        try
        {
            repo.setDelimiter(":");
            assertEquals(":", repo.getDelimiter());
            repo.setConnection("my:connection");
            assertEquals("my:connection", repo.getConnection());
        }
        catch(Exception e)
        {
            fail(e.getMessage());
        }
    }
}
