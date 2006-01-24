package org.apache.maven.scm.manager;

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

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.ArrayList;
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
public abstract class AbstractScmManager
    implements ScmManager
{
    private final static String ILLEGAL_SCM_URL = "The scm url must be on the form " +
        "'scm:<scm provider><delimiter><provider specific part>' " +
        "where <delimiter> can be either ':' or '|'.";

    private Map scmProviders = new HashMap();

    private ScmLogger logger;

    protected void setScmProviders( Map providers )
    {
        this.scmProviders = providers;
    }

    protected void addScmProvider( String providerType, ScmProvider provider )
    {
        scmProviders.put( providerType, provider );
    }

    protected abstract ScmLogger getScmLogger();

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
        if ( logger == null )
        {
            logger = getScmLogger();

            for ( Iterator i = scmProviders.keySet().iterator(); i.hasNext(); )
            {
                String key = (String) i.next();

                ScmProvider p = (ScmProvider) scmProviders.get( key );

                p.addListener( logger );
            }
        }

        ScmProvider scmProvider = (ScmProvider) scmProviders.get( providerType );

        if ( scmProvider == null )
        {
            throw new NoSuchScmProviderException( providerType );
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

        String scmSpecificUrl = cleanScmUrl( scmUrl.substring( providerType.length() + 5 ) );

        ScmProviderRepository providerRepository = provider.makeProviderScmRepository( scmSpecificUrl, delimiter );

        return new ScmRepository( providerType, providerRepository );
    }

    protected String cleanScmUrl( String scmUrl )
    {
        if ( scmUrl == null )
        {
            throw new NullPointerException( "The scm url cannot be null." );
        }

        String pathSeparator = "";

        int indexOfDoubleDot = -1;

        // Clean Unix path
        if ( scmUrl.indexOf( "../" ) > 1 )
        {
            pathSeparator = "/";

            indexOfDoubleDot = scmUrl.indexOf( "../" );
        }

        // Clean windows path
        if ( scmUrl.indexOf( "..\\" ) > 1 )
        {
            pathSeparator = "\\";

            indexOfDoubleDot = scmUrl.indexOf( "..\\" );
        }

        if ( indexOfDoubleDot > 1 )
        {
            int startOfTextToRemove = scmUrl.substring( 0, indexOfDoubleDot - 1 ).lastIndexOf( pathSeparator );

            String beginUrl = "";
            if ( startOfTextToRemove >= 0 )
            {
                beginUrl = scmUrl.substring( 0, startOfTextToRemove );
            }

            String endUrl = scmUrl.substring( indexOfDoubleDot + 3 );

            scmUrl = beginUrl + pathSeparator + endUrl;

            // Check if we have other double dot
            if ( scmUrl.indexOf( "../" ) > 1 || scmUrl.indexOf( "..\\" ) > 1 )
            {
                scmUrl = cleanScmUrl( scmUrl );
            }
        }

        return scmUrl;
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

        String scmSpecificUrl = cleanScmUrl( scmUrl.substring( providerType.length() + 5 ) );

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
