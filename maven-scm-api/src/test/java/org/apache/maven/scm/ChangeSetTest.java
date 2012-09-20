package org.apache.maven.scm;

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

import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;

/**
 * Tests for the {@link ChangeSet}class
 *
 * @author dion
 *
 */
public class ChangeSetTest
    extends TestCase
{
    /**
     * the {@link ChangeSet} used for testing
     */
    private ChangeSet instance;

    /**
     * Initialize per test data
     */
    public void setUp()
    {
        instance = createInstance();
    }

    private static ChangeSet createInstance()
    {
        ChangeSet instance = new ChangeSet();
        instance.setAuthor( "dion" );
        instance.setComment( "comment" );
        instance.setDate( "2002/04/01 00:00:00" );
        return instance;
    }

    /**
     * Test of addFile methods: using ChangeFile
     */
    public void testAddFileWithFile()
    {
        ChangeFile file = new ChangeFile( "maven:dummy" );
        instance.addFile( file );
        assertTrue( "File name not found in list", instance.toString().indexOf( "maven:dummy" ) != -1 );

        assertTrue( instance.containsFilename("maven:") );
        assertTrue( instance.containsFilename(":dummy") );
        assertTrue( instance.containsFilename(":") );
        assertTrue( instance.containsFilename("maven:dummy") );
        assertFalse( instance.containsFilename("dammy") );
    }

    /**
     * Test of toString method
     */
    public void testToString()
    {
        //dion, Mon Apr 01 00:00:00 EST 2002, comment
        String value = instance.toString();
        assertTrue( "author not found in string", value.indexOf( "dion" ) != -1 );
        assertTrue( "comment not found in string", value.indexOf( "comment" ) != -1 );
        assertTrue( "date not found in string", value.indexOf( "Mon Apr 01" ) != -1 );
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
        assertEquals( "Date value not retrieved correctly", getDate( 2002, 3, 1 ), instance.getDate() );
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
        assertEquals( "Date value not set correctly from a string", getDate( 2002, 2, 4 ), instance.getDate() );
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

    public static Date getDate( int year, int month, int day )
    {
        Calendar cal = Calendar.getInstance();

        cal.set( year, month, day, 0, 0, 0 );
        cal.set( Calendar.MILLISECOND, 0 );

        return cal.getTime();
    }

    public void testEscapeValue()
    {
        assertEquals( "", ChangeSet.escapeValue("") );
        assertEquals( "&apos;", ChangeSet.escapeValue("'") );
        assertEquals( "a", ChangeSet.escapeValue("a") );
        assertEquals( "a&apos;", ChangeSet.escapeValue("a'") );
        assertEquals( "&apos;a&apos;", ChangeSet.escapeValue("'a'") );

        assertEquals( "a&lt;b&gt;c", ChangeSet.escapeValue("a<b>c") );
        assertEquals( "&apos;&amp;;&lt;&gt;&quot;", ChangeSet.escapeValue("'&;<>\"") );
    }

    public void testEquals()
    {
        ChangeSet instance2 = createInstance();
        assertEquals(instance, instance2);

        instance2.setComment("another comment");
        assertFalse(instance2.equals(instance));

        instance2.setComment("comment");
        assertEquals(instance, instance2);
    }

    public void testHashCode()
    {
        int hashCode1 = instance.hashCode();
        instance.setAuthor("anotherAuthor");

        assertFalse( hashCode1 == instance.hashCode() );
        instance.setAuthor( "dion" );
        assertEquals( hashCode1, instance.hashCode() );
    }

    public void testToXml()
    {
        String sXml = instance.toXML();
        assertNotNull(sXml);

        assertTrue(sXml.indexOf("<changelog-entry>") > -1);
        assertTrue(sXml.indexOf("</changelog-entry>") > -1);
    }

    public void testToXmlWithFiles()
    {
        instance.addFile( new ChangeFile( "maven1:dummy" ) );
        instance.addFile( new ChangeFile( "maven2:dummy2" ) );

        String sXml = instance.toXML();
        assertNotNull(sXml);

        assertTrue(sXml.indexOf("<changelog-entry>") > -1);
        assertTrue(sXml.indexOf("</changelog-entry>") > -1);

        assertTrue(sXml.indexOf("<file>") > -1);
        assertTrue(sXml.indexOf("<name>maven1:dummy</name>") > -1);
        assertTrue(sXml.indexOf("<name>maven2:dummy2</name>") > -1);


    }

}
