package org.apache.maven.scm.provider.svn.svnexe.command.changelog;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogConsumerTest
    extends PlexusTestCase
{
    Logger logger;
    SvnChangeLogConsumer consumer;
    

    protected void setUp()
        throws Exception
    {
        super.setUp();

        logger = getContainer().getLogger();
        consumer = new SvnChangeLogConsumer( new DefaultLog(), null );
    }
    
    /**
     * Initial modifications should be empty.
     */
    public void testGetModifications_Initial()
    {
        assertTrue("Initial modifications should be empty", consumer.getModifications().isEmpty());
    }
    
    /**
     * Valid svn log output should have expected values.
     * 
     * @throws Exception if any problem occurs.
     */
    public void testConsumeLine_ValidOutput()
        throws Exception
    {
        final File svnLog = getTestFile( "/src/test/resources/svn/changelog/svnLogValidOutput.txt" );

        consumeLog( svnLog );

        final ChangeSet entry = (ChangeSet) consumer.getModifications().get( 0 );

        final List changedFiles = entry.getFiles();
        final String revision = ( (ChangeFile) changedFiles.get( 0 ) ).getRevision();

        assertEquals( "Valid revision expected", "15", revision );
        assertEquals( "Valid num changed files expected", 2, changedFiles.size() );
        assertEquals( "Valid name expected", "unconventional author output (somedata)", entry.getAuthor() );
        String expectedDate = getLocalizedDate( "2002-08-26 14:33:26", TimeZone.getTimeZone( "GMT-4" ) );
        assertEquals( "Valid date expected", expectedDate, entry.getDateFormatted() );
        assertEquals( "Valid comment expected", "Minor formatting changes.\n", entry.getComment() );
    }

    private static String getLocalizedDate( String date, TimeZone timeZone )
        throws Exception
    {
        DateFormat fmt = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        fmt.setTimeZone( timeZone );
        Date parsed = fmt.parse( date );
        fmt = new SimpleDateFormat( "yyyy-MM-dd" );
        return fmt.format( parsed );
    }
    
    /**
     * Svn log output with an invalid reason should throw an IllegalOutputException.
     * 
     * @throws Exception
     */
    public void testConsumeLine_InvalidReason()
        throws Exception
    {
        final File svnLog = getTestFile( "/src/test/resources/svn/changelog/svnLogInvalidReason.txt" );

        try
        {
            consumeLog( svnLog );
            fail( "Svn log output with an invalid reason should throw IllegalOutputException" );
        }
        catch ( final IllegalOutputException e )
        {
            assertTrue( true );
        }
    }
    
    /**
     * Svn log output with an invalid date should throw an IllegalOutputException.
     * 
     * @throws Exception
     */
    public void testConsumeLine_InvalidDate()
        throws Exception
    {
        final File svnLog = getTestFile( "/src/test/resources/svn/changelog/svnLogInvalidDate.txt" );
        try
        {
            consumeLog( svnLog );
            fail( "Svn log output with an invalid date should throw IllegalOutputException" );
        }
        catch ( final IllegalOutputException e )
        {
            assertTrue( true );
        }
    }

    /**
     * Consumes change log information stored in a file.
     * 
     * @param logFile the file.
     * @throws IOException if a problem occurs.
     */
    private void consumeLog( final File logFile )
        throws IOException
    {
        final BufferedReader reader = new BufferedReader( new FileReader( logFile ) );
        String line = reader.readLine();

        while ( line != null )
        {
            consumer.consumeLine( line );
            line = reader.readLine();
        }
    }

    public void testConsumerWithPattern1()
        throws Exception
    {
        StringBuffer out = new StringBuffer();

        File f = getTestFile( "/src/test/resources/svn/changelog/svnlog.txt" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List modifications = consumer.getModifications();

        out.append( "Text format:" );

        out.append( "nb modifications : " + modifications.size() );

        for ( Iterator i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = (ChangeSet) i.next();

            out.append( "Author:" + entry.getAuthor() );

            out.append( "Date:" + entry.getDate() );

            out.append( "Comment:" + entry.getComment() );

            List files = entry.getFiles();

            for ( Iterator it = files.iterator(); it.hasNext(); )
            {
                ChangeFile file = (ChangeFile) it.next();

                out.append( "File:" + file.getName() );
            }

            out.append( "==============================" );
        }

        out.append( "XML format:" );

        out.append( "nb modifications : " + modifications.size() );

        for ( Iterator i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = (ChangeSet) i.next();

            out.append( entry.toXML() );

            out.append( "==============================" );
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( out.toString() );
        }
    }

    public void testConsumerWithPattern2()
        throws Exception
    {
        StringBuffer out = new StringBuffer();

        File f = getTestFile( "/src/test/resources/svn/changelog/svnlog2.txt" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List modifications = consumer.getModifications();

        out.append( "nb modifications : " + modifications.size() );

        for ( Iterator i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = (ChangeSet) i.next();

            out.append( "Author:" + entry.getAuthor() );

            out.append( "Date:" + entry.getDate() );

            out.append( "Comment:" + entry.getComment() );

            List files = entry.getFiles();

            for ( Iterator it = files.iterator(); it.hasNext(); )
            {
                ChangeFile file = (ChangeFile) it.next();

                out.append( "File:" + file.getName() );
            }

            out.append( "==============================" );
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( out.toString() );
        }
    }
}
