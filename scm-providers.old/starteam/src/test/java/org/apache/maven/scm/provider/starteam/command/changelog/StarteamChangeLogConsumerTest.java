package org.apache.maven.scm.provider.starteam.command.changelog;

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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import junit.framework.TestCase;

import org.apache.maven.scm.command.changelog.ChangeLogEntry;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamChangeLogConsumerTest extends TestCase
{
	/** the {@link StarteamChangeLogConsumer} used for testing */
	private StarteamChangeLogConsumer instance;
	/** file with test results to check against */
	private String testFile;

	/**
	 * Create a test with the given name
	 * @param testName the name of the test
	 */
	public StarteamChangeLogConsumerTest(String testName)
	{
		super(testName);
	}

	/**
	 * Initialize per test data
	 * @throws Exception when there is an unexpected problem
	 */
	public void setUp() throws Exception
	{
		String baseDir = System.getProperty("basedir");

		assertNotNull("The system property basedir was not defined.", baseDir);

        String language = Locale.getDefault().getLanguage();
        
		testFile = baseDir + "/src/test/resources/starteam/changelog/starteamlog_" + language + ".txt";

		instance = new StarteamChangeLogConsumer();
	}

	/**
	 * Test of parse method
	 * @throws Exception when there is an unexpected problem
	 */
	public void testParse() throws Exception
	{
		FileInputStream fis = new FileInputStream(testFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));
		String s = in.readLine();
		while ( s != null )
		{
			instance.consumeLine( s );
			s = in.readLine();
		}

		Collection entries = instance.getModifications();
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
