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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.Stream;
import org.apache.maven.scm.provider.accurev.cli.AccuRevJUnitUtil;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith( MockitoJUnitRunner.class )
public abstract class AbstractAccuRevCommandTest
    extends ScmTestCase
{

    @Override
    protected InputStream getCustomConfiguration()
        throws Exception
    {
        return AccuRevJUnitUtil.getPlexusConfiguration();
    }

    @Mock
    protected AccuRev accurev;

    protected File basedir;

    protected AccuRevInfo info;

    private ScmLogger logger;

    protected InOrder sequence;

    protected AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();

    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
        logger = AccuRevJUnitUtil.getLogger( getContainer() );
        basedir = getWorkingCopy();
        sequence = inOrder( accurev );

        info = new AccuRevInfo( basedir );
        info.setUser( "me" );

        when( accurev.getCommandLines() ).thenReturn( "accurev mock" );
        when( accurev.getErrorOutput() ).thenReturn( "accurev mock error output" );
        when( accurev.getClientVersion() ).thenReturn( "4.7.4b" );
        when( accurev.showStream( "myStream" ) ).thenReturn(
                                                             new Stream( "myStream", 10L, "myDepot", 1L, "myDepot",
                                                                         new Date(), "normal" ) );

        when( accurev.info( null ) ).thenReturn( info );
        when( accurev.info( basedir ) ).thenReturn( info );

        repo.setLogger( getLogger() );
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

    }

    protected ScmLogger getLogger()
    {
        return logger;
    }

}