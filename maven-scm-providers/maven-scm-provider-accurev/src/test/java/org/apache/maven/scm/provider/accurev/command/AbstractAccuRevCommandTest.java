package org.apache.maven.scm.provider.accurev.command;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith( JUnit4.class )
public abstract class AbstractAccuRevCommandTest
    extends ScmTestCase
{

    protected Mockery context;

    protected AccuRev accurev;

    protected File basedir;

    protected AccuRevInfo info;

    protected Sequence sequence;

    private ScmLogger logger = new DefaultLog(); // TODO switch between Debug and DefaultLog by property.

    @Before
    public void setUp()
        throws Exception
    {
        context = new Mockery();
        accurev = context.mock( AccuRev.class );

        basedir = getWorkingCopy();
        sequence = context.sequence( "accurev" );

        info = new AccuRevInfo( basedir );
        info.setUser( "me" );

        context.checking( new Expectations()
        {
            {
                atLeast( 1 ).of( accurev ).reset();

                allowing( accurev ).getCommandLines();
                will( returnValue( "accurev mock" ) );

                allowing( accurev ).getErrorOutput();
                will( returnValue( "accurev mock" ) );
            }
        } );

    }

    protected ScmLogger getLogger()
    {
        return logger;
    }

}