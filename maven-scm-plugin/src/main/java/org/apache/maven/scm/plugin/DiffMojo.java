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
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Display the difference of the working copy with the latest copy in the configured scm url.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal diff
 * @aggregator
 */
public class DiffMojo
    extends AbstractScmMojo
{
    /**
     * The version type (branch/tag/revision) of scmVersion.
     *
     * @parameter expression="${startScmVersionType}"
     */
    private String startScmVersionType;

    /**
     * The version (revision number/branch name/tag name).
     *
     * @parameter expression="${startScmVersion}"
     */
    private String startScmVersion;

    /**
     * The version type (branch/tag/revision) of scmVersion.
     *
     * @parameter expression="${endScmVersionType}"
     */
    private String endScmVersionType;

    /**
     * The version (revision number/branch name/tag name).
     *
     * @parameter expression="${startScmVersion}"
     */
    private String endScmVersion;

    /**
     * Output file name.
     *
     * @parameter expression="${outputFile}"
     * default-value="${project.artifactId}.diff"
     */
    private File outputFile;

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        super.execute();

        try
        {
            ScmRepository repository = getScmRepository();

            DiffScmResult result = getScmManager().diff( repository, getFileSet(),
                                                         getScmVersion( startScmVersionType, startScmVersion ),
                                                         getScmVersion( endScmVersionType, endScmVersion ) );

            checkResult( result );

            getLog().info( result.getPatch() );

            try
            {
                if ( outputFile != null )
                {
                    FileUtils.fileWrite( outputFile.getAbsolutePath(), result.getPatch() );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Can't write patch file.", e );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot run diff command : ", e );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run diff command : ", e );
        }
    }
}
