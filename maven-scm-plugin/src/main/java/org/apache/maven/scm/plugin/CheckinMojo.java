package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.IOException;

/**
 * Commit changes to the configured scm url.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal checkin
 * @description checkin the project
 */
public class CheckinMojo
    extends AbstractScmMojo
{
    /**
     * Commit log.
     *
     * @parameter expression="${message}"
     */
    private String message;

    /**
     * The configured scm url to use.
     *
     * @parameter expression="${connectionType}" default-value="developerConnection"
     */
    private String connectionType;

    /**
     * The version type (branch/tag/revision) of scmVersion.
     *
     * @parameter expression="${scmVersionType}"
     */
    private String scmVersionType;

    /**
     * The version (revision number/branch name/tag name).
     *
     * @parameter expression="${scmVersion}"
     */
    private String scmVersion;

    public void execute()
        throws MojoExecutionException
    {
        setConnectionType( connectionType );

        try
        {
            ScmRepository repository = getScmRepository();

            CheckInScmResult result = getScmManager().getProviderByRepository( repository ).checkIn( repository,
                                                                                                     getFileSet(),
                                                                                                     getScmVersion(
                                                                                                         scmVersionType,
                                                                                                         scmVersion ),
                                                                                                     message );

            checkResult( result );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot run checkin command : ", e );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run checkin command : ", e );
        }
    }
}
