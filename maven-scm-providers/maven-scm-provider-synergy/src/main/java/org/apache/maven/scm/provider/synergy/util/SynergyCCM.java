package org.apache.maven.scm.provider.synergy.util;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmVersion;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This class contains methods to execute Synergy <code>ccm</code> command line.
 *
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 * @version $Id$
 */
public class SynergyCCM
{

    private static final String CCM = "ccm";

    private static final String BASELINE = "baseline";

    private static final String CI = "ci";

    private static final String CO = "co";

    private static final String CREATE = "create";

    private static final String DELETE = "delete";

    private static final String DELIMITER = "delimiter";

    private static final String DIR = "dir";

    private static final String QUERY = "query";

    private static final String RECONCILE = "rwa";

    private static final String RECONFIGURE = "reconfigure";

    private static final String RECONFIGURE_PROPERTIES = "reconfigure_properties";

    private static final String START = "start";

    private static final String STOP = "stop";

    private static final String SYNC = "sync";

    private static final String TASK = "task";

    private static final String WA = "wa";

    /**
     * Create commandline for getting list of objects in a task.
     *
     * @param taskNumber Task number.
     * @param format     Output format.
     * @param ccmAddr
     * @return the commandline.
     * @throws ScmException
     */
    public static Commandline showTaskObjects( int taskNumber, String format, String ccmAddr )
        throws ScmException
    {
        // Construct the CM Synergy command
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );

        cl.createArg().setValue( TASK );
        cl.createArg().setValue( "-show" );
        cl.createArg().setValue( "objects" );

        // Set up the output format
        if ( format != null && !format.equals( "" ) )
        {
            cl.createArg().setValue( "-f" );
            cl.createArg().setValue( format );
        }

        cl.createArg().setValue( Integer.toString( taskNumber ) );

        return cl;
    }

    /**
     * Create commandline for query.
     *
     * @param query    query.
     * @param format   Output format
     * @param ccmAddr
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline query( String query, String format, String ccmAddr )
        throws ScmException
    {

        // Construct the CM Synergy command
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( QUERY );

        cl.createArg().setValue( "-u" );

        // Set up the output format
        if ( format != null && !format.equals( "" ) )
        {
            cl.createArg().setValue( "-f" );
            cl.createArg().setValue( format );
        }

        cl.createArg().setValue( query );

        return cl;
    }

    /**
     * Create command line for creating a baseline.
     *
     * @param projectSpec project_name~project_version
     * @param name        Name of the baseline
     * @param release     the release.
     * @param purpose     the purpose.
     * @param ccmAddr
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline createBaseline( String projectSpec, String name, String release, String purpose,
                                              String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( BASELINE );

        cl.createArg().setValue( "-create" );
        cl.createArg().setValue( name );

        cl.createArg().setValue( "-p" );
        cl.createArg().setValue( projectSpec );

        cl.createArg().setValue( "-release" );
        cl.createArg().setValue( release );

        cl.createArg().setValue( "-purpose" );
        cl.createArg().setValue( purpose );

        return cl;

    }

    /**
     * Create command line for adding a fileset to a project
     *
     * @param files    fileset.
     * @param message  message log, or null if none.
     * @param ccmAddr
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline create( List<File> files, String message, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( CREATE );

        if ( message != null && !message.equals( "" ) )
        {

            cl.createArg().setValue( "-c" );

            cl.createArg().setValue( message );

        }

        for ( File f : files )
        {
            try
            {
                cl.createArg().setValue( f.getCanonicalPath() );
            }
            catch ( IOException e )
            {
                throw new ScmException( "Invalid file path " + f.toString(), e );
            }
        }

        return cl;

    }

    /**
     * Create command line for creating a task
     *
     * @param synopsis    synopsis.
     * @param release     release.
     * @param defaultTask default.
     * @param ccmAddr
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline createTask( String synopsis, String release, boolean defaultTask, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( TASK );

        cl.createArg().setValue( "-create" );

        cl.createArg().setValue( "-synopsis" );
        cl.createArg().setValue( synopsis );

        if ( release != null && !release.equals( "" ) )
        {
            cl.createArg().setValue( "-release" );
            cl.createArg().setValue( release );
        }

        if ( defaultTask )
        {
            cl.createArg().setValue( "-default" );
        }

        cl.createArg().setValue( "-description" );
        cl.createArg().setValue(
            "This task was created by Maven SCM Synergy provider on " + Calendar.getInstance().getTime() );

        return cl;

    }

    /**
     * Create command line for checkin a task
     *
     * @param taskSpecs task_specs or default
     * @param comment    comment.
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline checkinTask( String taskSpecs, String comment, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( TASK );

        cl.createArg().setValue( "-checkin" );

        cl.createArg().setValue( taskSpecs );

        cl.createArg().setValue( "-comment" );
        cl.createArg().setValue( comment );

        return cl;

    }

    /**
     * Create command line for deleting file(s).
     *
     * @param files    fileset.
     * @param ccmAddr
     * @param replace  replace with previous version of file ?
     * @return
     * @throws ScmException
     */
    public static Commandline delete( List<File> files, String ccmAddr, boolean replace )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( DELETE );

        if ( replace )
        {
            cl.createArg().setValue( "-replace" );
        }

        for ( File f : files )
        {
            try
            {
                cl.createArg().setValue( f.getCanonicalPath() );
            }
            catch ( IOException e )
            {
                throw new ScmException( "Invalid file path " + f.toString(), e );
            }
        }

        return cl;

    }

    /**
     * Create commandline to reconfigure a project.
     *
     * @param projectSpec
     * @param ccmAddr
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline reconfigure( String projectSpec, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( RECONFIGURE );

        cl.createArg().setValue( "-recurse" );

        if ( projectSpec != null )
        {
            cl.createArg().setValue( "-p" );
            cl.createArg().setValue( projectSpec );
        }

        return cl;

    }

    /**
     * Create commandline to reconfigure properties of a project.
     *
     * @param projectSpec
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline reconfigureProperties( String projectSpec, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( RECONFIGURE_PROPERTIES );

        cl.createArg().setValue( "-refresh" );
        cl.createArg().setValue( projectSpec );

        return cl;

    }

    /**
     * Create command line to reconcile a project with uwa option.
     *
     * @param projectSpec
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline reconcileUwa( String projectSpec, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( RECONCILE );

        cl.createArg().setValue( "-r" );
        cl.createArg().setValue( "-uwa" ); // Update wa from database

        if ( projectSpec != null )
        {
            cl.createArg().setValue( "-p" );
            cl.createArg().setValue( projectSpec );
        }

        return cl;

    }

    /**
     * Create command line to reconcile a project with udb option.
     *
     * @param projectSpec
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline reconcileUdb( String projectSpec, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( RECONCILE );

        cl.createArg().setValue( "-r" );
        cl.createArg().setValue( "-udb" ); // Update database from wa

        if ( projectSpec != null )
        {
            cl.createArg().setValue( "-p" );
            cl.createArg().setValue( projectSpec );
        }

        return cl;

    }

    /**
     * Create command line to perform a dir on the directory.
     *
     * @param directory
     * @param format    Output format.
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline dir( File directory, String format, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        try
        {
            cl.setWorkingDirectory( directory.getCanonicalPath() );
        }
        catch ( IOException e )
        {
            throw new ScmException( "Invalid directory", e );
        }

        cl.setExecutable( CCM );
        cl.createArg().setValue( DIR );
        cl.createArg().setValue( "-m" );

        // Set up the output format
        if ( format != null && !format.equals( "" ) )
        {
            cl.createArg().setValue( "-f" );
            cl.createArg().setValue( format );
        }

        return cl;

    }

    /**
     * Create commandline to checkout a fileset.
     *
     * @param files    fileset.
     * @param ccmAddr
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline checkoutFiles( List<File> files, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( CO );

        for ( File f : files )
        {
            try
            {
                cl.createArg().setValue( f.getCanonicalPath() );
            }
            catch ( IOException e )
            {
                throw new ScmException( "Invalid file path " + f.toString(), e );
            }
        }

        return cl;
    }

    /**
     * Create commandline to checkout a project
     *
     * @param directory    target WA, or null if using default directory
     * @param projectSpec
     * @param version      new version of the project, or null if using default Synergy
     *                     mecanism
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline checkoutProject( File directory, String projectSpec, ScmVersion version, String purpose,
                                               String release, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( CO );
        cl.createArg().setValue( "-subprojects" ); // Checkout sub-projects
        cl.createArg().setValue( "-rel" ); // Relative

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            cl.createArg().setValue( "-t" ); // Version
            cl.createArg().setValue( version.getName() );
        }

        if ( purpose != null && !purpose.equals( "" ) )
        {
            cl.createArg().setValue( "-purpose" );
            cl.createArg().setValue( purpose );
        }

        if ( release != null && !release.equals( "" ) )
        {
            cl.createArg().setValue( "-release" );
            cl.createArg().setValue( release );
        }

        if ( directory != null )
        {
            cl.createArg().setValue( "-path" );
            try
            {
                cl.createArg().setValue( directory.getCanonicalPath() );
            }
            catch ( IOException e )
            {
                throw new ScmException( "Invalid directory", e );
            }
        }
        cl.createArg().setValue( "-p" );
        cl.createArg().setValue( projectSpec );

        return cl;
    }

    /**
     * Create commandline to checkin a project
     *
     * @param projectSpec
     * @param comment
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline checkinProject( String projectSpec, String comment, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( CI );
        if ( comment != null && !comment.equals( "" ) )
        {
            cl.createArg().setValue( "-c" );
            cl.createArg().setValue( comment );
        }
        cl.createArg().setValue( "-p" );
        cl.createArg().setValue( projectSpec );

        return cl;
    }

    /**
     * Create commandline to checkin a fileset
     *
     * @param files    fileset.
     * @param comment
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline checkinFiles( List<File> files, String comment, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( CI );
        if ( comment != null && !comment.equals( "" ) )
        {
            cl.createArg().setValue( "-c" );
            cl.createArg().setValue( comment );
        }

        if ( files.size() > 0 )
        {
            for ( File f : files )
            {
                try
                {
                    cl.createArg().setValue( f.getCanonicalPath() );
                }
                catch ( IOException e )
                {
                    throw new ScmException( "Invalid file path " + f.toString(), e );
                }
            }
        }
        return cl;
    }

    /**
     * Create commandline to synchronize a project
     *
     * @param projectSpec
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline synchronize( String projectSpec, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( SYNC );
        cl.createArg().setValue( "-r" ); // Recursive
        cl.createArg().setValue( "-p" );
        cl.createArg().setValue( projectSpec );

        return cl;
    }

    /**
     * Create commandline to get workarea informations for a given project.
     *
     * @param projectSpec
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline showWorkArea( String projectSpec, String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( WA );
        cl.createArg().setValue( "-show" );
        cl.createArg().setValue( projectSpec );

        return cl;
    }

    /**
     * Create commandline to stop a Synergy session
     *
     * @param ccmAddr
     * @return
     * @throws ScmException
     */
    public static Commandline stop( String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( STOP );

        return cl;
    }

    /**
     * Configure a commandline to use environment variables ($PATH)
     *
     * @param cl
     * @param ccmAddr
     * @throws ScmException
     */
    private static void configureEnvironment( Commandline cl, String ccmAddr )
        throws ScmException
    {
        // We need PATH to be set for using CCM
        try
        {
            Properties envVars = CommandLineUtils.getSystemEnvVars();

            for ( @SuppressWarnings( "rawtypes" )
            Iterator i = envVars.keySet().iterator(); i.hasNext(); )
            {
                String key = (String) i.next();

                if ( !key.equalsIgnoreCase( "CCM_ADDR" ) )
                {

                    cl.addEnvironment( key, envVars.getProperty( key ) );

                }
            }
        }
        catch ( Exception e1 )
        {
            throw new ScmException( "Fail to add PATH environment variable.", e1 );

        }
        cl.addEnvironment( "CCM_ADDR", ccmAddr );

    }

    /**
     * Create commandline to start a Synergy session
     *
     * @param username
     * @param password
     * @param role
     * @return
     * @throws ScmException
     */
    public static Commandline start( String username, String password, SynergyRole role )
        throws ScmException
    {
        Commandline cl = new Commandline();

        cl.setExecutable( CCM );
        cl.createArg().setValue( START );
        cl.createArg().setValue( "-nogui" );
        cl.createArg().setValue( "-m" ); // Multissesion
        cl.createArg().setValue( "-q" ); // Quiet (return only CCM_ADDR)
        cl.createArg().setValue( "-n" );
        cl.createArg().setValue( username );
        cl.createArg().setValue( "-pw" );
        cl.createArg().setValue( password );
        if ( role != null )
        {
            cl.createArg().setValue( "-r" );
            cl.createArg().setValue( role.toString() );
        }

        return cl;
    }

    /**
     * Create commandline to start a  remote Synergy session
     *
     * @param username
     * @param password
     * @param role
     * @return
     * @throws ScmException
     */
    public static Commandline startRemote( String username, String password, SynergyRole role )
        throws ScmException
    {
        Commandline cl = new Commandline();

        cl.setExecutable( CCM );
        cl.createArg().setValue( START );
        cl.createArg().setValue( "-nogui" );
        cl.createArg().setValue( "-m" ); // Multissesion
        cl.createArg().setValue( "-q" ); // Quiet (return only CCM_ADDR)
        cl.createArg().setValue( "-rc" ); //Remote client
        cl.createArg().setValue( "-n" );
        cl.createArg().setValue( username );
        cl.createArg().setValue( "-pw" );
        cl.createArg().setValue( password );
        if ( role != null )
        {
            cl.createArg().setValue( "-r" );
            cl.createArg().setValue( role.toString() );
        }

        return cl;
    }
	
    /**
     * Create commandline to get Synergy database delimiter
     *
     * @return
     * @throws ScmException
     */
    public static Commandline delimiter( String ccmAddr )
        throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );

        cl.setExecutable( CCM );
        cl.createArg().setValue( DELIMITER );

        return cl;
    }
    
    /**
     * Create commandline to get current (i.e. default) task
     * 
     * @param ccmAddr current Synergy session ID
     * @return 
     * @throws ScmException
     */
    public static Commandline showDefaultTask( String ccmAddr ) 
    	throws ScmException
    {
    	Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );
        cl.setExecutable( CCM );
        cl.createArg().setValue( TASK );
        cl.createArg().setValue( "-default" );
        
        return cl;
    }
    
    /**
     * Create commandline to set current (i.e. default) task
     * 
     * @param task	  the number of the task to set as current task
     * @param ccmAddr current Synergy session ID
     * @return 
     * @throws ScmException
     */
    public static Commandline setDefaultTask( int task, String ccmAddr ) 
    	throws ScmException
    {
    	Commandline cl = new Commandline();

        configureEnvironment( cl, ccmAddr );
        cl.setExecutable( CCM );
        cl.createArg().setValue( TASK );
        cl.createArg().setValue( "-default" );
        cl.createArg().setValue( String.valueOf( task ) );
        return cl;
    }
}
