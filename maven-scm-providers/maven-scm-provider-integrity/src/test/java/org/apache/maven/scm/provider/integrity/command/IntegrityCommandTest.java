package org.apache.maven.scm.provider.integrity.command;

/**
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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.manager.plexus.PlexusLogger;
import org.apache.maven.scm.provider.integrity.command.checkout.IntegrityCheckOutCommand;
import org.apache.maven.scm.provider.integrity.command.login.IntegrityLoginCommand;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.logging.LoggerManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Parent class IntegrityCommandTest for all Integrity Test Command executions
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityCommandTest.java 1.1 2011/08/29 00:29:46EDT Cletus D'Souza (dsouza) Exp  $
 */
public abstract class IntegrityCommandTest
    extends ScmTestCase
{
    // A simple date format to generate unique names for the test run
    public static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmssSSS" );

    public static final String fileName = "NewFile_" + sdf.format( new Date() ) + ".txt";

    // A locally pre-configured repository for use with executing Integrity SCM Commands
    protected String testScmURL = "scm:integrity|dsouza/password@xpvm:7001|#/Code_Gen";

    protected ScmManager scmManager;

    protected ScmLogger logger;

    protected IntegrityScmProviderRepository iRepo;

    protected ScmFileSet fileSet;

    protected CommandParameters parameters;

    /**
     * Sets up all commands for unit test execution
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();
        // Set the Change Package ID to :bypass as we wont have a valid Change Pacakge ID for automated tests
        System.setProperty( "maven.scm.integrity.cpid", ":bypass" );
        // Initialize our scmManager
        scmManager = getScmManager();
        // Initialize our logger
        LoggerManager loggerManager = (LoggerManager) getContainer().lookup( LoggerManager.ROLE );
        logger = new PlexusLogger( loggerManager.getLoggerForComponent( ScmManager.ROLE ) );
        // Construct the SCM Repository and initialize our command execution variables
        ScmRepository repo = scmManager.makeScmRepository( testScmURL );
        iRepo = (IntegrityScmProviderRepository) repo.getProviderRepository();
        fileSet = new ScmFileSet( getTestFile( "target/test-execution" ) );
        parameters = new CommandParameters();
        // Set the tag name for our tag and branch commands
        parameters.setString( CommandParameter.TAG_NAME,
                              "Maven-${new java.text.SimpleDateFormat(\"yyyyMMddHHmmssSSS\").format(new Date())}" );
        // Connect to the MKS Integrity Server
        IntegrityLoginCommand login = new IntegrityLoginCommand();
        login.setLogger( logger );
        assertResultIsSuccess( login.execute( iRepo, fileSet, parameters ) );
        // Then make sure we've got a sandbox to work with
        IntegrityCheckOutCommand checkout = new IntegrityCheckOutCommand();
        checkout.setLogger( logger );
        assertResultIsSuccess( checkout.execute( iRepo, fileSet, parameters ) );
    }
}

