package org.apache.maven.scm;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import junit.framework.TestCase;
import org.apache.maven.scm.ScmTestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * Tests for the {@link ChangeSet}class
 *
 * @author dion
 * @version $Id: ChangeSetTest.java 162299 2005-01-18 12:31:28Z brett $
 */
public class ChangeSetTest
    extends TestCase
{
    /** the {@link ChangeSet} used for testing */
    private ChangeSet instance;

    /**
     * Initialize per test data
     */
    public void setUp()
    {
        instance = new ChangeSet();
        instance.setAuthor( "dion" );
        instance.setComment( "comment" );
        instance.setDate( "2002/04/01 00:00:00" );
    }

    /**
     * Test of addFile methods: using ChangeFile
     */
    public void testAddFileWithFile()
    {
        ChangeFile file = new ChangeFile( "maven:dummy" );
        instance.setFile( file );
        assertTrue( "File name not found in list", instance.toString().indexOf( "maven:dummy" ) != -1 );
    }

    /**
     * Test of toString method
     */
    public void testToString()
    {
        //dion, Mon Apr 01 00:00:00 EST 2002, null, comment
        String value = instance.toString();
        assertTrue( "author not found in string", value.indexOf( "dion" ) != -1 );
        assertTrue( "comment not found in string", value.indexOf( "comment" ) != -1 );
        assertTrue( "date not found in string", value.indexOf( "Mon Apr 01" ) != -1 );
        assertTrue( "null file not found in string" + value, value.indexOf( "null" ) != -1 );
    }

    /**
     * Test of getAuthor method
     */
    public void testGetAuthor()
    {
        assertEquals( "Author value not retrieved correctly", "dion", instance.getAuthor() );
    }

    /**
     * Test of setAuthor method
     */
    public void testSetAuthor()
    {
        instance.setAuthor( "maven:dion" );
        assertEquals( "Author not set correctly", "maven:dion", instance.getAuthor() );
    }

    /**
     * Test of getComment method
     */
    public void testGetComment()
    {
        assertEquals( "Comment value not retrieved correctly", "comment", instance.getComment() );
    }

    /**
     * Test of setComment method
     */
    public void testSetComment()
    {
        instance.setComment( "maven:comment" );
        assertEquals( "Comment not set correctly", "maven:comment", instance.getComment() );
    }

    /**
     * Test of getDate method
     */
    public void testGetDate()
    {
        assertEquals( "Date value not retrieved correctly", ScmTestCase.getDate( 2002, 3, 1 ), instance.getDate() );
    }

    /**
     * Test of setDate method with Date object
     */
    public void testSetDate()
    {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        instance.setDate( date );
        assertEquals( "Date value not set correctly", date, instance.getDate() );
    }

    /**
     * Test of setDate method with String
     */
    public void testSetDateFromString()
    {
        instance.setDate( "2002/03/04 00:00:00" );
        assertEquals( "Date value not set correctly from a string", ScmTestCase.getDate( 2002, 2, 4 ), instance.getDate() );
    }

    /**
     * Test of getDateFormatted method
     */
    public void testGetDateFormatted()
    {
        assertEquals( "Date not formatted correctly", "2002-04-01", instance.getDateFormatted() );
    }

    /**
     * Test of getDateFormatted method
     */
    public void testGetTimeFormatted()
    {
        assertEquals( "Time not formatted correctly", "00:00:00", instance.getTimeFormatted() );
    }
}
