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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.util.ConsumerUtils;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.logging.Logger;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
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
        assertTrue( "Initial modifications should be empty", consumer.getModifications().isEmpty() );
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

        final ChangeSet entry = consumer.getModifications().get( 0 );

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
        ConsumerUtils.consumeFile( logFile, consumer );
    }

    public void testConsumerWithPattern1()
        throws Exception
    {
        StringBuilder out = new StringBuilder();

        File f = getTestFile( "/src/test/resources/svn/changelog/svnlog.txt" );

        ConsumerUtils.consumeFile( f, consumer );

        List<ChangeSet> modifications = consumer.getModifications();

        out.append( "Text format:" );

        out.append( "nb modifications : " + modifications.size() );

        for ( ChangeSet entry : modifications )
        {

            out.append( "Author:" + entry.getAuthor() );

            out.append( "Date:" + entry.getDate() );

            out.append( "Comment:" + entry.getComment() );

            for ( ChangeFile file : entry.getFiles() )
            {

                out.append( "File:" + file.getName() );
            }

            out.append( "==============================" );
        }

        out.append( "XML format:" );

        out.append( "nb modifications : " + modifications.size() );

        for ( ChangeSet entry : modifications )
        {
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
        StringBuilder out = new StringBuilder();

        File f = getTestFile( "/src/test/resources/svn/changelog/svnlog2.txt" );

        ConsumerUtils.consumeFile( f, consumer );

        List modifications = consumer.getModifications();

        out.append( "nb modifications : " + modifications.size() );

        int origFileCounter = 0;

        // must use *Linked* HashMap to have predictable toString
        final Map<ScmFileStatus, AtomicInteger> summary = new LinkedHashMap<ScmFileStatus, AtomicInteger>();

        for ( ChangeSet entry : consumer.getModifications() )
        {

            out.append( "Author:" + entry.getAuthor() );

            out.append( "Date:" + entry.getDate() );

            out.append( "Comment:" + entry.getComment() );

            for ( ChangeFile file : entry.getFiles() )
            {
                final ScmFileStatus action = file.getAction();
                if ( !summary.containsKey( action ) )
                {
                    summary.put( action, new AtomicInteger() );
                }
                summary.get( action ).incrementAndGet();

                final String fileName = file.getName();
                out.append( "File:" + fileName );

                // files in this log are known to be from one subtree
                Assert.assertTrue( "Unexpected file name: " + fileName, fileName.startsWith( "/maven/scm/trunk" ) );

                // files in this log are known not to contain space
                Assert.assertEquals( "Unexpected space found in filename: " + fileName, -1, fileName.indexOf( " " ) );

                if ( file.getOriginalName() != null )
                {
                    origFileCounter++;
                }
            }

            out.append( "==============================" );
        }

        Assert.assertEquals( "Unexpected number of file copy records", 1, origFileCounter );

        Assert.assertEquals( "Action summary differs from expectations",
                             "{modified=626, deleted=56, added=310, copied=1}", summary.toString() );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( out.toString() );
        }
    }
}
