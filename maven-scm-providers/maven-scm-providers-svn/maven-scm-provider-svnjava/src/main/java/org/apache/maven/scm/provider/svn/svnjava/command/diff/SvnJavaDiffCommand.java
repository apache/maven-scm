package org.apache.maven.scm.provider.svn.svnjava.command.diff;

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
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.diff.SvnDiffConsumer;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.util.ScmFileEventHandler;
import org.apache.maven.scm.provider.svn.svnjava.util.SvnJavaUtil;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SvnJavaDiffCommand
    extends AbstractDiffCommand
    implements SvnCommand
{
    /** {@inheritDoc} */
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                ScmVersion startRevision, ScmVersion endRevision )
        throws ScmException
    {
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "SVN diff directory: " + fileSet.getBasedir().getAbsolutePath() );
        }

        SvnJavaScmProviderRepository javaRepo = (SvnJavaScmProviderRepository) repo;

        ScmFileEventHandler handler = new ScmFileEventHandler( getLogger(), fileSet.getBasedir() );

        try
        {
            javaRepo.getClientManager().getDiffClient().setEventHandler( handler );

            SVNRevision start =
                ( startRevision == null ) ? SVNRevision.COMMITTED : SVNRevision.parse( startRevision.getName() );
            SVNRevision end =
                ( endRevision == null ) ? SVNRevision.WORKING : SVNRevision.parse( endRevision.getName() );

            ByteArrayOutputStream out =
                SvnJavaUtil.diff( javaRepo.getClientManager(), fileSet.getBasedir(), start, end );

            SvnDiffConsumer consumer = new SvnDiffConsumer( getLogger(), fileSet.getBasedir() );

            ByteArrayInputStream bis = new ByteArrayInputStream( out.toByteArray() );

            BufferedReader in = new BufferedReader( new InputStreamReader( bis ) );

            String line = in.readLine();
            while ( line != null )
            {
                consumer.consumeLine( line );

                line = in.readLine();
            }

            return new DiffScmResult( SvnJavaScmProvider.COMMAND_LINE, consumer.getChangedFiles(),
                                      consumer.getDifferences(), consumer.getPatch() );
        }
        catch ( IOException e )
        {
            return new DiffScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN diff failed.", e.getMessage(), false );
        }
        catch ( SVNException e )
        {
            return new DiffScmResult( SvnJavaScmProvider.COMMAND_LINE, "SVN diff failed.", e.getMessage(), false );
        }
        finally
        {
            javaRepo.getClientManager().getDiffClient().setEventHandler( null );
        }
    }
}
