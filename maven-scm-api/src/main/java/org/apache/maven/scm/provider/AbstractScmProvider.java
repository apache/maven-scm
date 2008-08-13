package org.apache.maven.scm.provider;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.NoSuchCommandScmException;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.log.ScmLogDispatcher;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractScmProvider
    implements ScmProvider
{
    private ScmLogDispatcher logDispatcher = new ScmLogDispatcher();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public String getScmSpecificFilename()
    {
        return null;
    }

    /** {@inheritDoc} */
    public String sanitizeTagName( String tag )
    {
        /* by default, we assume all tags are valid. */
        return tag;
    }

    /** {@inheritDoc} */
    public boolean validateTagName( String tag )
    {
        /* by default, we assume all tags are valid. */
        return true;
    }

    /** {@inheritDoc} */
    public List validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        List messages = new ArrayList();

        try
        {
            makeProviderScmRepository( scmSpecificUrl, delimiter );
        }
        catch ( ScmRepositoryException e )
        {
            messages.add( e.getMessage() );
        }

        return messages;
    }

    /** {@inheritDoc} */
    public boolean requiresEditMode()
    {
        return false;
    }

    // ----------------------------------------------------------------------
    // Scm Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return add( repository, fileSet, (String) null );
    }

    /** {@inheritDoc} */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.MESSAGE, message == null ? "" : message );

        // TODO: binary may be dependant on particular files though
        // TODO: set boolean?
        parameters.setString( CommandParameter.BINARY, "false" );

        return add( repository.getProviderRepository(), fileSet, parameters );
    }

    protected AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "add" );
    }

    /** {@inheritDoc} */
    public BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName )
        throws ScmException
    {
        return branch( repository, fileSet, branchName, null );
    }

    /** {@inheritDoc} */
    public BranchScmResult branch( ScmRepository repository, ScmFileSet fileSet, String branchName, String message )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.BRANCH_NAME, branchName );

        if ( StringUtils.isNotEmpty( message ) )
        {
            parameters.setString( CommandParameter.MESSAGE, message );
        }

        return branch( repository.getProviderRepository(), fileSet, parameters );
    }

    protected BranchScmResult branch( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "branch" );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch )
        throws ScmException
    {
        return changeLog( repository, fileSet, startDate, endDate, numDays, branch, null );
    }

    /**
     * {@inheritDoc}
     * @deprecated
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch, String datePattern )
        throws ScmException
    {
        ScmBranch scmBranch = null;

        if ( StringUtils.isNotEmpty( branch ) )
        {
            scmBranch = new ScmBranch( branch );
        }
        return changeLog( repository, fileSet, startDate, endDate, numDays, scmBranch, null );

    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, ScmBranch branch )
        throws ScmException
    {
        return changeLog( repository, fileSet, startDate, endDate, numDays, branch, null );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, ScmBranch branch, String datePattern )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setDate( CommandParameter.START_DATE, startDate );

        parameters.setDate( CommandParameter.END_DATE, endDate );

        parameters.setInt( CommandParameter.NUM_DAYS, numDays );

        parameters.setScmVersion( CommandParameter.BRANCH, branch );

        parameters.setString( CommandParameter.CHANGELOG_DATE_PATTERN, datePattern );

        return changelog( repository.getProviderRepository(), fileSet, parameters );
    }


    /** {@inheritDoc}
     * @deprecated
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag )
        throws ScmException
    {
        return changeLog( repository, fileSet, startTag, endTag, null );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag,
                                         String datePattern )
        throws ScmException
    {
        ScmVersion startRevision = null;
        ScmVersion endRevision = null;

        if ( StringUtils.isNotEmpty( startTag ) )
        {
            startRevision = new ScmRevision( startTag );
        }

        if ( StringUtils.isNotEmpty( endTag ) )
        {
            endRevision = new ScmRevision( endTag );
        }

        return changeLog( repository, fileSet, startRevision, endRevision, null );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startVersion,
                                         ScmVersion endVersion )
        throws ScmException
    {
        return changeLog( repository, fileSet, startVersion, endVersion, null );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, ScmVersion startVersion,
                                         ScmVersion endVersion, String datePattern )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setScmVersion( CommandParameter.START_SCM_VERSION, startVersion );

        parameters.setScmVersion( CommandParameter.END_SCM_VERSION, endVersion );

        parameters.setString( CommandParameter.CHANGELOG_DATE_PATTERN, datePattern );

        return changelog( repository.getProviderRepository(), fileSet, parameters );
    }

    protected ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                            CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "changelog" );
    }


    /** {@inheritDoc}
     * @deprecated
     */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String tag, String message )
        throws ScmException
    {
        ScmVersion scmVersion = null;

        if ( StringUtils.isNotEmpty( tag ) )
        {
            scmVersion = new ScmBranch( tag );
        }

        return checkIn( repository, fileSet, scmVersion, message );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        return checkIn( repository, fileSet, (ScmVersion) null, message );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion,
                                     String message )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setScmVersion( CommandParameter.SCM_VERSION, scmVersion );

        parameters.setString( CommandParameter.MESSAGE, message );

        return checkin( repository.getProviderRepository(), fileSet, parameters );
    }

    protected CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "checkin" );
    }


    /** {@inheritDoc}
     * @deprecated
     */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return checkOut( repository, fileSet, tag, true );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, String tag, boolean recursive )
        throws ScmException
    {
        ScmVersion scmVersion = null;

        if ( StringUtils.isNotEmpty( tag ) )
        {
            scmVersion = new ScmBranch( tag );
        }

        return checkOut( repository, fileSet, scmVersion, recursive );
    }

     /** {@inheritDoc} */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return checkOut( repository, fileSet, (ScmVersion) null, true );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion )
        throws ScmException
    {
        return checkOut( repository, fileSet, scmVersion, true );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, boolean recursive )
        throws ScmException
    {
        return checkOut( repository, fileSet, (ScmVersion) null, recursive );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion,
                                       boolean recursive )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setScmVersion( CommandParameter.SCM_VERSION, scmVersion );

        parameters.setString( CommandParameter.RECURSIVE, recursive + "" );

        return checkout( repository.getProviderRepository(), fileSet, parameters );
    }

    protected CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                          CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "checkout" );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, String startRevision, String endRevision )
        throws ScmException
    {
        ScmVersion startVersion = null;
        ScmVersion endVersion = null;

        if ( StringUtils.isNotEmpty( startRevision ) )
        {
            startVersion = new ScmRevision( startRevision );
        }

        if ( StringUtils.isNotEmpty( endRevision ) )
        {
            endVersion = new ScmRevision( endRevision );
        }

        return diff( repository, fileSet, startVersion, endVersion );
    }

    /** {@inheritDoc} */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, ScmVersion startVersion,
                               ScmVersion endVersion )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setScmVersion( CommandParameter.START_SCM_VERSION, startVersion );

        parameters.setScmVersion( CommandParameter.END_SCM_VERSION, endVersion );

        return diff( repository.getProviderRepository(), fileSet, parameters );
    }

    protected DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "diff" );
    }

    /** {@inheritDoc} */
    public EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        return edit( repository.getProviderRepository(), fileSet, parameters );
    }

    protected EditScmResult edit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        this.getLogger().warn( "Provider " + this.getScmType() + " does not support edit operation." );

        return new EditScmResult( "", null, null, true );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return export( repository, fileSet, tag, null );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, String tag, String outputDirectory )
        throws ScmException
    {
        ScmVersion scmVersion = null;

        if ( StringUtils.isNotEmpty( tag ) )
        {
            scmVersion = new ScmRevision( tag );
        }

        return export( repository, fileSet, scmVersion, outputDirectory );
    }

    /** {@inheritDoc} */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return export( repository, fileSet, (ScmVersion) null, null );
    }

    /** {@inheritDoc} */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion )
        throws ScmException
    {
        return export( repository, fileSet, scmVersion, null );
    }

    /** {@inheritDoc} */
    public ExportScmResult export( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion,
                                   String outputDirectory )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setScmVersion( CommandParameter.SCM_VERSION, scmVersion );

        parameters.setString( CommandParameter.OUTPUT_DIRECTORY, outputDirectory );

        return export( repository.getProviderRepository(), fileSet, parameters );
    }

    protected ExportScmResult export( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "export" );
    }

    /** {@inheritDoc} */
    public ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, String tag )
        throws ScmException
    {
        ScmVersion scmVersion = null;

        if ( StringUtils.isNotEmpty( tag ) )
        {
            scmVersion = new ScmRevision( tag );
        }

        return list( repository, fileSet, recursive, scmVersion );
    }

    /** {@inheritDoc} */
    public ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, ScmVersion scmVersion )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.RECURSIVE, Boolean.toString( recursive ) );

        if ( scmVersion != null )
        {
            parameters.setScmVersion( CommandParameter.SCM_VERSION, scmVersion );
        }

        return list( repository.getProviderRepository(), fileSet, parameters );
    }

    /**
     * List each element (files and directories) of <B>fileSet</B> as they exist in the repository.
     *
     * @param repository the source control system
     * @param fileSet    the files to list
     * @param parameters
     * @return The list of files in the repository
     * @throws NoSuchCommandScmException unless overriden by subclass
     * @throws ScmException
     */
    protected ListScmResult list( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "list" );
    }

    private void login( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        LoginScmResult result = login( repository.getProviderRepository(), fileSet, new CommandParameters() );

        if ( !result.isSuccess() )
        {
            throw new ScmException( "Can't login.\n" + result.getCommandOutput() );
        }
    }

    protected LoginScmResult login( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return new LoginScmResult( null, null, null, true );
    }

    /** {@inheritDoc} */
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.MESSAGE, message == null ? "" : message );

        return remove( repository.getProviderRepository(), fileSet, parameters );
    }

    protected RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "remove" );
    }

    /** {@inheritDoc} */
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        return status( repository.getProviderRepository(), fileSet, parameters );
    }

    protected StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "status" );
    }

    /** {@inheritDoc} */
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tagName )
        throws ScmException
    {
        return tag( repository, fileSet, tagName, null );
    }

    /** {@inheritDoc} */
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tagName, String message )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG_NAME, tagName );

        if ( StringUtils.isNotEmpty( message ) )
        {
            parameters.setString( CommandParameter.MESSAGE, message );
        }

        return tag( repository.getProviderRepository(), fileSet, parameters );
    }

    protected TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "tag" );
    }

    /** {@inheritDoc} */
    public UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        return unedit( repository.getProviderRepository(), fileSet, parameters );
    }

    protected UnEditScmResult unedit( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        this.getLogger().warn( "Provider " + this.getScmType() + " does not support unedit operation." );

        return new UnEditScmResult( "", null, null, true );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return update( repository, fileSet, tag, true );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, boolean runChangelog )
        throws ScmException
    {
        return update( repository, fileSet, tag, "", runChangelog );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#update(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return update( repository, fileSet, (ScmVersion) null, true );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion )
        throws ScmException
    {
        return update( repository, fileSet, scmVersion, true );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, boolean runChangelog )
        throws ScmException
    {
        return update( repository, fileSet, (ScmVersion) null, "", runChangelog );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion,
                                   boolean runChangelog )
        throws ScmException
    {
        return update( repository, fileSet, scmVersion, "", runChangelog );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, String datePattern )
        throws ScmException
    {
        return update( repository, fileSet, tag, datePattern, true );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion,
                                   String datePattern )
        throws ScmException
    {
        return update( repository, fileSet, scmVersion, datePattern, true );
    }

    /**
     * @deprecated
     */
    private UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, String datePattern,
                                    boolean runChangelog )
        throws ScmException
    {
        ScmBranch scmBranch = null;

        if ( StringUtils.isNotEmpty( tag ) )
        {
            scmBranch = new ScmBranch( tag );
        }

        return update( repository, fileSet, scmBranch, datePattern, runChangelog );
    }

    private UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion,
                                    String datePattern, boolean runChangelog )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setScmVersion( CommandParameter.SCM_VERSION, scmVersion );

        parameters.setString( CommandParameter.CHANGELOG_DATE_PATTERN, datePattern );

        parameters.setString( CommandParameter.RUN_CHANGELOG_WITH_UPDATE, String.valueOf( runChangelog ) );

        return update( repository.getProviderRepository(), fileSet, parameters );
    }

    /** {@inheritDoc}
     * @deprecated
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate )
        throws ScmException
    {
        return update( repository, fileSet, tag, lastUpdate, null );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion,
                                   Date lastUpdate )
        throws ScmException
    {
        return update( repository, fileSet, scmVersion, lastUpdate, null );
    }
    /** {@inheritDoc}
     * @deprecated
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate,
                                   String datePattern )
        throws ScmException
    {
        ScmBranch scmBranch = null;

        if ( StringUtils.isNotEmpty( tag ) )
        {
            scmBranch = new ScmBranch( tag );
        }

        return update( repository, fileSet, scmBranch, lastUpdate, datePattern );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, ScmVersion scmVersion, Date lastUpdate,
                                   String datePattern )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setScmVersion( CommandParameter.SCM_VERSION, scmVersion );

        if ( lastUpdate != null )
        {
            parameters.setDate( CommandParameter.START_DATE, lastUpdate );
        }

        parameters.setString( CommandParameter.CHANGELOG_DATE_PATTERN, datePattern );

        parameters.setString( CommandParameter.RUN_CHANGELOG_WITH_UPDATE, "true" );

        return update( repository.getProviderRepository(), fileSet, parameters );
    }

    protected UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "update" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void addListener( ScmLogger logger )
    {
        logDispatcher.addListener( logger );
    }

    public ScmLogger getLogger()
    {
        return logDispatcher;
    }

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        throw new UnknownRepositoryStructure();
    }
}
