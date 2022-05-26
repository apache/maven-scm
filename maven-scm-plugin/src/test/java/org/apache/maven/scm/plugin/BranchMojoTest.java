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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.scm.PlexusJUnit4TestSupport;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;

import static org.apache.maven.scm.PlexusJUnit4TestSupport.getBasedir;
import static org.apache.maven.scm.PlexusJUnit4TestSupport.getTestFile;
import static org.apache.maven.scm.ScmTestCase.checkScmPresence;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
@RunWith(JUnit4.class)
public class BranchMojoTest
    extends AbstractJUnit4MojoTestCase
{
    File checkoutDir;

    File repository;

    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();

        checkoutDir = getTestFile( "target/checkout" );

        FileUtils.forceDelete( checkoutDir );

        repository = getTestFile( "target/repository" );

        FileUtils.forceDelete( repository );

        checkScmPresence( SvnScmTestUtils.SVNADMIN_COMMAND_LINE, "setUp" );

        SvnScmTestUtils.initializeRepository( repository );

        checkScmPresence( SvnScmTestUtils.SVN_COMMAND_LINE, "setUp" );

        CheckoutMojo checkoutMojo = (CheckoutMojo) lookupMojo( "checkout", getTestFile(
            "src/test/resources/mojos/checkout/checkoutWithConnectionUrl.xml" ) );
        checkoutMojo.setWorkingDirectory( new File( getBasedir() ) );

        String connectionUrl = checkoutMojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        checkoutMojo.setConnectionUrl( connectionUrl );

        checkoutMojo.setCheckoutDirectory( checkoutDir );

        checkoutMojo.execute();
    }

    @Test
    public void testBranch()
        throws Exception
    {
        checkScmPresence( SvnScmTestUtils.SVNADMIN_COMMAND_LINE, "testBranch" );

        BranchMojo mojo =
            (BranchMojo) lookupMojo( "branch", PlexusJUnit4TestSupport.getTestFile( "src/test/resources/mojos/branch/branch.xml" ) );
        mojo.setWorkingDirectory( checkoutDir );

        String connectionUrl = mojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", PlexusJUnit4TestSupport.getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        mojo.setConnectionUrl( connectionUrl );

        mojo.execute();

        CheckoutMojo checkoutMojo =
            (CheckoutMojo) lookupMojo( "checkout", PlexusJUnit4TestSupport.getTestFile( "src/test/resources/mojos/branch/checkout.xml" ) );
        checkoutMojo.setWorkingDirectory( new File( PlexusJUnit4TestSupport.getBasedir() ) );

        connectionUrl = checkoutMojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", PlexusJUnit4TestSupport.getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        checkoutMojo.setConnectionUrl( connectionUrl );

        File branchCheckoutDir = PlexusJUnit4TestSupport.getTestFile( "target/branches/mybranch" );
        if ( branchCheckoutDir.exists() )
        {
            FileUtils.deleteDirectory( branchCheckoutDir );
        }
        checkoutMojo.setCheckoutDirectory( branchCheckoutDir );

        assertFalse( new File( branchCheckoutDir, "pom.xml" ).exists() );
        checkoutMojo.execute();
        assertTrue( new File( branchCheckoutDir, "pom.xml" ).exists() );
    }
}
