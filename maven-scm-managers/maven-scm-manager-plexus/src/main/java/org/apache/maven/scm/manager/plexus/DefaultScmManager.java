package org.apache.maven.scm.manager.plexus;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.CommandNameConstants;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.NoSuchCommandScmException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultScmManager
    extends AbstractLogEnabled
    implements ScmManager, Initializable
{
    private Map scmProviders;

    private List loggers = new ArrayList();

    private final static String ILLEGAL_SCM_URL = "The scm url must be on the form " +
                                                  "'scm:<scm provider><delimiter><provider specific part>' " +
                                                  "where <delimiter> can be either ':' or '|'.";

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
    {
        if ( scmProviders == null )
        {
            scmProviders = new HashMap();
        }

        if ( scmProviders.size() == 0 )
        {
            getLogger().warn( "No SCM providers configured." );
        }
    }

    // ----------------------------------------------------------------------
    // ScmManager Implementation
    // ----------------------------------------------------------------------

    // ----------------------------------------------------------------------
    // Repository
    // ----------------------------------------------------------------------

    public ScmRepository makeScmRepository( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        if ( scmUrl == null )
        {
            throw new NullPointerException( "The scm url cannot be null." );
        }

        char delimiter = findDelimiter( scmUrl );

        String providerType = scmUrl.substring( 4, scmUrl.indexOf( delimiter, 4 ) );

        ScmProvider provider = getScmProvider( providerType );

        String scmSpecificUrl = scmUrl.substring( providerType.length() + 5 );

        ScmProviderRepository providerRepository = provider.makeProviderScmRepository( scmSpecificUrl, delimiter );

        return new ScmRepository( providerType, providerRepository );
    }

    public ScmRepository makeProviderScmRepository( String providerType, File path )
        throws ScmRepositoryException, UnknownRepositoryStructure, NoSuchScmProviderException
    {
        if ( providerType == null )
        {
            throw new NullPointerException( "The provider type cannot be null." );
        }

        ScmProvider provider = getScmProvider( providerType );

        ScmProviderRepository providerRepository = provider.makeProviderScmRepository( path );

        return new ScmRepository( providerType, providerRepository );
    }

    public List validateScmRepository( String scmUrl )
    {
        List messages = new ArrayList();

        if ( scmUrl == null )
        {
            throw new NullPointerException( "The scm url cannot be null." );
        }

        if ( !scmUrl.startsWith( "scm:" ) )
        {
            messages.add( "The scm url must start with 'scm:'." );

            return messages;
        }

        if ( scmUrl.length() < 6 )
        {
            messages.add( ILLEGAL_SCM_URL );

            return messages;
        }

        // TODO: don't assume that the delimiter is eitgher ':' or '|' or
        // require the scm delimiter to be ':' or '|'

        char delimiter;

        try
        {
            delimiter = findDelimiter( scmUrl );
        }
        catch ( ScmRepositoryException e )
        {
            messages.add( e.getMessage() );

            return messages;
        }

        String providerType = scmUrl.substring( 4, scmUrl.indexOf( delimiter, 4 ) );

        ScmProvider provider;

        try
        {
            provider = getScmProvider( providerType );
        }
        catch ( NoSuchScmProviderException e )
        {
            messages.add( "No such provider installed '" + providerType + "'." );

            return messages;
        }

        String scmSpecificUrl = scmUrl.substring( providerType.length() + 5 );

        List providerMessages = provider.validateScmUrl( scmSpecificUrl, delimiter );

        if ( providerMessages == null )
        {
            throw new RuntimeException( "The SCM provider cannot return null from validateScmUrl()." );
        }

        messages.addAll( providerMessages );

        return messages;
    }

    // ----------------------------------------------------------------------
    // Scm commands
    // ----------------------------------------------------------------------

    public CheckOutScmResult checkOut( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        ScmResult scmResult = execute( CommandNameConstants.CHECK_OUT, repository, fileSet, parameters );

        return (CheckOutScmResult) checkScmResult( CheckOutScmResult.class, scmResult );
    }

    public CheckInScmResult checkIn( ScmRepository repository, ScmFileSet fileSet, String tag, String message )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        parameters.setString( CommandParameter.MESSAGE, message );

        ScmResult scmResult = execute( CommandNameConstants.CHECK_IN, repository, fileSet, parameters );

        return (CheckInScmResult) checkScmResult( CheckInScmResult.class, scmResult );
    }

    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        ScmResult scmResult = execute( CommandNameConstants.TAG, repository, fileSet, parameters );

        return (TagScmResult) checkScmResult( TagScmResult.class, scmResult );
    }

    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        ScmResult scmResult = execute( CommandNameConstants.UPDATE, repository, fileSet, parameters );

        return (UpdateScmResult) checkScmResult( UpdateScmResult.class, scmResult );
    }

    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, String startRevision, String endRevision )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.START_REVISION, startRevision );

        parameters.setString( CommandParameter.END_REVISION, endRevision );

        ScmResult scmResult = execute( CommandNameConstants.DIFF, repository, fileSet, parameters );

        return (DiffScmResult) checkScmResult( DiffScmResult.class, scmResult );
    }

    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate,
                                         int numDays, String branch )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setDate( CommandParameter.START_DATE, startDate );

        parameters.setDate( CommandParameter.END_DATE, endDate );

        parameters.setString( CommandParameter.BRANCH, branch );

        ScmResult scmResult = execute( CommandNameConstants.CHANGE_LOG, repository, fileSet, parameters );

        return (ChangeLogScmResult) checkScmResult( ChangeLogScmResult.class, scmResult );
    }

    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        ScmResult scmResult = execute( CommandNameConstants.STATUS, repository, fileSet, parameters );

        return (StatusScmResult) checkScmResult( StatusScmResult.class, scmResult );
    }

    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        // TODO: is message reasonable?
        parameters.setString( CommandParameter.MESSAGE, "" );

        // TODO: binary may be dependant on particular files though
        // TODO: set boolean?
        parameters.setString( CommandParameter.BINARY, "false" );

        ScmResult scmResult = execute( CommandNameConstants.ADD, repository, fileSet, parameters );

        return (AddScmResult) checkScmResult( AddScmResult.class, scmResult );
    }

    public void addListener( ScmLogger logger )
        throws NoSuchScmProviderException
    {
        loggers.add( logger );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private  ScmResult execute( String commandName, ScmRepository repository, ScmFileSet fileSet,
                                CommandParameters parameters )
        throws ScmException
    {
        if ( !CommandNameConstants.LOGIN.equals( commandName ) )
        {
            try
            {
                execute( CommandNameConstants.LOGIN, repository, fileSet, parameters );
            }
            catch ( NoSuchCommandScmException e )
            {
                // ignored : provider doesn't have a login command
            }
        }

        ScmProvider scmProvider = getScmProvider( repository.getProvider() );

        for ( Iterator i = loggers.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            scmProvider.addListener( logger );
        }

        return scmProvider.execute( commandName, repository.getProviderRepository(), fileSet, parameters );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private char findDelimiter( String scmUrl )
        throws ScmRepositoryException
    {
        scmUrl = scmUrl.substring( 4 );

        int index = scmUrl.indexOf( '|' );

        if ( index == -1 )
        {
            index = scmUrl.indexOf( ':' );

            if ( index == -1 )
            {
                throw new ScmRepositoryException( ILLEGAL_SCM_URL );
            }
        }

        return scmUrl.charAt( index );
    }

    private ScmProvider getScmProvider( String providerType )
        throws NoSuchScmProviderException
    {
        ScmProvider scmProvider = (ScmProvider) scmProviders.get( providerType );

        if ( scmProvider == null )
        {
            throw new NoSuchScmProviderException( providerType );
        }

        return scmProvider;
    }

    private ScmResult checkScmResult( Class clazz, ScmResult scmResult )
        throws ScmException
    {
        if ( !clazz.isAssignableFrom( scmResult.getClass() ) )
        {
            throw new ScmException( "Internal error: Wrong ScmResult returned. " +
                                    "Expected: " + clazz.getName() + ". " +
                                    "Got: " + scmResult.getClass().getName() );
        }

        return scmResult;
    }
}
