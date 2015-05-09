package org.apache.maven.scm.provider.jazz.command;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.DebugLoggerConsumer;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.junit.Assert;

import java.io.File;

/**
 * Common utilities for Jazz TCK tests.
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzTckUtil
    extends AbstractCommand
{
    private long currentSystemTimeMillis = System.currentTimeMillis();

    private String tckBaseDir;

    private String scmUrl;

    private String snapshotName;

    /**
     * Get the specified system property. Borrowed from AccuRevTckUtil.
     * TODO: Refactor to a common usage.
     *
     * @param name         The name of the property to get.
     * @param defaultValue A default value if not found.
     * @return
     */
    public String getSystemProperty( String name, String defaultValue )
    {
        String mavenProperty = "${" + name + "}";
        String result = System.getProperty( name, mavenProperty );
        if ( mavenProperty.equals( result ) )
        {
            result = defaultValue;
        }
        return result;
    }

    /* (non-Javadoc)
    * @see org.apache.maven.scm.command.AbstractCommand#executeCommand(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
    */
    @Override
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        JazzScmProviderRepository jazzRepo = (JazzScmProviderRepository) repository;

        StreamConsumer tckConsumer =
            new DebugLoggerConsumer( getLogger() );      // No need for a dedicated consumer for this
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );
        String nameWorkspace = jazzRepo.getRepositoryWorkspace();
        //String nameSnapshot = "MavenSCMTestSnapshot";
        String nameSnapshot = getSnapshotName();
        JazzScmCommand tckCreateWorkspaceFromSnapshotCmd =
            createCreateWorkspaceFromSnapshotCommand( jazzRepo, fileSet, nameWorkspace, nameSnapshot );
        int status = tckCreateWorkspaceFromSnapshotCmd.execute( tckConsumer, errConsumer );

        if ( status != 0 )
        {
            return new ScmResult( tckCreateWorkspaceFromSnapshotCmd.getCommandString(),
                                  "Error code for Jazz SCM (create workspace --snapshot) command - " + status,
                                  errConsumer.getOutput(), false );
        }

        return new ScmResult( tckCreateWorkspaceFromSnapshotCmd.getCommandString(), "All ok",
                              ( (DebugLoggerConsumer) tckConsumer ).getOutput(), true );
    }

    // Create the JazzScmCommand to execute the "scm create workspace ..." command
    // This will create a workspace of the same name as the tag.
    private JazzScmCommand createCreateWorkspaceFromSnapshotCommand( JazzScmProviderRepository repo, ScmFileSet fileSet,
                                                                     String nameWorkspace, String nameSnapshot )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_CREATE, JazzConstants.CMD_SUB_WORKSPACE, repo, fileSet, getLogger() );

        command.addArgument( nameWorkspace );
        command.addArgument( JazzConstants.ARG_WORKSPACE_SNAPSHOT );
        command.addArgument( nameSnapshot );

        return command;
    }

    /**
     * If a TCK test case has more than one test case, it will need
     * to generate a new workspace for each test. Use this method
     * to provide uniqueness again.
     */
    public void generateNewSystemTime()
    {
        currentSystemTimeMillis = System.currentTimeMillis();
    }

    /**
     * Create a unique repository workspace using the system time, based
     * upon a supplied snapshot. The creation of this initial snapshot
     * currently can not be scripted, so it needs to be done manually first.
     *
     * @see org.apache.maven.scm.ScmTckTestCase#initRepo()
     */
    public void initRepo( ScmRepository repository )
        throws Exception
    {
        // Set a default logger. because I cann't get to the ones later on...
        setLogger( new DefaultLog() );
        // Create the unique workspace based upon a snapshot
        executeCommand( repository.getProviderRepository(), new ScmFileSet( getWorkingCopy() ), null );
    }

    /**
     * This method is available to those SCM clients that need to perform
     * a cleanup at the end of the tests. It is needed when server side
     * operations are performed, or the check out dirs are outside
     * of the normal target directory.
     */
    public void removeRepo()
        throws Exception
    {
        FileUtils.deleteDirectory( new File( getTckBaseDir() ) );
    }

    /**
     * Return the URL used for this specific TCK test execution.
     * It generates a unique workspace name, based on the system time.
     *
     * @see org.apache.maven.scm.ScmTckTestCase#getScmUrl()
     */
    public String getScmUrl()
        throws Exception
    {
        if ( scmUrl == null )
        {
            // tckUrlPrefix is the system property that is used to seed the SCM URL.
            // EG:
            // "scm:jazz:Deb;Deb@https://rtc:9444/jazz:MavenSCMTestWorkspace"
            //
            String tckUrlPrefix = getSystemProperty( "tckUrlPrefix", "" );
            if ( StringUtils.isBlank( tckUrlPrefix ) )
            {
                Assert.fail( "Property \"tckUrlPrefix\" is not set." );
            }

            scmUrl = tckUrlPrefix + "_" + currentSystemTimeMillis;
        }

        return scmUrl;
    }

    /**
     * Get the snapshot name, getting it from the system properties if necessary.
     *
     * @return The name of the snapshot used to create a repository workspace,
     *         which is then loaded into the tckBaseDir.
     */
    private String getSnapshotName()
    {
        if ( snapshotName == null )
        {
            snapshotName = getSystemProperty( "tckSnapshotName", "" );
            if ( StringUtils.isBlank( snapshotName ) )
            {
                Assert.fail( "Property \"tckSnapshotName\" is not set." );
            }
        }

        return snapshotName;
    }

    /**
     * Get the base directory used for the tck tests.
     *
     * @return The base directory used for the tck tests, the sandbox.
     */
    private String getTckBaseDir()
    {
        if ( tckBaseDir == null )
        {
            tckBaseDir = getSystemProperty( "tckBaseDir", "" );
            if ( StringUtils.isBlank( tckBaseDir ) )
            {
                Assert.fail( "Property \"tckBaseDir\" is not set." );
            }
        }

        return tckBaseDir;
    }

    /**
     * @see org.apache.maven.scm.ScmTestCase#getWorkingCopy()
     */
    public File getWorkingCopy()
    {
        return new File( getTckBaseDir() + "/wc" );
    }

    /**
     * @see org.apache.maven.scm.ScmTestCase#getAssertionCopy()
     */
    public File getAssertionCopy()
    {
        return new File( getTckBaseDir() + "/ac" );
    }

    /**
     * @see org.apache.maven.scm.ScmTestCase#getUpdatingCopy()
     */
    public File getUpdatingCopy()
    {
        return new File( getTckBaseDir() + "/uc" );
    }
}
