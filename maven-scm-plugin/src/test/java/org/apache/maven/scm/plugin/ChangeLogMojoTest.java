package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ChangeLogMojoTest
    extends AbstractMojoTestCase
{
    File repository;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        repository = getTestFile( "target/repository" );

        FileUtils.forceDelete( repository );

        SvnScmTestUtils.initializeRepository( repository );
    }

    public void testChangeLog()
        throws Exception
    {
        ChangeLogMojo mojo = (ChangeLogMojo) lookupMojo( "changelog", getTestFile(
            "src/test/resources/mojos/changelog/changelog.xml" ) );

        String connectionUrl = mojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        mojo.setConnectionUrl( connectionUrl );
        mojo.setWorkingDirectory( new File( getBasedir() ) );
        mojo.setConnectionType( "connection" );

        mojo.execute();
    }

    public void testChangeLogWithParameters()
        throws Exception
    {
        ChangeLogMojo mojo = (ChangeLogMojo) lookupMojo( "changelog", getTestFile(
            "src/test/resources/mojos/changelog/changelogWithParameters.xml" ) );

        String connectionUrl = mojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        mojo.setConnectionUrl( connectionUrl );
        mojo.setWorkingDirectory( new File( getBasedir() ) );
        mojo.setConnectionType( "connection" );

        mojo.execute();
    }

    public void testChangeLogWithBadUserDateFormat()
        throws Exception
    {
        ChangeLogMojo mojo = (ChangeLogMojo) lookupMojo( "changelog", getTestFile(
            "src/test/resources/mojos/changelog/changelogWithBadUserDateFormat.xml" ) );

        String connectionUrl = mojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        mojo.setConnectionUrl( connectionUrl );
        mojo.setWorkingDirectory( new File( getBasedir() ) );
        mojo.setConnectionType( "connection" );

        try
        {
            mojo.execute();

            fail( "mojo execution must fail." );
        }
        catch ( MojoExecutionException e )
        {
            assertTrue( true );
        }
    }

    public void testChangeLogWithBadConnectionUrl()
        throws Exception
    {
        ChangeLogMojo mojo = (ChangeLogMojo) lookupMojo( "changelog", getTestFile(
            "src/test/resources/mojos/changelog/changelogWithBadConnectionUrl.xml" ) );

        String connectionUrl = mojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        mojo.setConnectionUrl( connectionUrl );
        mojo.setWorkingDirectory( new File( getBasedir() ) );
        mojo.setConnectionType( "connection" );

        try
        {
            mojo.execute();

            fail( "mojo execution must fail." );
        }
        catch ( MojoExecutionException e )
        {
            assertTrue( true );
        }
    }
}
