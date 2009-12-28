package org.apache.maven.scm.provider.git.jgit.command.add;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.eclipse.jgit.simple.SimpleRepository;
import org.eclipse.jgit.simple.StatusEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id$
 */
public class JGitAddCommand
    extends AbstractAddCommand
    implements GitCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        if ( fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "You must provide at least one file/directory to add" );
        }
        try
        {
            SimpleRepository srep = SimpleRepository.existing( fileSet.getBasedir() );
            JGitUtils.addAllFiles( srep, fileSet );
            
            // git-add doesn't show single files, but only summary
            // so we must run git-status
            List<StatusEntry> entries = srep.status();

            List<ScmFile> changedFiles = new ArrayList<ScmFile>();

            // rewrite all detected files to now have status 'checked_in'
            for ( StatusEntry entry : entries )
            {
                ScmFile scmfile = new ScmFile( entry.getFilePath(), JGitUtils.getScmFileStatus( entry ) );

                // if a specific fileSet is given, we have to check if the file is really tracked
                for ( Iterator<File> itfl = fileSet.getFileList().iterator(); itfl.hasNext(); )
                {
                    File f = (File) itfl.next();
                    if ( f.toString().equals( scmfile.getPath() ) )
                    {
                        changedFiles.add( scmfile );
                    }
                }
            }
            return new AddScmResult( "JGit add", changedFiles );
            
        }
        catch ( Exception e )
        {
            throw new ScmException("JGit add failure!", e );
        }
        
    }

}
