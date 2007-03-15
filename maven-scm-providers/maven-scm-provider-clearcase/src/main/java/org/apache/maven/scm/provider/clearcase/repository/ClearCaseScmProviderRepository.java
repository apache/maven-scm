package org.apache.maven.scm.provider.clearcase.repository;

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

import org.apache.maven.scm.log.ScmLogger;
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
 * Provider Repository
 * <p/>
 * Url format is [view_name]:[configspec|] or [view_name]|[configspec]
 * <p/>
 * [configspec] can be used in two different ways:
 * <ul>
 * <li>Path to a config spec file that is
 * used when creating the snapshot view, e.g.
 * "\\myserver\clearcase\configspecs\my_module.txt", or:</li>
 * <li>A load rule that is used to automatically create a config spec, e.g. "load /MY_VOB/my/project/dir"</li>
 * </ul>
 * Notice that checking out from a tag is currently only supported when the second option is used.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id: ClearCaseScmProviderRepository.java 483105 2006-12-06 15:07:54Z
 *          evenisse $
 */
public class ClearCaseScmProviderRepository
    extends ScmProviderRepository
{
    private ScmLogger logger;

    private boolean viewNameGivenByUser = false;

    private String viewName;

    /**
     * The user-specified config spec; may be null.
     */
    private File configSpec;

    /**
     * The directory to be loaded, when auto-generating the config spec.
     */
    private String loadDirectory;

    public ClearCaseScmProviderRepository( ScmLogger logger, String url )
        throws ScmRepositoryException
    {
        this.logger = logger;
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

        String configSpecString;
        if ( tokenizer.countTokens() == 1 )
        {
            //No view name was given
            viewName = getDefaultViewName();
            configSpecString = tokenizer.nextToken();
        }
        else
        {
            viewName = tokenizer.nextToken();
            if ( viewName.length() > 0 )
            {
                viewNameGivenByUser = true;
            }
            else
            {
                viewName = getDefaultViewName();
            }
            configSpecString = tokenizer.nextToken();
        }
        logger.info( "viewName = '" + viewName + "' ; configSpec = '" + configSpecString + "'" );
        if ( !configSpecString.startsWith( "load " ) )
        {
            configSpec = createConfigSpecFile( configSpecString );
            loadDirectory = null;
        }
        else
        {
            configSpec = null;
            loadDirectory = configSpecString.substring( 5 );

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

    /**
     * Returns the user-supplied config spec or <code>null</code> in case it
     * should be automatically generated
     *
     * @return File or <code>null</code>
     * @see #isAutoConfigSpec()
     */
    public File getConfigSpec()
    {
        return configSpec;
    }

    /**
     * Returns true when the config spec has not been supplied by the user, but
     * instead should automatically be generated by the plugin
     *
     * @return true if auto config spec
     */
    public boolean isAutoConfigSpec()
    {
        return configSpec == null;
    }

    /**
     * Returns the VOB directory to be loaded when auto-generating the config
     * spec.
     *
     * @return <code>null</code> when isAutoConfigSpec() returns false;
     *         otherwise the VOB directory
     */
    public String getLoadDirectory()
    {
        return loadDirectory;
    }
}
