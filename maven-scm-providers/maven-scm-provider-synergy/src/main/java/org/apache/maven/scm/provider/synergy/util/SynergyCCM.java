package org.apache.maven.scm.provider.synergy.util;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * This class contains methods to execute Synergy ccm command line.
 * 
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
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
     * @param taskNumber
     *            Task number.
     * @param format
     *            Output format.
     * @param CCM_ADDR
     * @return the commandl ine.
     * @throws ScmException
     */
    public static Commandline showTaskObjects( int taskNumber, String format, String CCM_ADDR ) throws ScmException
    {
        // Construct the CM Synergy command
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );

        cl.createArgument().setValue( TASK );
        cl.createArgument().setValue( "-show" );
        cl.createArgument().setValue( "objects" );

        // Set up the output format
        if ( format != null && !format.equals( "" ) )
        {
            cl.createArgument().setValue( "-f" );
            cl.createArgument().setValue( format );
        }

        cl.createArgument().setValue( Integer.toString( taskNumber ) );

        return cl;
    }

    /**
     * Create commandline for query.
     * 
     * @param query
     *            query.
     * @param format
     *            Output format
     * @param CCM_ADDR
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline query( String query, String format, String CCM_ADDR ) throws ScmException
    {

        // Construct the CM Synergy command
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( QUERY );

        cl.createArgument().setValue( "-u" );

        // Set up the output format
        if ( format != null && !format.equals( "" ) )
        {
            cl.createArgument().setValue( "-f" );
            cl.createArgument().setValue( format );
        }

        cl.createArgument().setValue( query );

        return cl;
    }

    /**
     * Create command line for creating a baseline.
     * 
     * @param projectSpec
     *            project_name~project_version
     * @param name
     *            Name of the baseline
     * @param release
     *            the release.
     * @param purpose
     *            the purpose.
     * @param CCM_ADDR
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline createBaseline( String projectSpec, String name, String release, String purpose,
            String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( BASELINE );

        cl.createArgument().setValue( "-create" );
        cl.createArgument().setValue( name );

        cl.createArgument().setValue( "-p" );
        cl.createArgument().setValue( projectSpec );

        cl.createArgument().setValue( "-release" );
        cl.createArgument().setValue( release );

        cl.createArgument().setValue( "-purpose" );
        cl.createArgument().setValue( purpose );

        return cl;

    }

    /**
     * Create command line for adding a fileset to a project
     * 
     * @param files
     *            fileset.
     * @param message
     *            message log, or null if none.
     * @param CCM_ADDR
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline create( List files, String message, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( CREATE );

        if ( message != null && !message.equals( "" ) )
        {

            cl.createArgument().setValue( "-c" );

            cl.createArgument().setValue( message );

        }

        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            File f = ( File ) i.next();
            try
            {
                cl.createArgument().setValue( f.getCanonicalPath() );
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
     * @param synopsis
     *            synopsis.
     * @param release
     *            release.
     * @param defaultTask
     *            default.
     * @param CCM_ADDR
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline createTask( String synopsis, String release, boolean defaultTask, String CCM_ADDR )
            throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( TASK );

        cl.createArgument().setValue( "-create" );

        cl.createArgument().setValue( "-synopsis" );
        cl.createArgument().setValue( synopsis );

        if ( release != null && !release.equals( "" ) )
        {
            cl.createArgument().setValue( "-release" );
            cl.createArgument().setValue( release );
        }

        if ( defaultTask )
        {
            cl.createArgument().setValue( "-default" );
        }

        cl.createArgument().setValue( "-description" );
        cl.createArgument().setValue(
                "This task was created by Maven SCM Synergy provider on " + Calendar.getInstance().getTime() );

        return cl;

    }

    /**
     * Create command line for checkin a task
     * 
     * @param task_specs
     *            task_specs or default
     * @param comment
     *            comment.
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline checkinTask( String task_specs, String comment, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( TASK );

        cl.createArgument().setValue( "-checkin" );

        cl.createArgument().setValue( task_specs );

        cl.createArgument().setValue( "-comment" );
        cl.createArgument().setValue( comment );

        return cl;

    }

    /**
     * Create command line for deleting file(s).
     * 
     * @param files
     *            fileset.
     * @param CCM_ADDR
     * @param replace
     *            replace with previous version of file ?
     * @return
     * @throws ScmException
     */
    public static Commandline delete( List files, String CCM_ADDR, boolean replace ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( DELETE );

        if ( replace )
        {
            cl.createArgument().setValue( "-replace" );
        }

        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            File f = ( File ) i.next();
            try
            {
                cl.createArgument().setValue( f.getCanonicalPath() );
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
     * @param project_spec
     * @param CCM_ADDR
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline reconfigure( String project_spec, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( RECONFIGURE );

        cl.createArgument().setValue( "-recurse" );

        if ( project_spec != null )
        {
            cl.createArgument().setValue( "-p" );
            cl.createArgument().setValue( project_spec );
        }

        return cl;

    }

    /**
     * Create commandline to reconfigure properties of a project.
     * 
     * @param project_spec
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline reconfigureProperties( String project_spec, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( RECONFIGURE_PROPERTIES );

        cl.createArgument().setValue( "-refresh" );
        cl.createArgument().setValue( project_spec );

        return cl;

    }

    /**
     * Create command line to reconcile a project with uwa option.
     * 
     * @param project_spec
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline reconcileUwa( String project_spec, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( RECONCILE );

        cl.createArgument().setValue( "-r" );
        cl.createArgument().setValue( "-uwa" ); // Update wa from database

        if ( project_spec != null )
        {
            cl.createArgument().setValue( "-p" );
            cl.createArgument().setValue( project_spec );
        }

        return cl;

    }

    /**
     * Create command line to reconcile a project with udb option.
     * 
     * @param project_spec
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline reconcileUdb( String project_spec, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( RECONCILE );

        cl.createArgument().setValue( "-r" );
        cl.createArgument().setValue( "-udb" ); // Update database from wa

        if ( project_spec != null )
        {
            cl.createArgument().setValue( "-p" );
            cl.createArgument().setValue( project_spec );
        }

        return cl;

    }

    /**
     * Create command line to perform a dir on the directory.
     * 
     * @param directory
     * @param format
     *            Output format.
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline dir( File directory, String format, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        try
        {
            cl.setWorkingDirectory( directory.getCanonicalPath() );
        }
        catch ( IOException e )
        {
            throw new ScmException( "Invalid directory", e );
        }

        cl.setExecutable( CCM );
        cl.createArgument().setValue( DIR );
        cl.createArgument().setValue( "-m" );

        // Set up the output format
        if ( format != null && !format.equals( "" ) )
        {
            cl.createArgument().setValue( "-f" );
            cl.createArgument().setValue( format );
        }

        return cl;

    }

    /**
     * Create commandline to checkout a fileset.
     * 
     * @param files
     *            fileset.
     * @param CCM_ADDR
     * @return the command line.
     * @throws ScmException
     */
    public static Commandline checkoutFiles( List files, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( CO );

        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            File f = ( File ) i.next();
            try
            {
                cl.createArgument().setValue( f.getCanonicalPath() );
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
     * @param directory
     *            target WA, or null if using default directory
     * @param project_spec
     * @param version
     *            new version of the project, or null if using default Synergy
     *            mecanism
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline checkoutProject( File directory, String project_spec, String version, String purpose,
            String release, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( CO );
        cl.createArgument().setValue( "-subprojects" ); // Checkout sub-projects
        cl.createArgument().setValue( "-rel" ); // Relative

        if ( version != null && !version.equals( "" ) )
        {
            cl.createArgument().setValue( "-t" ); // Version
            cl.createArgument().setValue( version );
        }

        if ( purpose != null && !purpose.equals( "" ) )
        {
            cl.createArgument().setValue( "-purpose" );
            cl.createArgument().setValue( purpose );
        }

        if ( release != null && !release.equals( "" ) )
        {
            cl.createArgument().setValue( "-release" );
            cl.createArgument().setValue( release );
        }

        if ( directory != null )
        {
            cl.createArgument().setValue( "-path" );
            try
            {
                cl.createArgument().setValue( directory.getCanonicalPath() );
            }
            catch ( IOException e )
            {
                throw new ScmException( "Invalid directory", e );
            }
        }
        cl.createArgument().setValue( "-p" );
        cl.createArgument().setValue( project_spec );

        return cl;
    }

    /**
     * Create commandline to checkin a project
     * 
     * @param project_spec
     * @param comment
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline checkinProject( String project_spec, String comment, String CCM_ADDR )
            throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( CI );
        if ( comment != null && !comment.equals( "" ) )
        {
            cl.createArgument().setValue( "-c" );
            cl.createArgument().setValue( comment );
        }
        cl.createArgument().setValue( "-p" );
        cl.createArgument().setValue( project_spec );

        return cl;
    }

    /**
     * Create commandline to checkin a fileset
     * 
     * @param files
     *            fileset.
     * @param comment
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline checkinFiles( List files, String comment, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( CI );
        if ( comment != null && !comment.equals( "" ) )
        {
            cl.createArgument().setValue( "-c" );
            cl.createArgument().setValue( comment );
        }

        if ( files.size() > 0 )
        {
            for ( Iterator i = files.iterator(); i.hasNext(); )
            {
                File f = ( File ) i.next();
                try
                {
                    cl.createArgument().setValue( f.getCanonicalPath() );
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
     * @param project_spec
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline synchronize( String project_spec, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( SYNC );
        cl.createArgument().setValue( "-r" ); // Recursive
        cl.createArgument().setValue( "-p" );
        cl.createArgument().setValue( project_spec );

        return cl;
    }

    /**
     * Create commandline to get workarea informations for a given project.
     * 
     * @param project_spec
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline showWorkArea( String project_spec, String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( WA );
        cl.createArgument().setValue( "-show" );
        cl.createArgument().setValue( project_spec );

        return cl;
    }

    /**
     * Create commandline to stop a Synergy session
     * 
     * @param CCM_ADDR
     * @return
     * @throws ScmException
     */
    public static Commandline stop( String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( STOP );

        return cl;
    }

    /**
     * Configure a commandline to use environement variables ($PATH)
     * 
     * @param cl
     * @param CCM_ADDR
     * @throws ScmException
     */
    private static void configureEnvironment( Commandline cl, String CCM_ADDR ) throws ScmException
    {
        // We need PATH to be set for using CCM
        try
        {
            Properties envVars = CommandLineUtils.getSystemEnvVars();

            for ( Iterator i = envVars.keySet().iterator(); i.hasNext(); )
            {
                String key = ( String ) i.next();

                if ( !key.toUpperCase().equals( "CCM_ADDR" ) )
                {

                    cl.addEnvironment( key, envVars.getProperty( key ) );

                }
            }
        }
        catch ( Exception e1 )
        {
            throw new ScmException( "Fail to add PATH environment variable.", e1 );

        }
        cl.addEnvironment( "CCM_ADDR", CCM_ADDR );

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
    public static Commandline start( String username, String password, SynergyRole role ) throws ScmException
    {
        Commandline cl = new Commandline();

        cl.setExecutable( CCM );
        cl.createArgument().setValue( START );
        cl.createArgument().setValue( "-nogui" );
        cl.createArgument().setValue( "-m" ); // Multissesion
        cl.createArgument().setValue( "-q" ); // Quiet (return only CCM_ADDR)
        cl.createArgument().setValue( "-n" );
        cl.createArgument().setValue( username );
        cl.createArgument().setValue( "-pw" );
        cl.createArgument().setValue( password );
        if ( role != null )
        {
            cl.createArgument().setValue( "-r" );
            cl.createArgument().setValue( role.toString() );
        }

        return cl;
    }

    /**
     * Create commandline to get Synergy database delimiter
     * 
     * @return
     * @throws ScmException
     */
    public static Commandline delimiter( String CCM_ADDR ) throws ScmException
    {
        Commandline cl = new Commandline();

        configureEnvironment( cl, CCM_ADDR );

        cl.setExecutable( CCM );
        cl.createArgument().setValue( DELIMITER );

        return cl;
    }

}
