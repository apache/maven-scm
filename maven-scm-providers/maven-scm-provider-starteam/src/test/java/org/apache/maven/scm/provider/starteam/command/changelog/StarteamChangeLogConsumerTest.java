package org.apache.maven.scm.provider.starteam.command.changelog;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.util.ConsumerUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class StarteamChangeLogConsumerTest
    extends ScmTestCase
{
    private File testFile;

    public void setUp()
        throws Exception
    {
        super.setUp();

        String language = Locale.getDefault().getLanguage();

        testFile = getTestFile( "/src/test/resources/starteam/changelog/starteamlog_" + language + ".txt" );

        if ( !testFile.exists() )
        {
            testFile = getTestFile( "/src/test/resources/starteam/changelog/starteamlog_en.txt" );
        }
    }

    private List<ChangeSet> parseTestFile()
        throws Exception
    {
        /* must match the working directory in the text test file */

        File basedir = new File( "C:/Test" );

        StarteamChangeLogConsumer consumer =
            new StarteamChangeLogConsumer( basedir, new DefaultLog(), null, null, null );

        ConsumerUtils.consumeFile( testFile, consumer );

        return consumer.getModifications();
    }

    public void testNumberOfModifications()
        throws Exception
    {
        List<ChangeSet> entries = parseTestFile();

        assertEquals( "Wrong number of entries returned", 6, entries.size() );

        for ( Iterator<ChangeSet> i = entries.iterator(); i.hasNext(); )
        {
            assertTrue( "ChangeLogEntry erroneously picked up",
                        i.next().toString().indexOf( "ChangeLogEntry.java" ) == -1 );
        }
    }

    public void testRelativeFilePath()
        throws Exception
    {
        List<ChangeSet> entries = parseTestFile();

        // ensure the filename in the first ChangeSet has correct relative path
        ChangeSet entry = (ChangeSet) entries.get( 1 );

        assertTrue( entry.containsFilename( "./maven/src/File2.java" ));
    }

    public void testLocales()
        throws Exception
    {
        Locale currentLocale = Locale.getDefault();

        Locale[] availableLocales = Locale.getAvailableLocales();
        try
        {
            for ( int i = 0; i < availableLocales.length; i++ )
            {
                Locale.setDefault( availableLocales[i] );

                String language = availableLocales[i].getLanguage();

                testFile = getTestFile( "/src/test/resources/starteam/changelog/starteamlog_" + language + ".txt" );

                if ( !testFile.exists() )
                {
                    testFile = getTestFile( "/src/test/resources/starteam/changelog/starteamlog_en.txt" );
                }

                parseTestFile();
            }
        }
        finally
        {
            Locale.setDefault( currentLocale );
        }
    }
}
