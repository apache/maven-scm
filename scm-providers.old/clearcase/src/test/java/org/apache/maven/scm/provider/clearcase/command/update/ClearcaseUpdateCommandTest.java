package org.apache.maven.scm.provider.clearcase.command.update;

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
import org.apache.maven.scm.provider.clearcase.repository.ClearcaseRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ClearcaseUpdateCommandTest extends TestCase
{
    public ClearcaseUpdateCommandTest(String testName)
    {
        super(testName);
    }
    
    public void testGetDisplayNameName()
    {
        try
        {
            ClearcaseUpdateCommand cmd = new ClearcaseUpdateCommand();
            assertEquals("Update", cmd.getDisplayName());
        }
        catch(Exception e)
        {
            fail();
        }
    }
    
    public void testGetName()
    {
        try
        {
            ClearcaseUpdateCommand cmd = new ClearcaseUpdateCommand();
            assertEquals("update", cmd.getName());
        }
        catch(Exception e)
        {
            fail();
        }
    }
}