package org.apache.maven.scm.provider.git.jgit.command.diff;

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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.command.diff.GitDiffConsumer;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitDiffCommand
    extends AbstractDiffCommand
    implements GitCommand
{

    @Override
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                ScmVersion startRevision, ScmVersion endRevision )
        throws ScmException
    {

        Git git = null;
        try
        {
            git = JGitUtils.openRepo( fileSet.getBasedir() );
            DiffScmResult diff = callDiff( git, startRevision, endRevision );
            git.getRepository().close();
            return diff;
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit diff failure!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }

    public DiffScmResult callDiff( Git git, ScmVersion startRevision, ScmVersion endRevision )
        throws IOException, GitAPIException, ScmException
    {

        AbstractTreeIterator oldTree = null;
        if ( startRevision != null && StringUtils.isNotEmpty( startRevision.getName().trim() ) )
        {
            String startRev = startRevision.getName().trim();
            oldTree = getTreeIterator( git.getRepository(), startRev );
        }

        AbstractTreeIterator newTree = null;
        if ( endRevision != null && StringUtils.isNotEmpty( endRevision.getName().trim() ) )
        {
            String endRev = endRevision.getName().trim();
            newTree = getTreeIterator( git.getRepository(), endRev );
        }

        OutputStream out = new ByteArrayOutputStream();

        git.diff().setOutputStream( out ).setOldTree( oldTree ).setNewTree( newTree ).setCached( false ).call();
        git.diff().setOutputStream( out ).setOldTree( oldTree ).setNewTree( newTree ).setCached( true ).call();

        out.flush();

        GitDiffConsumer consumer = new GitDiffConsumer( getLogger(), null );
        String fullDiff = out.toString();
        out.close();

        String[] lines = fullDiff.split( "\n" );
        for ( String aLine : lines )
        {
            consumer.consumeLine( aLine );
        }

        return new DiffScmResult( "JGit diff", consumer.getChangedFiles(), consumer.getDifferences(),
                                  consumer.getPatch() );
    }

    private AbstractTreeIterator getTreeIterator( Repository repo, String name )
        throws IOException
    {
        final ObjectId id = repo.resolve( name );
        if ( id == null )
        {
            throw new IllegalArgumentException( name );
        }
        final CanonicalTreeParser p = new CanonicalTreeParser();
        final ObjectReader or = repo.newObjectReader();
        try
        {
            p.reset( or, new RevWalk( repo ).parseTree( id ) );
            return p;
        }
        finally
        {
            or.release();
        }
    }
}
