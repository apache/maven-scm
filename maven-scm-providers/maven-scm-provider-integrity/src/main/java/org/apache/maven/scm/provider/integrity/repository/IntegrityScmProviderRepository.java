package org.apache.maven.scm.provider.integrity.repository;

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
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.integrity.APISession;
import org.apache.maven.scm.provider.integrity.Project;
import org.apache.maven.scm.provider.integrity.Sandbox;

/**
 * MKS Integrity implementation of Maven's ScmProviderRepositoryWithHost
 * <br>This class stores an abstraction of the MKS API Session, Project, and Sandbox
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityScmProviderRepository.java 1.2 2011/08/22 13:06:43EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    // Configuration Path for the MKS Integrity SCM Project
    private String configurationPath;

    // MKS API Session
    private APISession api;

    // Encapsulation for our MKS Integrity SCM Project
    private Project siProject;

    // Encapsulation for our MKS Integrity SCM Sandbox
    private Sandbox siSandbox;

    /**
     * IntegrityScmProviderRepository constructor
     *
     * @param host       MKS Integrity Server hostname or ip address
     * @param port       MKS Integrity Server port number
     * @param user       MKS Integrity Server username
     * @param paswd      Password for MKS Integrity Server username
     * @param configPath MKS Integrity SCM Project Configuration Path
     * @param logger     Maven ScmLogger object
     */
    public IntegrityScmProviderRepository( String host, int port, String user, String paswd, String configPath,
                                           ScmLogger logger )
    {
        super();
        setHost( host );
        setPort( port );
        setUser( user );
        setPassword( paswd );
        configurationPath = configPath;
        api = new APISession( logger );
        logger.debug( "Configuration Path: " + configurationPath );
    }

    /**
     * Returns the MKS Integrity SCM Project object for this SCM Provider
     *
     * @return MKS Integrity SCM Project object
     */
    public Project getProject()
    {
        return siProject;
    }

    /**
     * Sets the MKS Integrity SCM Project object for this SCM Provider
     *
     * @param project MKS Integrity SCM Project object
     */
    public void setProject( Project project )
    {
        siProject = project;
    }

    /**
     * Returns the MKS Integrity SCM Sandbox object for this SCM Provider
     *
     * @return MKS Integrity SCM Sandbox object
     */
    public Sandbox getSandbox()
    {
        return siSandbox;
    }

    /**
     * Sets the MKS Integrity SCM Sandbox object for this SCM Provider
     *
     * @param sandbox MKS Integrity SCM Sandbox object
     */
    public void setSandbox( Sandbox sandbox )
    {
        siSandbox = sandbox;
    }

    /**
     * Returns the MKS Integrity API Session object for this SCM Provider
     *
     * @return MKS Integrity API Session
     */
    public APISession getAPISession()
    {
        return api;
    }

    /**
     * Returns the MKS Integrity SCM Project Configuration Path
     *
     * @return MKS Integrity SCM Project Configuration Path
     */
    public String getConfigruationPath()
    {
        return configurationPath;
    }
}
