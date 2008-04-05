package org.apache.maven.scm.provider.git.gitexe.command.changelog;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class GitChangeLogCommand
    extends AbstractChangeLogCommand
    implements GitCommand
{
    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          ScmVersion startVersion, ScmVersion endVersion,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, null, null, null, datePattern, startVersion, endVersion );
    }

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, startDate, endDate, branch, datePattern, null, null );
    }

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern, ScmVersion startVersion,
                                                          ScmVersion endVersion )
        throws ScmException
    {
        Commandline cl = createCommandLine( (GitScmProviderRepository) repo, fileSet.getBasedir(), branch, startDate,
                                            endDate, startVersion, endVersion );

        GitChangeLogConsumer consumer = new GitChangeLogConsumer( getLogger(), datePattern );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        exitCode = GitCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new ChangeLogScmResult( cl.toString(), "The git-log command failed.", stderr.getOutput(), false );
        }
        ChangeLogSet changeLogSet = new ChangeLogSet( consumer.getModifications(), startDate, endDate );
        changeLogSet.setStartVersion( startVersion );
        changeLogSet.setEndVersion( endVersion );

        return new ChangeLogScmResult( cl.toString(), changeLogSet );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory,
                                                 ScmBranch branch, Date startDate, Date endDate,
                                                 ScmVersion startVersion, ScmVersion endVersion )
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat( DATE_FORMAT );
        dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "log" );

        if ( startVersion != null ) {
            cl.createArgument().setValue( "--since=" + StringUtils.escape( startVersion.getName() ) );
        }
        else 
        {
            if ( startDate != null )
            {
                cl.createArgument().setValue( "--since=" + StringUtils.escape( dateFormat.format( startDate ) ) );
            }
        }
        
        if ( endVersion != null ) {
            cl.createArgument().setValue( "--until=" + StringUtils.escape( endVersion.getName() ) );
        }
        else
        {
            if ( endDate != null )
            {
                cl.createArgument().setValue( "--until=" + StringUtils.escape( dateFormat.format( endDate ) ) );
            }
        }
        
        return cl;
    }
}
