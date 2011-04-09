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
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @author <a href="paul@webotech.co.uk">Paul Mackinlay</a>
 */
public class RemoveMojoTest
    extends AbstractMojoTestCase
{

    public void testShouldInvokeP4Delete()
        throws Exception
    {

        if ( !ScmTestCase.isSystemCmd( "p4" ) )
        {
            System.out.println( "'skip test as p4 is not available" );
            return;
        }

        String testConfig = "src/test/resources/mojos/remove/removeWithPerforce.xml";
        try
        {
            RemoveMojo removeMojo = (RemoveMojo) lookupMojo( "remove", getTestFile( testConfig ) );
            String connectionUrl = removeMojo.getConnectionUrl();
            connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
            connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
            removeMojo.setWorkingDirectory( new File( getBasedir() ) );
            removeMojo.setConnectionUrl( connectionUrl );

            removeMojo.execute();
        }
        finally
        {
            // Just to be sure unedit anything that has been marked for delete
            UnEditMojo unEditMojo = (UnEditMojo) lookupMojo( "unedit", getTestFile( testConfig ) );
            String connectionUrl = unEditMojo.getConnectionUrl();
            connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
            connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
            unEditMojo.setWorkingDirectory( new File( getBasedir() ) );
            unEditMojo.setConnectionUrl( connectionUrl );
            unEditMojo.execute();
        }
    }

    public void testShouldFailToInvokeP4Delete()
        throws Exception
    {
        if ( !ScmTestCase.isSystemCmd( "p4" ) )
        {
            System.out.println( "'skip test as p4 is not available" );
            return;
        }
        String testConfig = "src/test/resources/mojos/remove/removeWithPerforceNoIncludes.xml";
        try
        {
            RemoveMojo removeMojo = (RemoveMojo) lookupMojo( "remove", getTestFile( testConfig ) );
            String connectionUrl = removeMojo.getConnectionUrl();
            connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
            connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
            removeMojo.setWorkingDirectory( new File( getBasedir() ) );
            removeMojo.setConnectionUrl( connectionUrl );

            try
            {
                removeMojo.execute();
                fail( "At least one file needs to be included for removal" );
            }
            catch ( MojoExecutionException e )
            {
                // we're expecting this exception
            }
        }
        finally
        {
            // Just to be sure unedit anything that has been marked for delete
            UnEditMojo unEditMojo = (UnEditMojo) lookupMojo( "unedit", getTestFile( testConfig ) );
            String connectionUrl = unEditMojo.getConnectionUrl();
            connectionUrl = StringUtils.replace( connectionUrl, "${basedir}", getBasedir() );
            connectionUrl = StringUtils.replace( connectionUrl, "\\", "/" );
            unEditMojo.setWorkingDirectory( new File( getBasedir() ) );
            unEditMojo.setConnectionUrl( connectionUrl );
            unEditMojo.execute();
        }
    }

}
