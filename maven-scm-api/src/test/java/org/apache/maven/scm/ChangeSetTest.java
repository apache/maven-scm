/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link ChangeSet}class
 *
 * @author dion
 *
 */
public class ChangeSetTest {
    /**
     * the {@link ChangeSet} used for testing
     */
    private ChangeSet instance;

    /**
     * Initialize per test data
     */
    @BeforeEach
    public void setUp() {
        instance = createInstance();
    }

    private static ChangeSet createInstance() {
        ChangeSet instance = new ChangeSet();
        instance.setAuthor("dion");
        instance.setComment("comment");
        instance.setDate("2002/04/01 00:00:00");
        instance.setTags(Arrays.asList("v3.14", "v2<bla>.7]]1828", "<![CDATA[NastyTag"));
        return instance;
    }

    /**
     * Test of addFile methods: using ChangeFile
     */
    @Test
    public void testAddFileWithFile() {
        ChangeFile file = new ChangeFile("maven:dummy");
        instance.addFile(file);
        assertTrue(instance.toString().indexOf("maven:dummy") != -1, "File name not found in list");

        assertTrue(instance.containsFilename("maven:"));
        assertTrue(instance.containsFilename(":dummy"));
        assertTrue(instance.containsFilename(":"));
        assertTrue(instance.containsFilename("maven:dummy"));
        assertFalse(instance.containsFilename("dammy"));
    }

    /**
     * Test of toString method
     */
    @Test
    public void testToString() {
        // dion, Mon Apr 01 00:00:00 EST 2002, comment
        String value = instance.toString();
        assertTrue(value.indexOf("dion") != -1, "author not found in string");
        assertTrue(value.indexOf("comment") != -1, "comment not found in string");
        assertTrue(value.indexOf("Mon Apr 01") != -1, "date not found in string");
    }

    /**
     * Test of getAuthor method
     */
    @Test
    public void testGetAuthor() {
        assertEquals("dion", instance.getAuthor(), "Author value not retrieved correctly");
    }

    /**
     * Test of setAuthor method
     */
    @Test
    public void testSetAuthor() {
        instance.setAuthor("maven:dion");
        assertEquals("maven:dion", instance.getAuthor(), "Author not set correctly");
    }

    /**
     * Test of getComment method
     */
    @Test
    public void testGetComment() {
        assertEquals("comment", instance.getComment(), "Comment value not retrieved correctly");
    }

    /**
     * Test of setComment method
     */
    @Test
    public void testSetComment() {
        instance.setComment("maven:comment");
        assertEquals("maven:comment", instance.getComment(), "Comment not set correctly");
    }

    /**
     * Test of getDate method
     */
    @Test
    public void testGetDate() {
        assertEquals(getDate(2002, 3, 1), instance.getDate(), "Date value not retrieved correctly");
    }

    /**
     * Test of setDate method with Date object
     */
    @Test
    public void testSetDate() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        instance.setDate(date);
        assertEquals(date, instance.getDate(), "Date value not set correctly");
    }

    /**
     * Test of setDate method with String
     */
    @Test
    public void testSetDateFromString() {
        instance.setDate("2002/03/04 00:00:00");
        assertEquals(getDate(2002, 2, 4), instance.getDate(), "Date value not set correctly from a string");
    }

    /**
     * Test of getDateFormatted method
     */
    @Test
    public void testGetDateFormatted() {
        assertEquals("2002-04-01", instance.getDateFormatted(), "Date not formatted correctly");
    }

    /**
     * Test of getDateFormatted method
     */
    @Test
    public void testGetTimeFormatted() {
        assertEquals("00:00:00", instance.getTimeFormatted(), "Time not formatted correctly");
    }

    public static Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();

        cal.set(year, month, day, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    @Test
    public void testEscapeValue() {
        assertEquals("", ChangeSet.escapeValue(""));
        assertEquals("&apos;", ChangeSet.escapeValue("'"));
        assertEquals("a", ChangeSet.escapeValue("a"));
        assertEquals("a&apos;", ChangeSet.escapeValue("a'"));
        assertEquals("&apos;a&apos;", ChangeSet.escapeValue("'a'"));

        assertEquals("a&lt;b&gt;c", ChangeSet.escapeValue("a<b>c"));
        assertEquals("&apos;&amp;;&lt;&gt;&quot;", ChangeSet.escapeValue("'&;<>\""));
    }

    @Test
    public void testEquals() {
        ChangeSet instance2 = createInstance();
        assertEquals(instance, instance2);

        instance2.setComment("another comment");
        assertNotEquals(instance2, instance);

        instance2.setComment("comment");
        assertEquals(instance, instance2);
    }

    @Test
    public void testHashCode() {
        int hashCode1 = instance.hashCode();
        instance.setAuthor("anotherAuthor");

        assertNotEquals(hashCode1, instance.hashCode());
        instance.setAuthor("dion");
        assertEquals(hashCode1, instance.hashCode());
    }

    @Test
    public void testToXml() {
        String sXml = instance.toXML();
        assertNotNull(sXml);

        assertTrue(sXml.indexOf("<changelog-entry>") > -1);
        assertTrue(sXml.indexOf("</changelog-entry>") > -1);
    }

    @Test
    public void testToXmlWithFiles() {
        instance.addFile(new ChangeFile("maven1:dummy"));
        instance.addFile(new ChangeFile("maven2:dummy2"));

        String sXml = instance.toXML();
        assertNotNull(sXml);

        assertTrue(sXml.indexOf("<changelog-entry>") > -1);
        assertTrue(sXml.indexOf("</changelog-entry>") > -1);

        assertTrue(sXml.indexOf("<file>") > -1);
        assertTrue(sXml.indexOf("<name>maven1:dummy</name>") > -1);
        assertTrue(sXml.indexOf("<name>maven2:dummy2</name>") > -1);
    }
}
