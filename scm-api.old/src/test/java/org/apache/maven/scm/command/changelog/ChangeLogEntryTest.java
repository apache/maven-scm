package org.apache.maven.scm.command.changelog;

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

import java.util.Calendar;
import java.util.Date;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Tests for the {@link ChangeLogEntry} class
 *
 * @author dion
 * @version $Id$
 */
public class ChangeLogEntryTest extends TestCase
{
    
    /** the {@link ChangeLogEntry} used for testing */
    private ChangeLogEntry instance;
    
    /**
     * Create a test with the given name
     * @param testName the name of the test
     */
    public ChangeLogEntryTest(String testName)
    {
        super(testName);
    }
    
    /**
     * Run the test using the {@link TestRunner}
     * @param args command line provided arguments
     */
    public static void main(String[] args) 
    {
        TestRunner.run(suite());
    }
    
    /**
     * Create a test suite for this class
     * @return a {@link TestSuite} for all tests in this class
     */
    public static Test suite()
    {
        return new TestSuite(ChangeLogEntryTest.class);
    }
    
    /**
     * Initialize per test data
     */
    public void setUp()
    {
        instance = new ChangeLogEntry();
        instance.setAuthor("dion");
        instance.setComment("comment");
        instance.setDate("2002/04/01 00:00:00");
    }
    
    /** 
     * Test of addFile methods: using ChangeLogFile
     */
    public void testAddFileWithFile()
    {
        ChangeLogFile file = new ChangeLogFile("maven:dummy");
        instance.addFile(file);
        assertTrue("File name not found in list",
            instance.toString().indexOf("maven:dummy") != -1 );
    }

    /** 
     * Test of addFile methods: using file & revision
     */
    public void testAddFileWithFileAndRevision()
    {
        instance.addFile("maven:dummy", "x.y");
        assertTrue("File name not found in list", 
            instance.toString().indexOf("maven:dummy") != -1);
        assertTrue("Revision not found in list", 
            instance.toString().indexOf("x.y") != -1);
    }

    /** 
     * Test of toString method
     */
    public void testToString()
    {
        //dion, Mon Apr 01 00:00:00 EST 2002, [], comment
        String value = instance.toString();
        assertTrue("author not found in string", value.indexOf("dion") != -1);
        assertTrue("comment not found in string", 
            value.indexOf("comment") != -1);
        assertTrue("date not found in string", 
            value.indexOf("Mon Apr 01") != -1);
        assertTrue("empty file list not found in string", 
            value.indexOf("[]") != -1);
    }
    
    /**
     * Test of toXML method
     */
    public void testToXML()
    {
        String value = instance.toXML();
        String trimmedValue = value.trim();
        assertTrue("XML doesn't start with changelog-entry",
            trimmedValue.startsWith("<changelog-entry>"));
        assertTrue("XML doesn't contain date", 
            value.indexOf("<date>2002-04-01</date>") != -1);
        assertTrue("XML doesn't contain author CDATA", 
            value.indexOf("<author><![CDATA[dion]]></author>") != -1);
        assertTrue("XML doesn't contain comment CDATA",
            value.indexOf("<msg><![CDATA[comment]]></msg>") != -1);
    }
    
    /**
     * Test of getAuthor method
     */
    public void testGetAuthor()
    {
        assertEquals("Author value not retrieved correctly", "dion",  
            instance.getAuthor());
    }
    
    /** 
     * Test of setAuthor method
     */
    public void testSetAuthor()
    {
        instance.setAuthor("maven:dion");
        assertEquals("Author not set correctly", "maven:dion", 
            instance.getAuthor());
    }
    
    /** 
     * Test of getComment method
     */
    public void testGetComment()
    {
        assertEquals("Comment value not retrieved correctly", "comment", 
            instance.getComment());
    }
    
    /**
     * Test of setComment method
     */
    public void testSetComment()
    {
        instance.setComment("maven:comment");
        assertEquals("Comment not set correctly", "maven:comment", 
            instance.getComment());
    }
    
    /**
     * Test of getDate method
     */
    public void testGetDate()
    {
        Calendar cal = Calendar.getInstance();
        cal.set(2002, 3, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals("Date value not retrieved correctly",  cal.getTime(), 
            instance.getDate());
    }
    
    /**
     * Test of setDate method with Date object
     */
    public void testSetDate()
    {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        instance.setDate(date);
        assertEquals("Date value not set correctly", date, instance.getDate());
    }
    
    /**
     * Test of setDate method with String
     */
    public void testSetDateFromString()
    {
        instance.setDate("2002/03/04 00:00:00");
        Calendar cal = Calendar.getInstance();
        cal.set(2002, 2, 4, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        assertEquals("Date value not set correctly from a string", 
            cal.getTime(), instance.getDate());
    }

    
    /** 
     * Test of getDateFormatted method
     */
    public void testGetDateFormatted()
    {
        assertEquals("Date not formatted correctly", "2002-04-01",
            instance.getDateFormatted());
    }

    /** 
     * Test of getDateFormatted method
     */
    public void testGetTimeFormatted()
    {
        assertEquals("Time not formatted correctly", "00:00:00",
            instance.getTimeFormatted());
    }

    // Add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
}
