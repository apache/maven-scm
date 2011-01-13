package org.apache.maven.scm.provider.synergy.consumer;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.synergy.util.SynergyTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyGetCompletedTasksConsumerTest
    extends ScmTestCase
{
    public void testSynergyGetCompletedTasksConsumer()
        throws IOException
    {
        InputStream inputStream = getResourceAsStream( "/synergy/consumer/getCompletedTasks.txt" );

        BufferedReader in = new BufferedReader( new InputStreamReader( inputStream ) );

        String s = in.readLine();

        SynergyGetCompletedTasksConsumer consumer = new SynergyGetCompletedTasksConsumer( new DefaultLog() );

        while ( s != null )
        {
            consumer.consumeLine( s );

            s = in.readLine();
        }

        Collection<SynergyTask> entries = consumer.getTasks();

        assertEquals( "Wrong number of tasks returned", 1, entries.size() );

        SynergyTask task = (SynergyTask) entries.iterator().next();
        assertEquals( 35, task.getNumber() );
        assertEquals( "AHD456", task.getUsername() );
        assertEquals( "gefdeag", task.getComment() );
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set( 2006, Calendar.OCTOBER, 4, 15, 42, 47 );

        assertEquals( cal.getTime(), task.getModifiedTime() );

    }

    public void testSynergyGetCompletedTasksConsumerWithFrenchDate()
        throws IOException
    {
        InputStream inputStream = getResourceAsStream( "/synergy/consumer/getCompletedTasksFrenchDate.txt" );

        BufferedReader in = new BufferedReader( new InputStreamReader( inputStream ) );

        String s = in.readLine();

        System.setProperty( "maven.scm.synergy.ccmDateFormat", "EEEE dd MMMM yyyy HH:mm:ss" );
        System.setProperty( "maven.scm.synergy.language", "fr" );
        System.setProperty( "maven.scm.synergy.country", "FR" );
        SynergyGetCompletedTasksConsumer consumer = new SynergyGetCompletedTasksConsumer( new DefaultLog() );

        while ( s != null )
        {
            consumer.consumeLine( s );

            s = in.readLine();
        }

        Collection<SynergyTask> entries = consumer.getTasks();

        assertEquals( "Wrong number of tasks returned", 2, entries.size() );

        Iterator<SynergyTask> i = entries.iterator();
        SynergyTask task = (SynergyTask) i.next();
        assertEquals( 52, task.getNumber() );
        assertEquals( "ccm_root", task.getUsername() );
        assertEquals( "Modification du pom (url scm)", task.getComment() );
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set( 2006, Calendar.OCTOBER, 6, 10, 3, 59 );
        assertEquals( cal.getTime(), task.getModifiedTime() );

        task = (SynergyTask) i.next();
        assertEquals( 53, task.getNumber() );
        assertEquals( "ccm_root", task.getUsername() );
        assertEquals( "Inverser l'odre purpose/release", task.getComment() );
        cal.clear();
        cal.set( 2006, Calendar.OCTOBER, 6, 10, 47, 13 );
        assertEquals( cal.getTime(), task.getModifiedTime() );
    }
}
