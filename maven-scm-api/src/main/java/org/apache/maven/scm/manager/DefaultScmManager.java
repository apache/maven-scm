package org.apache.maven.scm.manager;

/*
 * ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation. Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * ====================================================================
 */

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.scm.CommandNameConstants;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
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

//        char delimiter = scmUrl.charAt( 3 );

//        repository.setDelimiter( delimiter );

        int index = scmUrl.indexOf( ":", 4 );

        if ( index <= 0 )
        {
            throw new ScmRepositoryException( "The scm url must be on the form 'scm:<scm provider>:'." );
        }

        String providerType = scmUrl.substring( 4, index );

        String scmSpecificUrl = scmUrl.substring( index + 1 );

        ScmProviderRepository providerRepository = getScmProvider( providerType ).makeProviderScmRepository( scmSpecificUrl );

        return new ScmRepository( providerType, scmSpecificUrl, providerRepository );
	}

    // ----------------------------------------------------------------------
    // Scm commands
    // ----------------------------------------------------------------------

    public CheckOutScmResult checkOut( ScmRepository repository, File workingDirectory, String tag )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        ScmResult scmResult = execute( CommandNameConstants.CHECK_OUT, repository, workingDirectory, parameters );

        return (CheckOutScmResult) checkScmResult( CheckOutScmResult.class, scmResult );
    }

    public CheckInScmResult checkIn( ScmRepository repository, File workingDirectory, String tag, String message )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        parameters.setString( CommandParameter.MESSAGE, message );

        ScmResult scmResult = execute( CommandNameConstants.CHECK_IN, repository, workingDirectory, parameters );

        return (CheckInScmResult) checkScmResult( CheckInScmResult.class, scmResult );
    }

    public UpdateScmResult update( ScmRepository repository, File workingDirectory, String tag )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.TAG, tag );

        ScmResult scmResult = execute( CommandNameConstants.UPDATE, repository, workingDirectory, parameters );

        return (UpdateScmResult) checkScmResult( UpdateScmResult.class, scmResult );
    }

    public ChangeLogScmResult changeLog( ScmRepository repository, File workingDirectory, Date startDate, Date endDate, int numDays, String branch )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setDate( CommandParameter.START_DATE, startDate );

        parameters.setDate( CommandParameter.END_DATE, endDate );

        parameters.setString( CommandParameter.BRANCH, branch );

        ScmResult scmResult = execute( CommandNameConstants.CHANGE_LOG, repository, workingDirectory, parameters );

        return (ChangeLogScmResult) checkScmResult( ChangeLogScmResult.class, scmResult );
    }

    // ----------------------------------------------------------------------
    // 
    // ----------------------------------------------------------------------

    private  ScmResult execute( String commandName, ScmRepository repository, File workingDirectory, CommandParameters parameters )
        throws ScmException
    {
        ScmProvider scmProvider = getScmProvider( repository.getProvider() );

        return scmProvider.execute( commandName, repository.getProviderRepository(), workingDirectory, parameters );
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
