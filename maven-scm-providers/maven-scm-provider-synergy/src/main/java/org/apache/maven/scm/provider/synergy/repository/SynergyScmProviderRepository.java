package org.apache.maven.scm.provider.synergy.repository;

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

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyScmProviderRepository
    extends ScmProviderRepository
{

    private String project_spec;

    private String project_name;

    private String project_version;

    private String project_release;

    private String project_purpose;

    private String delimiter;

    /**
     * @param url format is
     *            project_name|delimiter|project_version|Release|Purpose
     */
    public SynergyScmProviderRepository( String url )
        throws ScmRepositoryException
    {
        System.out.println( "DEBUG" );
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
        if ( tokenizer.countTokens() == 5 )
        {
            project_name = tokenizer.nextToken();
            delimiter = tokenizer.nextToken();
            project_version = tokenizer.nextToken();
            project_release = tokenizer.nextToken();
            project_purpose = tokenizer.nextToken();

            project_spec = project_name + delimiter + project_version;

        }
        else
        {
            throw new MalformedURLException();
        }
    }

    public String getProjectSpec()
    {
        return project_spec;
    }

    public String getProjectName()
    {
        return project_name;
    }

    public String getProjectVersion()
    {
        return project_version;
    }

    /**
     * @return the project_purpose
     */
    public String getProjectPurpose()
    {
        return project_purpose;
    }

    /**
     * @return the project_release
     */
    public String getProjectRelease()
    {
        return project_release;
    }

}
