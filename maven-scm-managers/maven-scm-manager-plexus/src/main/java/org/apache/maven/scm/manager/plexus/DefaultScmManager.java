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
import java.util.HashMap;
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

    private PlexusLogger logger;

    private final static String ILLEGAL_SCM_URL = "The scm url must be on the form "
                                                  + "'scm:<scm provider><delimiter><provider specific part>' "
                                                  + "where <delimiter> can be either ':' or '|'.";

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

    public ScmProvider getProviderByUrl( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        if ( scmUrl == null )
        {
            throw new NullPointerException( "The scm url cannot be null." );
        }

        char delimiter = findDelimiter( scmUrl );

        String providerType = scmUrl.substring( 4, scmUrl.indexOf( delimiter, 4 ) );

        return getProviderByType( providerType );
    }

    public ScmProvider getProviderByType( String providerType )
        throws NoSuchScmProviderException
    {
        ScmProvider scmProvider = (ScmProvider) scmProviders.get( providerType );

        if ( scmProvider == null )
        {
            throw new NoSuchScmProviderException( providerType );
        }

        if ( logger == null )
        {
            logger = new PlexusLogger( getLogger() );

            scmProvider.addListener( logger );
        }

        return scmProvider;
    }

    public ScmProvider getProviderByRepository( ScmRepository repository )
        throws NoSuchScmProviderException
    {
        return getProviderByType( repository.getProvider() );
    }

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

        ScmProvider provider = getProviderByType( providerType );

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

        ScmProvider provider = getProviderByType( providerType );

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
            provider = getProviderByType( providerType );
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
}
