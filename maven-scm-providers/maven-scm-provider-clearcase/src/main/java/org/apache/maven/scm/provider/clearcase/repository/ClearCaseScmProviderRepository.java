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
import org.apache.maven.scm.providers.clearcase.settings.Settings;
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
 * Provider Repository for ClearCase (standard, LT, UCM)
 * <p />
 * Url format for ClearCase and ClearCaseLT : <br />
 * [view_name]:[configspec] or [view_name]|[configspec]
 * <p />
 * Url format for ClearCaseUCM : <br />
 * [view_name]|[configspec]|[vob_name]|[stream_name] or [view_name]:[configspec]:[vob_name]:[stream_name]
 * <p />
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

    /**
     * Describe the stream linked to the view. Only used with ClearCaseUCM
     */
    private String streamName;

    /**
     * Describe the vob containing the stream. Only used with ClearCaseUCM
     */
    private String vobName;

    /**
     * Provider configuration settings
     */
    private Settings settings;

    /**
     * Describe the Element Name
     */
    private String elementName;

    /**
     * Define the flag used in the clearcase-settings.xml when using ClearCaseLT
     */
    public static final String CLEARCASE_LT = "LT";

    /**
     * Define the flag used in the clearcase-settings.xml when using ClearCaseUCM
     */
    public static final String CLEARCASE_UCM = "UCM";

    /**
     * Define the default value from the clearcase-settings.xml when using ClearCase
     */
    public static final String CLEARCASE_DEFAULT = null;

    public ClearCaseScmProviderRepository( ScmLogger logger, String url, Settings settings )
        throws ScmRepositoryException
    {
        this.logger = logger;
        this.settings = settings;
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
        String configSpecString = null;

        if ( CLEARCASE_UCM.equals( settings.getClearcaseType() ) )
        {
            configSpecString = fillUCMProperties( tokenizer );
        }
        else
        {
            configSpecString = fillDefaultProperties( tokenizer );
        }

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

    private String fillDefaultProperties( StringTokenizer tokenizer )
        throws UnknownHostException
    {
        int tokenNumber = tokenizer.countTokens();
        String configSpecString;
        if ( tokenNumber == 1 )
        {
            // No view name was given
            viewName = getDefaultViewName();
            configSpecString = tokenizer.nextToken();
        }
        else
        {
            configSpecString = checkViewName( tokenizer );
            checkUnexpectedParameter( tokenizer, tokenNumber, 2 );
        }
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "viewName = '" + viewName + "' ; configSpec = '" + configSpecString + "'" );
        }
        return configSpecString;
    }

    private String fillUCMProperties( StringTokenizer tokenizer )
        throws UnknownHostException, MalformedURLException
    {
        int tokenNumber = tokenizer.countTokens();
        if ( tokenNumber <= 2 )
        {
            throw new MalformedURLException( "ClearCaseUCM need more parameters. Expected url format : "
                + "[view_name]|[configspec]|[vob_name]|[stream_name]" );
        }

        String configSpecString;
        if ( tokenNumber == 3 )
        {
            // No view name was given
            viewName = getDefaultViewName();
            configSpecString = tokenizer.nextToken();
            vobName = tokenizer.nextToken();
            streamName = tokenizer.nextToken();
        }
        else if ( tokenNumber == 4 )
        {
            String[] tokens = new String[4];
            tokens[0] = tokenizer.nextToken();
            tokens[1] = tokenizer.nextToken();
            tokens[2] = tokenizer.nextToken();
            tokens[3] = tokenizer.nextToken();

            if ( tokens[3].startsWith( "/main/" ) )
            {
                viewName = getDefaultViewName();
                configSpecString = tokens[0];
                vobName = tokens[1];
                streamName = tokens[2];
                elementName = tokens[3];
            }
            else
            {
                viewName = tokens[0];
                viewNameGivenByUser = true;
                configSpecString = tokens[1];
                vobName = tokens[2];
                streamName = tokens[3];
            }
        }
        else
        {
            configSpecString = checkViewName( tokenizer );
            vobName = tokenizer.nextToken();
            streamName = tokenizer.nextToken();
            elementName = tokenizer.nextToken();
            checkUnexpectedParameter( tokenizer, tokenNumber, 5 );
        }

        if ( logger.isInfoEnabled() )
        {
            logger.info( "viewName = '" + viewName + "' ; configSpec = '" + configSpecString + "' ; vobName = '"
                + vobName + "' ; streamName = '" + streamName + "' ; elementName = '" + elementName + "'" );
        }

        return configSpecString;
    }

    private String checkViewName( StringTokenizer tokenizer )
        throws UnknownHostException
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

        return tokenizer.nextToken();
    }

    private void checkUnexpectedParameter( StringTokenizer tokenizer, int tokenNumber, int maxTokenNumber )
    {
        if ( tokenNumber > maxTokenNumber )
        {
            String unexpectedToken = tokenizer.nextToken();
            if ( logger.isInfoEnabled() )
            {
                logger.info( "The SCM URL contains unused parameter : " + unexpectedToken );
            }
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

    public String getStreamName()
    {
        return streamName;
    }

    public String getVobName()
    {
        return vobName;
    }

    public String getElementName()
    {
        return elementName;
    }

    public boolean hasElements()
    {
        if ( elementName == null )
        {
            return false;
        }

        return true;
    }
}
