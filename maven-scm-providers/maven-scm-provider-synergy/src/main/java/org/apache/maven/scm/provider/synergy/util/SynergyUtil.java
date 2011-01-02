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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.synergy.consumer.SynergyCreateTaskConsumer;
import org.apache.maven.scm.provider.synergy.consumer.SynergyGetCompletedTasksConsumer;
import org.apache.maven.scm.provider.synergy.consumer.SynergyGetTaskObjectsConsumer;
import org.apache.maven.scm.provider.synergy.consumer.SynergyGetWorkingFilesConsumer;
import org.apache.maven.scm.provider.synergy.consumer.SynergyGetWorkingProjectConsumer;
import org.apache.maven.scm.provider.synergy.consumer.SynergyWorkareaConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * This class contains functional methodsfor Synergy.
 *
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class SynergyUtil
{

    /**
     * Separator used with formatted result
     */
    public static final String SEPARATOR = "#####";

    /**
     * Remove prefix path from a path. Example: removeParent("c:\tmp",
     * "c:\tmp\foo.bar") returns "foo.bar"
     *
     * @param prefix parent path (prefix).
     * @param file   file path.
     * @return suffix
     * @throws ScmException if parent is not a prefix of file
     */
    public static String removePrefix( File prefix, File file )
        throws ScmException
    {
        try
        {
            String prefixStr = prefix.getCanonicalPath();
            String fileStr = file.getCanonicalPath();
            if ( !fileStr.startsWith( prefixStr ) )
            {
                throw new ScmException( prefixStr + " is not a prefix of " + fileStr );
            }
            return fileStr.substring( prefixStr.length() );
        }
        catch ( IOException e )
        {
            throw new ScmException( "IOException", e );
        }

    }

    /**
     * Get a working project whose predecessor is given.
     *
     * @param logger       a logger.
     * @param projectSpec predecessor (prep project)
     * @param username     owner of working project
     * @param ccmAddr      Synergy session ID.
     * @return projectSpec of the working checkout, or null if none
     */
    public static String getWorkingProject( ScmLogger logger, String projectSpec, String username, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering getWorkingProject method" );
        }

        String query =
            "owner='" + username + "' and status='working' and type='project' and has_predecessor('" + projectSpec +"')";
                //+ ":project:1')"; SCM-261

        Commandline cl = SynergyCCM.query( query, "%objectname", ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        SynergyGetWorkingProjectConsumer stdout = new SynergyGetWorkingProjectConsumer( logger );

        int errorCode = executeSynergyCommand( logger, cl, stderr, stdout, false );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : getWorkingProject returns " + stdout.getProjectSpec() + " with code "
                + errorCode );
        }

        return stdout.getProjectSpec();
    }

    /**
     * Get working file(s) in a given project.
     *
     * @param logger       a logger.
     * @param projectSpec (project)
     * @param release      release
     * @param ccmAddr      Synergy session ID.
     * @return list of working files.
     */
    public static List<String> getWorkingFiles( ScmLogger logger, String projectSpec, String release, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering getWorkingFiles method" );
        }

        String query = "status='working' and release='" + release + "' and is_member_of('" + projectSpec + "')";

        Commandline cl = SynergyCCM.query( query, SynergyGetWorkingFilesConsumer.OUTPUT_FORMAT, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        SynergyGetWorkingFilesConsumer stdout = new SynergyGetWorkingFilesConsumer( logger );

        int errorCode = executeSynergyCommand( logger, cl, stderr, stdout, false );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : getWorkingFiles returns " + stdout.getFiles().size() + " files with code "
                + errorCode );
        }

        return stdout.getFiles();
    }

    /**
     * Populate the object list of a Modification by querying for objects
     * associated with the task.
     *
     * @param logger  a logger.
     * @param numTask task number.
     * @param ccmAddr Synergy session ID.
     */
    public static List<ChangeFile> getModifiedObjects( ScmLogger logger, int numTask, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering getModifiedObjects method" );
        }

        Commandline cl = SynergyCCM.showTaskObjects( numTask, SynergyGetTaskObjectsConsumer.OUTPUT_FORMAT, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        SynergyGetTaskObjectsConsumer stdout = new SynergyGetTaskObjectsConsumer( logger );
        int errorCode = executeSynergyCommand( logger, cl, stderr, stdout, false );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : getModifiedObjects returns " + stdout.getFiles().size() + " files with code "
                + errorCode );
        }

        return stdout.getFiles();

    }

    /**
     * Get a list of all tasks which are contained in all folders in the
     * reconfigure properties of the specified project and were completed after
     * startDate and before endDate.
     *
     * @param logger      a logger.
     * @param projectSpec projectSpec.
     * @param startDate   start date.
     * @param endDate     end date.
     * @param ccmAddr     Synergy session ID.
     * @return A list of  {@link SynergyTask}
     */
    public static List<SynergyTask> getCompletedTasks( ScmLogger logger, String projectSpec, Date startDate, Date endDate,
                                          String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering getCompletedTasks method" );
        }

        // The format used for converting Java dates into CM Synergy dates
        // Note that the format used to submit commands differs from the
        // format used in the results of that command!?!
        SimpleDateFormat toCcmDate = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss", new Locale( "en", "US" ) );

        // Construct the query string
        String query = "is_task_in_folder_of(is_folder_in_rp_of('" + projectSpec + "'))";
        if ( startDate != null )
        {
            query = query + "and completion_date>time('" + toCcmDate.format( startDate ) + "')";
        }
        if ( endDate != null )
        {
            query = query + "and completion_date<time('" + toCcmDate.format( endDate ) + "')";
        }

        Commandline cl = SynergyCCM.query( query, SynergyGetCompletedTasksConsumer.OUTPUT_FORMAT, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        SynergyGetCompletedTasksConsumer stdout = new SynergyGetCompletedTasksConsumer( logger );

        executeSynergyCommand( logger, cl, stderr, stdout, false );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : getCompletedTasks method returns " + stdout.getTasks().size() + " tasks" );
        }

        return stdout.getTasks();
    }

    /**
     * Create a baseline.
     *
     * @param logger      a logger.
     * @param projectSpec the projectSpec.
     * @param name        name of the baseline.
     * @param release     the release.
     * @param purpose     the purpose.
     * @param ccmAddr     used to run in multi-session.
     * @throws ScmException
     */
    public static void createBaseline( ScmLogger logger, String projectSpec, String name, String release,
                                       String purpose, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering createBaseline method" );
        }

        Commandline cl = SynergyCCM.createBaseline( projectSpec, name, release, purpose, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Add new file to Synergy database.
     *
     * @param logger  a logger.
     * @param file    file to be added.
     * @param message log message for Synergy.
     * @param ccmAddr used to run in multi-session.
     * @throws ScmException
     */
    public static void create( ScmLogger logger, File file, String message, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering create method" );
        }

        List<File> files = new ArrayList<File>();
        files.add( file );
        Commandline cl = SynergyCCM.create( files, message, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Create new task.
     *
     * @param logger      a logger.
     * @param synopsis    short description of task.
     * @param release     release.
     * @param defaultTask should this task become the default task?
     * @param ccmAddr     used to run in multi-session.
     * @return Task number
     * @throws ScmException
     */
    public static int createTask( ScmLogger logger, String synopsis, String release, boolean defaultTask,
                                  String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering createTask method" );
        }

        if ( synopsis == null || synopsis.equals( "" ) )
        {
            throw new ScmException( "A synopsis must be specified to create a task." );
        }

        Commandline cl = SynergyCCM.createTask( synopsis, release, defaultTask, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        SynergyCreateTaskConsumer stdout = new SynergyCreateTaskConsumer( logger );

        executeSynergyCommand( logger, cl, stderr, stdout, true );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "createTask returns " + stdout.getTask() );
        }

        return stdout.getTask();
    }

    /**
     * Checkin the default task.
     *
     * @param logger  a logger.
     * @param comment a comment.
     * @param ccmAddr Synergy session ID.
     * @throws ScmException
     */
    public static void checkinDefaultTask( ScmLogger logger, String comment, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering checkinDefaultTask method" );
        }

        Commandline cl = SynergyCCM.checkinTask( "default", comment, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Checkin a task.
     *
     * @param logger     a logger.
     * @param taskNumber task number.
     * @param comment    a comment.
     * @param ccmAddr    Synergy session ID.
     * @throws ScmException
     */
    public static void checkinTask( ScmLogger logger, int taskNumber, String comment, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering checkinTask method" );
        }

        Commandline cl = SynergyCCM.checkinTask( "" + taskNumber, comment, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Delete file from Synergy database.
     *
     * @param logger  a logger.
     * @param file    file to be deleted.
     * @param ccmAddr used to run in multi-session.
     * @throws ScmException
     */
    public static void delete( ScmLogger logger, File file, String ccmAddr, boolean replace )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering delete method" );
        }

        List<File> list = new ArrayList<File>();
        list.add( file );

        Commandline cl = SynergyCCM.delete( list, ccmAddr, replace );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Reconfigure a project.
     *
     * @param logger       a logger.
     * @param projectSpec projectSpec (i.e. myProject~1).
     * @param ccmAddr      used to run in multi-session.
     * @throws ScmException
     */
    public static void reconfigure( ScmLogger logger, String projectSpec, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering reconfigure method" );
        }
        Commandline cl = SynergyCCM.reconfigure( projectSpec, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Reconfigure properties of a project.
     *
     * @param logger       a logger.
     * @param projectSpec projectSpec (i.e. myProject~1).
     * @param ccmAddr      used to run in multi-session.
     * @throws ScmException
     */
    public static void reconfigureProperties( ScmLogger logger, String projectSpec, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering reconfigureProperties method" );
        }
        Commandline cl = SynergyCCM.reconfigureProperties( projectSpec, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Reconcile a project with -uwa option.
     *
     * @param logger       a logger.
     * @param projectSpec projectSpec (i.e. myProject~1).
     * @param ccmAddr      used to run in multi-session.
     * @throws ScmException
     */
    public static void reconcileUwa( ScmLogger logger, String projectSpec, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering reconcileUwa method" );
        }
        Commandline cl = SynergyCCM.reconcileUwa( projectSpec, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Reconcile a project with -udb option.
     *
     * @param logger       a logger.
     * @param projectSpec projectSpec (i.e. myProject~1).
     * @param ccmAddr      used to run in multi-session.
     * @throws ScmException
     */
    public static void reconcileUdb( ScmLogger logger, String projectSpec, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering reconcileUdb method" );
        }
        Commandline cl = SynergyCCM.reconcileUdb( projectSpec, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Checkout given files or directory.
     *
     * @param logger  a logger.
     * @param files   files to add.
     * @param ccmAddr Synergy session ID.
     * @throws ScmException
     */
    public static void checkoutFiles( ScmLogger logger, List<File> files, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering checkoutFiles files method" );
        }

        Commandline cl = SynergyCCM.checkoutFiles( files, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Checkout a given project.
     *
     * @param logger       a logger.
     * @param directory    new project work area, or null if you want to use default wa.
     * @param projectSpec projectSpec (i.e. myProject~1).
     * @param ccmAddr      used to run in multi-session.
     * @return checkout directory (directory + new project spec)
     * @throws ScmException
     */
    public static void checkoutProject( ScmLogger logger, File directory, String projectSpec, ScmVersion version,
                                        String purpose, String release, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering checkoutProject project method" );
        }

        Commandline cl = SynergyCCM.checkoutProject( directory, projectSpec, version, purpose, release, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );

    }

    /**
     * Checkin a given project.
     *
     * @param logger       a logger.
     * @param projectSpec projectSpec (i.e. myProject~1).
     * @param comment      message.
     * @param ccmAddr      used to run in multi-session.
     * @return checkout directory (directory + new project spec)
     * @throws ScmException
     */
    public static void checkinProject( ScmLogger logger, String projectSpec, String comment, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering checkinProject project method" );
        }

        Commandline cl = SynergyCCM.checkinProject( projectSpec, comment, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );

    }

    /**
     * Checkin a file set.
     *
     * @param logger  a logger.
     * @param ccmAddr used to run in multi-session.
     * @return checkout directory (directory + new project spec)
     * @throws ScmException
     */
    public static void checkinFiles( ScmLogger logger, List<File> files, String comment, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering checkinFiles project method" );
        }

        Commandline cl = SynergyCCM.checkinFiles( files, comment, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );

    }

    /**
     * Synchronize a given project.
     *
     * @param logger       a logger.
     * @param projectSpec projectSpec (i.e. myProject~1).
     * @param ccmAddr      used to run in multi-session.
     * @throws ScmException
     */
    public static void synchronize( ScmLogger logger, String projectSpec, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering synchronize method" );
        }

        Commandline cl = SynergyCCM.synchronize( projectSpec, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Get the work area of a given project.
     *
     * @param logger       a logger.
     * @param projectSpec projectSpec (i.e. myProject~1).
     * @param ccmAddr      used to run in multi-session.
     * @throws ScmException
     */
    public static File getWorkArea( ScmLogger logger, String projectSpec, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering getWorkArea method" );
        }

        Commandline cl = SynergyCCM.showWorkArea( projectSpec, ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        SynergyWorkareaConsumer stdout = new SynergyWorkareaConsumer( logger );

        executeSynergyCommand( logger, cl, stderr, stdout, true );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : getWorkArea returns " + stdout.getWorkAreaPath() );
        }

        return stdout.getWorkAreaPath();
    }

    /**
     * Stop a ccm session.
     *
     * @param logger  a logger.
     * @param ccmAddr used to run in multi-session.
     * @throws ScmException
     */
    public static void stop( ScmLogger logger, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering stop method" );
        }
        Commandline cl = SynergyCCM.stop( ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );
    }

    /**
     * Start a session Synergy
     *
     * @param logger   a logger.
     * @param username username.
     * @param password password.
     * @param role     role or null if none.
     * @return ccmAddr value to use with this session.
     */
    public static String start( ScmLogger logger, String username, String password, SynergyRole role )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering start method" );
        }

        if ( username == null )
        {
            throw new ScmException( "username can't be null" );
        }

        if ( password == null )
        {
            throw new ScmException( "password can't be null" );
        }

        Commandline cl = SynergyCCM.start( username, password, role );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        //executeSynergyCommand( logger, cl, stderr, stdout, true );
		
        int exitCode = executeSynergyCommand( logger, cl, stderr, stdout, false );
		
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : start returns with error code " + exitCode );
        }		
		
        if ( exitCode != 0 ) 
        {
            cl = SynergyCCM.startRemote( username, password, role );
            
            stderr = new CommandLineUtils.StringStreamConsumer();
            stdout = new CommandLineUtils.StringStreamConsumer();			
            
            executeSynergyCommand( logger, cl, stderr, stdout, true );			
        }

        return stdout.getOutput();
    }

    /**
     * Get Database delimiter
     *
     * @param logger  a logger.
     * @param ccmAddr Synergy session ID.
     * @return delimiter of the database (i.e. ~).
     */
    public static String delimiter( ScmLogger logger, String ccmAddr )
        throws ScmException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Synergy : Entering delimiter method" );
        }

        Commandline cl = SynergyCCM.delimiter( ccmAddr );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        executeSynergyCommand( logger, cl, stderr, stdout, true );

        return stdout.getOutput();
    }

    /**
     * Execute a command line for Synergy.
     *
     * @param logger      a logger.
     * @param cl          command line.
     * @param stderr      stderr.
     * @param stdout      stdout.
     * @param failOnError should we raise an exception when exit code != 0
     * @return exit code.
     * @throws ScmException on error or if exit code != 0 and failOnError = true
     */
    protected static int executeSynergyCommand( ScmLogger logger, Commandline cl, StringStreamConsumer stderr,
                                                StreamConsumer stdout, boolean failOnError )
        throws ScmException
    {
        int exitCode;

        try
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Executing: " + cl.toString() );
            }
            exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing synergy command [" + cl.toString() + "].", ex );
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Exit code :" + exitCode );
        }
        if ( stdout instanceof StringStreamConsumer )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "STDOUT :" + ( (StringStreamConsumer) stdout ).getOutput() );
            }
        }
        else
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "STDOUT : unavailable" );
            }
        }
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "STDERR :" + stderr.getOutput() );
        }

        if ( exitCode != 0 && failOnError )
        {
            if ( stdout instanceof StringStreamConsumer )
            {
                throw new ScmException( "Commandeline = " + cl.toString() + "\nSTDOUT = "
                    + ( (StringStreamConsumer) stdout ).getOutput() + "\nSTDERR = " + stderr.getOutput() + "\n" );
            }
            else
            {
                throw new ScmException( "Commandeline = " + cl.toString() + "\nSTDOUT = unavailable" + "\nSTDERR = "
                    + stderr.getOutput() + "\n" );
            }
        }

        return exitCode;
    }

}
