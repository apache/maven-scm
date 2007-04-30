package org.apache.maven.scm.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

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

/**
 * Get a fresh copy of the latest source from the configured scm url.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal list
 * @description List files in project
 */
public class ListMojo
    extends AbstractScmMojo
{
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

    /**
     * Use recursive mode.
     *
     * @parameter expression="${recursive}" default-value="true"
     */
    private boolean recursive = true;

    public void execute()
        throws MojoExecutionException
    {
        super.execute();

        try
        {
            ScmRepository repository = getScmRepository();
            ListScmResult result = getScmManager().list( repository, getFileSet(), recursive,
                                                         getScmVersion( scmVersionType, scmVersion ) );

            checkResult( result );

            if ( result.getFiles() != null )
            {
                for ( Iterator i = result.getFiles().iterator(); i.hasNext(); )
                {
                    ScmFile scmFile = (ScmFile) i.next();
                    getLog().info( scmFile.getPath() );
                }
            }
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run list command : ", e );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot run list command : ", e );
        }
    }

    public ScmFileSet getFileSet()
        throws IOException
    {
        if ( getIncludes() != null || getExcludes() != null )
        {
            return new ScmFileSet( getWorkingDirectory(), getIncludes(), getExcludes() );
        }
        else
        {
            return new ScmFileSet( getWorkingDirectory(), new File( "." ) );
        }
    }

}

