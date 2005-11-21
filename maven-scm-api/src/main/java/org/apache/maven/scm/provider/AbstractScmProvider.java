package org.apache.maven.scm.provider;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.maven.scm.*;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.log.ScmLogDispatcher;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.login.LoginScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

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

    // ----------------------------------------------------------------------
    // Scm Implementation
    // ----------------------------------------------------------------------

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#add(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet)
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        // TODO: is message reasonable?
        parameters.setString( CommandParameter.MESSAGE, "" );

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
     * @see org.apache.maven.scm.provider.ScmProvider#changeLog(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, java.util.Date, java.util.Date, int, java.lang.String)
     */
    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setDate( CommandParameter.START_DATE, startDate );

        parameters.setDate( CommandParameter.END_DATE, endDate );

        parameters.setString( CommandParameter.BRANCH, branch );

        return changelog( repository, fileSet, parameters );
    }

    protected ChangeLogScmResult changelog( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "changelog" );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#checkIn(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, java.lang.String, java.lang.String)
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
     * @see org.apache.maven.scm.provider.ScmProvider#checkOut(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, java.lang.String)
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
     * @see org.apache.maven.scm.provider.ScmProvider#diff(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, java.lang.String, java.lang.String)
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
        LoginScmResult result = new LoginScmResult( null, null, null, true );

        return result;
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#remove(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, java.lang.String)
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
     * @see org.apache.maven.scm.provider.ScmProvider#status(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet)
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
     * @see org.apache.maven.scm.provider.ScmProvider#tag(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, java.lang.String)
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
     * @see org.apache.maven.scm.provider.ScmProvider#update(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, java.lang.String)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        return update( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.ScmProvider#update(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, java.lang.String, java.util.Date)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag, Date lastUpdate )
        throws ScmException
    {
        login( repository, fileSet );

        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        if ( lastUpdate != null )
        {
            parameters.setDate( CommandParameter.START_DATE, lastUpdate );
        }

        return update( repository, fileSet, parameters );
    }

    protected UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        throw new NoSuchCommandScmException( "update" );
    }

	public EditScmResult edit( ScmRepository repository, ScmFileSet fileSet ) throws ScmException
	{
		login( repository, fileSet );

		CommandParameters parameters = new CommandParameters();

		return edit( repository, fileSet, parameters );
	}

	protected EditScmResult edit( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters ) throws ScmException
	{
		return new EditScmResult( "", null, null, true );
	}

	public UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet ) throws ScmException
	{
		login( repository, fileSet );

		CommandParameters parameters = new CommandParameters();

		return unedit( repository, fileSet, parameters );
	}

	protected UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters ) throws ScmException
	{
		return new UnEditScmResult( "", null, null, true );
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
     * @see org.apache.maven.scm.provider.ScmProvider#makeProviderScmRepository(java.io.File, java.lang.String)
     */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        throw new UnknownRepositoryStructure();
    }
}
