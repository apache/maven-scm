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
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
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

    public String getScmSpecificFilename()
    {
        return null;
    }

    /* (non-Javadoc)
    * @see org.apache.maven.scm.provider.ScmProvider#sanitizeTagName(java.lang.String)
    */
    public String sanitizeTagName( String tag )
    {
        /* by default, we assume all tags are valid. */
        return tag;
    }

    /* (non-Javadoc)
    * @see org.apache.maven.scm.provider.ScmProvider#validateTagName(java.lang.String)
    */
    public boolean validateTagName( String tag )
    {
        /* by default, we assume all tags are valid. */
        return true;
    }

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

    public boolean requiresEditMode()
    {
        return false;
    }

    // ----------------------------------------------------------------------
    // Scm Implementation
    // ----------------------------------------------------------------------

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#add(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet)
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        return add( repository, fileSet, (String) null );
    }


    /**
     * @see org.apache.maven.scm.provider.ScmProvider#add(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,Stringmessage)
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.MESSAGE, message == null ? "" : message );

        // TODO: binary may be dependant on particular files though
        // TODO: set boolean?
        parameters.setString( CommandParameter.BINARY, "false" );

        return add( repository, fileSet, parameters );
    }

    protected AddScmResult add( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "add" );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#changeLog(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.util.Date,java.util.Date,int,java.lang.String)
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch )
        throws ScmException
    {
        return changeLog( repository, fileSet, startDate, endDate, numDays, branch, null );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#changeLog(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.util.Date,java.util.Date,int,java.lang.String,java.lang.String)
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch, String datePattern )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setDate( CommandParameter.START_DATE, startDate );

        parameters.setDate( CommandParameter.END_DATE, endDate );

        parameters.setInt( CommandParameter.NUM_DAYS, numDays );

        parameters.setString( CommandParameter.BRANCH, branch );

        parameters.setString( CommandParameter.CHANGELOG_DATE_PATTERN, datePattern );

        return changelog( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#changeLog(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String,java.lang.String)
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag )
        throws ScmException
    {
        return changeLog( repository, fileSet, startTag, endTag, null );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#changeLog(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String,java.lang.String,java.lang.String)
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, String startTag, String endTag,
                                         String datePattern )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.START_TAG, startTag );

        parameters.setString( CommandParameter.END_TAG, endTag );

        parameters.setString( CommandParameter.CHANGELOG_DATE_PATTERN, datePattern );

        return changelog( repository, fileSet, parameters );
    }

    protected ChangeLogScmResult changelog( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "changelog" );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#checkIn(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String,java.lang.String)
     */
    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String tag, String message )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        parameters.setString( CommandParameter.MESSAGE, message );

        return checkin( repository, fileSet, parameters );
    }

    protected CheckInScmResult checkin( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "checkin" );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#checkOut(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String)
     */
    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        return checkout( repository, fileSet, parameters );
    }

    protected CheckOutScmResult checkout( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "checkout" );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#diff(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String,java.lang.String)
     */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, String startRevision, String endRevision )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.START_REVISION, startRevision );

        parameters.setString( CommandParameter.END_REVISION, endRevision );

        return diff( repository, fileSet, parameters );
    }

    protected DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "diff" );
    }

    private void login( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        LoginScmResult result = login( repository, fileSet, new CommandParameters() );

        if ( !result.isSuccess() )
        {
            throw new ScmException( "Can't login.\n" + result.getCommandOutput() );
        }
    }

    protected LoginScmResult login( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return new LoginScmResult( null, null, null, true );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#remove(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String)
     */
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.MESSAGE, message == null ? "" : message );

        return remove( repository, fileSet, parameters );
    }

    protected RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "remove" );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#status(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet)
     */
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        return status( repository, fileSet, parameters );
    }

    protected StatusScmResult status( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "status" );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#tag(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String)
     */
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        return tag( repository, fileSet, parameters );
    }

    protected TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "tag" );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#update(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        return update( repository, fileSet, tag, "" );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#update(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String,java.lang.String)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, String datePattern )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        parameters.setString( CommandParameter.CHANGELOG_DATE_PATTERN, datePattern );

        return update( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#update(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String,java.util.Date)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate )
        throws ScmException
    {
        return update( repository, fileSet, tag, lastUpdate, null );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#update(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,java.lang.String,java.util.Date,java.lang.String)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate,
                                   String datePattern )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        if ( lastUpdate != null )
        {
            parameters.setDate( CommandParameter.START_DATE, lastUpdate );
        }

        parameters.setString( CommandParameter.CHANGELOG_DATE_PATTERN, datePattern );

        return update( repository, fileSet, parameters );
    }

    protected UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "update" );
    }

    public EditScmResult edit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        return edit( repository, fileSet, parameters );
    }

    protected EditScmResult edit( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        this.getLogger().warn( "Provider " + repository.getProvider() + " does not support edit operation." );

        return new EditScmResult( "", null, null, true );
    }

    public UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        return unedit( repository, fileSet, parameters );
    }

    protected UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        this.getLogger().warn( "Provider " + repository.getProvider() + " does not support unedit operation." );

        return new UnEditScmResult( "", null, null, true );
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
    protected ListScmResult list( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "list" );
    }

    /**
     * Calls {@link #list(ScmRepository,ScmFileSet,CommandParameters)} setting the {@link CommandParameters} with
     * the necessary values from <code>recursive</code> and <code>tag</code>.
     *
     * @see #list(ScmRepository,ScmFileSet,CommandParameters)
     */
    public ListScmResult list( ScmRepository repository, ScmFileSet fileSet, boolean recursive, String tag )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.RECURSIVE, Boolean.toString( recursive ) );

        if ( StringUtils.isNotEmpty( tag ) )
        {
            parameters.setString( CommandParameter.TAG, tag );
        }

        return list( repository, fileSet, parameters );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#addListener(org.apache.maven.scm.log.ScmLogger)
     */
    public void addListener( ScmLogger logger )
    {
        logDispatcher.addListener( logger );
    }

    public ScmLogger getLogger()
    {
        return logDispatcher;
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#makeProviderScmRepository(java.io.File)
     */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        throw new UnknownRepositoryStructure();
    }
}
