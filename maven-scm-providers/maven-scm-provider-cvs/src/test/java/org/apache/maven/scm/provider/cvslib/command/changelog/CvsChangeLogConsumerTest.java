package org.apache.maven.scm.provider.cvslib.command.changelog;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import org.apache.maven.scm.command.changelog.ChangeLogEntry;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsChangeLogConsumerTest
    extends AbstractCvsScmTest
{
    /** file with test results to check against */
	private File testFile;

	/**
	 * Initialize per test data
	 * @throws Exception when there is an unexpected problem
	 */
	public void setUp() throws Exception
	{
        super.setUp();

        testFile = getTestFile( "/src/test/resources/cvslib/changelog/cvslog.txt" );
	}

	/**
	 * Test of parse method
	 * @throws Exception when there is an unexpected problem
	 */
	public void testParse()
        throws Exception
	{
        CvsChangeLogConsumer command = new CvsChangeLogConsumer( null );

        FileInputStream fis = new FileInputStream( testFile );
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));
		String s = in.readLine();
		while ( s != null )
		{
			command.consumeLine( s );
			s = in.readLine();
		}

		Collection entries = command.getModifications();
		assertEquals("Wrong number of entries returned", 3, entries.size());
		ChangeLogEntry entry = null;
		for (Iterator i = entries.iterator(); i.hasNext(); )
		{
			entry = (ChangeLogEntry) i.next();
			assertTrue("ChangeLogEntry erroneously picked up",
				entry.toString().indexOf("ChangeLogEntry.java") == -1);
		}
	}
}
