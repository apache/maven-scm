package org.apache.maven.scm.command.changelog;

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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ChangeLogFileTest extends TestCase
{
	private String baseDir;
    /**
     * @param testName
     */
    public ChangeLogFileTest(String testName)
    {
        super(testName);
    }

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		super.setUp();
		baseDir = System.getProperty("basedir");
		assertNotNull("The system property basedir was not defined.", baseDir);
	}
	
	public void testNewFile()
	{
	    ChangeLogFile f = new ChangeLogFile("test.java");
	    assertEquals("test.java", f.getName());
	    assertEquals(null, f.getRevision());
	    assertEquals("test.java", f.toString());
	}
	
	public void testNewRevisionFile()
	{
	    ChangeLogFile f = new ChangeLogFile("test.java", "revision1");
	    assertEquals("test.java", f.getName());
	    assertEquals("revision1", f.getRevision());
	    assertEquals("test.java, revision1", f.toString());
	}
}