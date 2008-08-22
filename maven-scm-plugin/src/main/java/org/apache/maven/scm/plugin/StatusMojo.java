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
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Display the modification status of the files in the configured scm url.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal status
 * @aggregator
 */
public class StatusMojo
    extends AbstractScmMojo
{
    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        super.execute();

        try
        {
            ScmRepository repository = getScmRepository();

            StatusScmResult result = getScmManager().status( repository, getFileSet() );

            checkResult( result );

            File baseDir = getFileSet().getBasedir();

            // Determine the maximum length of the status column
            int maxLen = 0;

            for ( Iterator iter = result.getChangedFiles().iterator(); iter.hasNext(); )
            {
                ScmFile file = (ScmFile) iter.next();
                maxLen = Math.max( maxLen, file.getStatus().toString().length() );
            }

            for ( Iterator iter = result.getChangedFiles().iterator(); iter.hasNext(); )
            {
                ScmFile file = (ScmFile) iter.next();

                // right align all of the statuses
                getLog().info(
                               StringUtils.leftPad( file.getStatus().toString(), maxLen ) + " status for "
                                   + getRelativePath( baseDir, file.getPath() ) );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot run status command : ", e );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run status command : ", e );
        }
    }

    /**
     * Formats the filename so that it is a relative directory from the base.
     *
     * @param baseDir
     * @param path
     * @return The relative path
     */
    protected String getRelativePath( File baseDir, String path )
    {
        if ( path.equals( baseDir.getAbsolutePath() ) )
        {
            return ".";
        }
        else if ( path.indexOf( baseDir.getAbsolutePath() ) == 0 )
        {
            // the + 1 gets rid of a leading file separator
            return path.substring( baseDir.getAbsolutePath().length() + 1 );
        }
        else
        {
            return path;
        }
    }
}
