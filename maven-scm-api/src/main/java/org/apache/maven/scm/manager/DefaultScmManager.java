package org.apache.maven.scm.manager;

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

import org.apache.maven.scm.CommandNameConstants;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
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
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class DefaultScmManager
    extends AbstractLogEnabled
    implements ScmManager, Initializable
{
    private Map scmProviders;

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void initialize()
        throws Exception
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

        if ( !scmUrl.startsWith( "scm" ) )
        {
            throw new ScmRepositoryException( "The scm url must start with 'scm'." );
        }

        if ( scmUrl.length() < 6 )
        {
            throw new ScmRepositoryException( "The scm url must be on the form 'scm:<scm provider>:'." );
        }

        String scmUrlTmp = scmUrl.substring( 4 );

        String providerType = null;

        String delimiter = null;

        for ( Iterator iter = scmProviders.values().iterator(); iter.hasNext(); )
        {
            String providerName = ( (AbstractScmProvider) iter.next() ).getScmType();

            if ( scmUrlTmp.startsWith( providerName ) )
            {
                providerType = providerName;

                delimiter = scmUrlTmp.substring( providerName.length(), providerName.length() + 1 );

                break;
            }
        }

        ScmProvider provider = getScmProvider( providerType );

        String scmSpecificUrl = scmUrl.substring( providerType.length() + 5 );

        ScmProviderRepository providerRepository = provider.makeProviderScmRepository( scmSpecificUrl, delimiter );

        return new ScmRepository( providerType, providerRepository );
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

    public ChangeLogScmResult changeLog( ScmRepository repository, ScmFileSet fileSet, Date startDate, Date endDate, int numDays, String branch )
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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private  ScmResult execute( String commandName, ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        ScmProvider scmProvider = getScmProvider( repository.getProvider() );

        return scmProvider.execute( commandName, repository.getProviderRepository(), fileSet, parameters );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

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
            throw new ScmException( "Internal error: Wrong ScmResult returned. Expected: " + clazz.getName() + ", got: " + scmResult.getClass().getName() );
        }

        return scmResult;
    }
}
