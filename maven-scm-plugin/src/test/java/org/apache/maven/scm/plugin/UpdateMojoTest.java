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
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class UpdateMojoTest
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

    public void testSkipCheckoutWithConnectionUrl()
        throws Exception
    {
        if ( !ScmTestCase.isSystemCmd( SvnScmTestUtils.SVNADMIN_COMMAND_LINE ) )
        {
            ScmTestCase.printSystemCmdUnavail( SvnScmTestUtils.SVNADMIN_COMMAND_LINE, getName() );
            return;
        }

        SvnScmTestUtils.initializeRepository( repository );

        if ( !ScmTestCase.isSystemCmd( SvnScmTestUtils.SVN_COMMAND_LINE ) )
        {
            ScmTestCase.printSystemCmdUnavail( SvnScmTestUtils.SVN_COMMAND_LINE, getName() );
            return;
        }

        CheckoutMojo checkoutMojo = (CheckoutMojo) lookupMojo( "checkout", getTestFile(
            "src/test/resources/mojos/checkout/checkoutWithConnectionUrl.xml" ) );

        String connectionUrl = checkoutMojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        checkoutMojo.setConnectionUrl( connectionUrl );

        checkoutMojo.execute();

        UpdateMojo updateMojo = (UpdateMojo) lookupMojo( "update", getTestFile(
            "src/test/resources/mojos/update/updateWithConnectionUrl.xml" ) );

        connectionUrl = updateMojo.getConnectionUrl();
        connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
        connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
        updateMojo.setConnectionUrl( connectionUrl );

        updateMojo.execute();

    }

}
