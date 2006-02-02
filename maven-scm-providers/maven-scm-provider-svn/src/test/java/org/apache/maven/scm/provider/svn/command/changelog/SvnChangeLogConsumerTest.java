package org.apache.maven.scm.provider.svn.command.changelog;

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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogConsumerTest
    extends PlexusTestCase
{
    public void testConsumerWithPattern1()
        throws Exception
    {
        SvnChangeLogConsumer consumer = new SvnChangeLogConsumer( new DefaultLog(), null );

        File f = getTestFile( "/src/test/resources/svn/changelog/svnlog.txt" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List modifications = consumer.getModifications();

        System.out.println( "Text format:" );

        System.out.println( "nb modifications : " + modifications.size() );

        for ( Iterator i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = (ChangeSet) i.next();

            System.out.println( "Author:" + entry.getAuthor() );

            System.out.println( "Date:" + entry.getDate() );

            System.out.println( "Comment:" + entry.getComment() );

            List files = entry.getFiles();

            for ( Iterator it = files.iterator(); it.hasNext(); )
            {
                ChangeFile file = (ChangeFile) it.next();

                System.out.println( "File:" + file.getName() );
            }

            System.out.println( "==============================" );
        }

        System.out.println( "XML format:" );

        System.out.println( "nb modifications : " + modifications.size() );

        for ( Iterator i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = (ChangeSet) i.next();

            System.out.println( entry.toXML() );

            System.out.println( "==============================" );
        }
    }

    public void testConsumerWithPattern2()
        throws Exception
    {
        SvnChangeLogConsumer consumer = new SvnChangeLogConsumer( new DefaultLog(), null );

        File f = getTestFile( "/src/test/resources/svn/changelog/svnlog2.txt" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List modifications = consumer.getModifications();

        System.out.println( "nb modifications : " + modifications.size() );

        for ( Iterator i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = (ChangeSet) i.next();

            System.out.println( "Author:" + entry.getAuthor() );

            System.out.println( "Date:" + entry.getDate() );

            System.out.println( "Comment:" + entry.getComment() );

            List files = entry.getFiles();

            for ( Iterator it = files.iterator(); it.hasNext(); )
            {
                ChangeFile file = (ChangeFile) it.next();

                System.out.println( "File:" + file.getName() );
            }

            System.out.println( "==============================" );
        }
    }
}