package org.apache.maven.scm.provider.svn.command.changelog;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.maven.scm.command.changelog.ChangeLogEntry;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogConsumerTest extends TestCase
{
	/** Date formatter */
	private static final SimpleDateFormat DATE =
		new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US);
		
	/** the {@link SvnChangeLogConsumer} used for testing */
	private SvnChangeLogConsumer instance;
	/** file with test results to check against */
	private String testFile;

    /**
     * @param testName
     */
    public SvnChangeLogConsumerTest(String testName)
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
		testFile = baseDir + "/src/test/resources/svn/changelog/svnlog.txt";
		instance = new SvnChangeLogConsumer();
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

		List entries = new ArrayList(instance.getModifications());
		assertEquals("Wrong number of entries returned", 12, entries.size());
		ChangeLogEntry entry = null;
		for (Iterator i = entries.iterator(); i.hasNext(); )
		{
			entry = (ChangeLogEntry) i.next();
			assertTrue("ChangeLogEntry erroneously picked up",
				entry.toString().indexOf("ChangeLogEntry.java") == -1);
		}
		
		entry = (ChangeLogEntry) entries.get(0);
		assertEquals("Entry 0 was parsed incorrectly", 
				"kaz\n" +
				DATE.parse("Mon Aug 26 14:33:26 EDT 2002") + "\n" +
				"[/poolserver/trunk/build.xml, 15, " +
				"/poolserver/trunk/project.properties, 15]\n" +
				"Minor formatting changes.\n\n",
				entry.toString());

		entry = (ChangeLogEntry) entries.get(6);
		assertEquals("Entry 6 was parsed incorrectly", 
				"kaz\n" +
				DATE.parse("Fri Aug 23 11:11:52 EDT 2002") + "\n" +
				"[/poolserver/trunk/build.xml, 9]\n" +
				"Testing script out again ...\n\n",
				entry.toString());

		entry = (ChangeLogEntry) entries.get(8);
		assertEquals("Entry 8 was parsed incorrectly",
				"pete\n" +
				DATE.parse("Fri Aug 23 11:03:39 EDT 2002") + "\n" +
				"[/poolserver/trunk/build.xml, 7]\n" +
				"Reformatted the indentation (really just an excuse to test out\n" +
				"subversion).\n\n",
				entry.toString());
	}
}
