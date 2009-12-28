package org.apache.maven.scm.provider.git.jgit.command.tag;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.simple.LsFileEntry;
import org.eclipse.jgit.simple.SimpleRepository;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id $
 */
public class JGitTagCommand
    extends AbstractTagCommand
    implements GitCommand
{
    
    public ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag, String message )
        throws ScmException
    {
        return executeTagCommand( repo, fileSet, tag, new ScmTagParameters( message ) );
    }
    
    /** {@inheritDoc} */
    public ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag, ScmTagParameters scmTagParameters )
        throws ScmException
    {
        if ( tag == null || StringUtils.isEmpty( tag.trim() ) )
        {
            throw new ScmException( "tag name must be specified" );
        }

        if ( !fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "This provider doesn't support tagging subsets of a directory" );
        }

        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        try
        {
            SimpleRepository srep = SimpleRepository.existing( fileSet.getBasedir() );

            // tag the revision 
            String tagMessage = scmTagParameters.getMessage();
            srep.tag( tag, tagMessage, null , false);

            // and now push the tag to the origin repository
            srep.push( null, null, "origin", null, null, false, true, null);
            
            // plus search for the tagged files
            List<LsFileEntry> lsFiles = srep.lsFiles();

            List<ScmFile> taggedFiles = new ArrayList<ScmFile>();

            for (LsFileEntry fEntry : lsFiles ) {
                taggedFiles.add( new ScmFile(fEntry.getFilePath(), ScmFileStatus.TAGGED) );
            }
            
            
            return new TagScmResult( "JGit tag", taggedFiles );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit tag failure!", e );
        }

    }
 
}
