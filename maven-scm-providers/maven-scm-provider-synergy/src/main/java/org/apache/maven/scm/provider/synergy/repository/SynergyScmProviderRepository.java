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
 * @version $Id$
 */
public class SynergyScmProviderRepository
    extends ScmProviderRepository
{

    private String projectSpec;

    private String projectName;

    private String projectVersion;

    private String projectRelease;

    private String projectPurpose;

    private String delimiter;
    
    private String instance;

    /**
     * @param url format is
     *            project_name|delimiter|project_version|Release|Purpose|instance
     */
    public SynergyScmProviderRepository( String url )
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
        if ( tokenizer.countTokens() == 5 )
        {
            projectName = tokenizer.nextToken();
            delimiter = tokenizer.nextToken();
            projectVersion = tokenizer.nextToken();
            projectRelease = tokenizer.nextToken();
            projectPurpose = tokenizer.nextToken();
            instance = "1";

            projectSpec = projectName + delimiter + projectVersion + ":project:" + instance;

        }
        else if (tokenizer.countTokens() == 6 )
        {   //optional prep project instance also
            projectName = tokenizer.nextToken();
            delimiter = tokenizer.nextToken();
            projectVersion = tokenizer.nextToken();
            projectRelease = tokenizer.nextToken();
            projectPurpose = tokenizer.nextToken();
            instance = tokenizer.nextToken();

            projectSpec = projectName + delimiter + projectVersion + ":project:" + instance;
            
        }
        else
        {
            throw new MalformedURLException();
        }
    }

    public String getProjectSpec()
    {
        return projectSpec;
    }

    public String getProjectName()
    {
        return projectName;
    }

    public String getProjectVersion()
    {
        return projectVersion;
    }

    /**
     * @return the project_purpose
     */
    public String getProjectPurpose()
    {
        return projectPurpose;
    }

    /**
     * @return the project_release
     */
    public String getProjectRelease()
    {
        return projectRelease;
    }

    /**
     * @return the instance
     */
    public String getInstance() {
        return instance;
    }
    

}
