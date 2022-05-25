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
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class CheckoutMojoTest
    extends AbstractMojoTestCase
{
    File checkoutDir;

    File repository;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        checkoutDir = getTestFile( "target/checkout" );

        repository = getTestFile( "target/repository" );

        FileUtils.forceDelete( checkoutDir );
    }

    public void testSkipCheckoutWhenCheckoutDirectoryExistsAndSkip()
        throws Exception
    {
        FileUtils.forceDelete( checkoutDir );
        checkoutDir.mkdirs();

        CheckoutMojo mojo = (CheckoutMojo) lookupMojo( "checkout", getTestFile(
            "src/test/resources/mojos/checkout/checkoutWhenCheckoutDirectoryExistsAndSkip.xml" ) );

        mojo.setCheckoutDirectory( checkoutDir );

        mojo.execute();

        assertEquals( 0, checkoutDir.listFiles().length );
    }

    public void testSkipCheckoutWithConnectionUrl()
        throws Exception
    {
        if ( !ScmTestCase.isSystemCmd( SvnScmTestUtils.SVNADMIN_COMMAND_LINE ) )
        {
            ScmTestCase.printSystemCmdUnavail( SvnScmTestUtils.SVNADMIN_COMMAND_LINE, getName() );
            return;
        }

        FileUtils.forceDelete( checkoutDir );

        SvnScmTestUtils.initializeRepository( repository );

        if ( !ScmTestCase.isSystemCmd( SvnScmTestUtils.SVN_COMMAND_LINE ) )
        {
            ScmTestCase.printSystemCmdUnavail( SvnScmTestUtils.SVN_COMMAND_LINE, getName() );
            return;
        }

        CheckoutMojo mojo = (CheckoutMojo) lookupMojo( "checkout", getTestFile(
            "src/test/resources/mojos/checkout/checkoutWithConnectionUrl.xml" ) );
        mojo.setWorkingDirectory( new File( getBasedir() ) );

        String connectionUrl = mojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        mojo.setConnectionUrl( connectionUrl );

        mojo.setCheckoutDirectory( checkoutDir );

        mojo.execute();
    }

    public void testSkipCheckoutWithoutConnectionUrl()
        throws Exception
    {
        FileUtils.forceDelete( checkoutDir );

        checkoutDir.mkdirs();
        CheckoutMojo mojo = (CheckoutMojo) lookupMojo( "checkout", getTestFile(
            "src/test/resources/mojos/checkout/checkoutWithoutConnectionUrl.xml" ) );

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

    public void testUseExport()
        throws Exception
    {
        if ( !ScmTestCase.isSystemCmd( SvnScmTestUtils.SVN_COMMAND_LINE ) )
        {
            ScmTestCase.printSystemCmdUnavail( SvnScmTestUtils.SVN_COMMAND_LINE, getName() );
            return;
        }

        FileUtils.forceDelete( checkoutDir );

        checkoutDir.mkdirs();

        CheckoutMojo mojo = (CheckoutMojo) lookupMojo( "checkout", getTestFile(
            "src/test/resources/mojos/checkout/checkoutUsingExport.xml" ) );

        mojo.setCheckoutDirectory( checkoutDir );

        mojo.execute();

        assertTrue( checkoutDir.listFiles().length > 0  );
        assertFalse( new File( checkoutDir, ".svn" ).exists() );
    }

    public void testExcludeInclude()
        throws Exception
    {
        if ( !ScmTestCase.isSystemCmd( SvnScmTestUtils.SVNADMIN_COMMAND_LINE ) )
        {
            ScmTestCase.printSystemCmdUnavail( SvnScmTestUtils.SVNADMIN_COMMAND_LINE, getName() );
            return;
        }

        FileUtils.forceDelete( checkoutDir );

        checkoutDir.mkdirs();

        SvnScmTestUtils.initializeRepository( repository );

        if ( !ScmTestCase.isSystemCmd( SvnScmTestUtils.SVN_COMMAND_LINE ) )
        {
            ScmTestCase.printSystemCmdUnavail( SvnScmTestUtils.SVN_COMMAND_LINE, getName() );
            return;
        }

        CheckoutMojo mojo = (CheckoutMojo) lookupMojo(
                                                       "checkout",
                                                       getTestFile( "src/test/resources/mojos/checkout/checkoutWithExcludesIncludes.xml" ) );

        mojo.setCheckoutDirectory( checkoutDir );

        mojo.execute();

        assertTrue( checkoutDir.listFiles().length > 0 );
        assertTrue( new File( checkoutDir, ".svn").exists() );
        assertTrue( new File( checkoutDir, "pom.xml" ).exists() );
        assertFalse( new File( checkoutDir, "readme.txt" ).exists() );
        assertFalse( new File( checkoutDir, "src/test" ).exists() );
        assertTrue( new File( checkoutDir, "src/main/java" ).exists() );
        // olamy those files not exists anymore with svn 1.7
        //assertTrue( new File( checkoutDir, "src/main/java/.svn" ).exists() );
        //assertTrue( new File( checkoutDir, "src/main/.svn" ).exists() );
    }

    public void testEncryptedPasswordFromSettings()
        throws Exception
    {
        File pom = getTestFile( "src/test/resources/mojos/checkout/checkoutEncryptedPasswordFromSettings.xml" );
        CheckoutMojo mojo = (CheckoutMojo) lookupMojo( "checkout", pom );
        ScmProviderRepositoryWithHost repo =
            (ScmProviderRepositoryWithHost) mojo.getScmRepository().getProviderRepository();

        assertEquals( "testuser", repo.getUser() );
        assertEquals( "testpass", repo.getPassword() );
        assertEquals( "testphrase", repo.getPassphrase() );
    }

}
