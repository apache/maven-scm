package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.IOException;

/**
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal update
 * @aggregator
 * @description Update the project
 */
public class UpdateMojo
    extends AbstractScmMojo
{
    /**
     * @parameter expression="${branch}"
     */
    private String branch;

    /**
     * @parameter expression="${tag}"
     */
    private String tag;

    /**
     * The project property where to store the revision name
     *
     * @parameter expression="${revisionKey}" default-value="scm.revision"
     */
    private String revisionKey;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            ScmRepository repository = getScmRepository();

            String currentTag = null;

            if ( branch != null )
            {
                currentTag = branch;
            }

            if ( tag != null )
            {
                currentTag = tag;
            }

            UpdateScmResult result =
                getScmManager().getProviderByRepository( repository ).update( repository, getFileSet(), currentTag );

            checkResult( result );

            if ( result instanceof UpdateScmResultWithRevision )
            {
                getLog().info( "Storing revision in '" + revisionKey + "' project property." );

                if ( project.getProperties() != null ) // Remove the test when we'll use plugin-test-harness 1.0-alpha-2
                {
                    project.getProperties().put( revisionKey, ( (UpdateScmResultWithRevision) result ).getRevision() );
                }
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot run checkout command : ", e );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run checkout command : ", e );
        }
    }
}
