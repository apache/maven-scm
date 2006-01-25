package org.apache.maven.scm.provider.clearcase.repository;

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

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ClearCaseScmProviderRepository
    extends ScmProviderRepository
{
    private boolean viewNameGivenByUser = false;

    private String viewName;

    private File configSpec;

    /**
     * @param url format is [view_name]:[url_to_configspec] or [view_name]|[url_to_configspec]
     */
    public ClearCaseScmProviderRepository( String url )
        throws ScmRepositoryException
    {
        try
        {
            parseUrl( url );
        }
        catch ( MalformedURLException e )
        {
            throw new ScmRepositoryException( "Illegal URL: " + url + "(" + e.getMessage() + ")" );
        }
        catch ( URISyntaxException e )
        {
            throw new ScmRepositoryException( "Illegal URL: " + url + "(" + e.getMessage() + ")" );
        }
        catch ( UnknownHostException e )
        {
            throw new ScmRepositoryException( "Illegal URL: " + url + "(" + e.getMessage() + ")" );
        }
    }

    private void parseUrl( String url )
        throws MalformedURLException, URISyntaxException, UnknownHostException
    {
        if ( url.indexOf( '|' ) != -1 )
        {
            StringTokenizer tokenizer = new StringTokenizer( url, "|" );
            fillInProperties( tokenizer );
        }
        else
        {
            StringTokenizer tokenizer = new StringTokenizer( url, ":" );
            fillInProperties( tokenizer );
        }
    }

    private void fillInProperties( StringTokenizer tokenizer )
        throws UnknownHostException, URISyntaxException, MalformedURLException
    {
        if ( tokenizer.countTokens() == 1 )
        {
            //No view name was given
            viewName = getDefaultViewName();
            String spec = tokenizer.nextToken();
            System.out.println( "spec = " + spec );
            configSpec = createConfigSpecFile( spec );
        }
        else
        {
            viewName = tokenizer.nextToken();
            viewNameGivenByUser = true;
            System.out.println( "viewName = " + viewName );
            String pathname = tokenizer.nextToken();
            System.out.println( "pathname = " + pathname );
            configSpec = createConfigSpecFile( pathname );
        }
    }

    private File createConfigSpecFile( String spec )
        throws URISyntaxException, MalformedURLException
    {
        File result;
        if ( spec.indexOf( ':' ) == -1 )
        {
            result = new File( spec );
        }
        else
        {
            result = new File( new URI( new URL( spec ).toString() ) );
        }
        return result;
    }

    /**
     * Default: ${hostname}-{user.name}-maven
     *
     * @return the default view name
     */
    private String getDefaultViewName()
        throws UnknownHostException
    {
        String username = System.getProperty( "user.name", "nouser" );
        String hostname = getHostName();
        return username + "-" + hostname + "-maven";
    }

    private String getHostName()
        throws UnknownHostException
    {
        return InetAddress.getLocalHost().getHostName();
    }

    /**
     * Returns the name of the view. If it is defined in the scm url, then it is returned as defined there.
     * If it is the default name, then the uniqueId is added
     *
     * @param uniqueId
     * @return the name of the view
     */
    public String getViewName( String uniqueId )
    {
        String result;
        if ( viewNameGivenByUser )
        {
            result = viewName;
        }
        else
        {
            result = viewName + "-" + uniqueId;
        }

        return result;
    }

    public File getConfigSpec()
    {
        return configSpec;
    }
}
