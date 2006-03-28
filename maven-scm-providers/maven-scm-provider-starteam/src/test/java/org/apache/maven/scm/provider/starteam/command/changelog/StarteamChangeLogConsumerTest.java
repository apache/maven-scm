package org.apache.maven.scm.provider.starteam.command.changelog;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
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

    private List parseTestFile()
        throws Exception
    {
        /* must match the working directory in the text test file */

        File basedir = new File( "C:/Test" );

        FileInputStream fis = new FileInputStream( testFile );

        BufferedReader in = new BufferedReader( new InputStreamReader( fis ) );

        String s = in.readLine();

        StarteamChangeLogConsumer consumer =
            new StarteamChangeLogConsumer( basedir, new DefaultLog(), null, null, null );

        while ( s != null )
        {
            consumer.consumeLine( s );

            s = in.readLine();
        }

        return consumer.getModifications();
    }

    public void testNumberOfModifications()
        throws Exception
    {
        List entries = parseTestFile();

        assertEquals( "Wrong number of entries returned", 6, entries.size() );

        ChangeSet entry = null;

        for ( Iterator i = entries.iterator(); i.hasNext(); )
        {
            entry = (ChangeSet) i.next();

            assertTrue( "ChangeLogEntry erroneously picked up",
                        entry.toString().indexOf( "ChangeLogEntry.java" ) == -1 );
        }
    }

    public void testRelativeFilePath()
        throws Exception
    {
        List entries = parseTestFile();

        //ensure the filename in the first ChangeSet has correct relative path
        ChangeSet entry = (ChangeSet) entries.get( 1 );

        assertTrue( entry.containsFilename( "./maven/src/File2.java", null ) );
    }


}
