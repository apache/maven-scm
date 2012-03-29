package org.apache.maven.scm.provider.perforce.command.changelog;

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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class PerforceChangeLogCommand
    extends AbstractChangeLogCommand
    implements PerforceCommand
{
    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          ScmVersion startVersion, ScmVersion endVersion,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, null, null, null, datePattern, startVersion, endVersion );
    }

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        if ( branch != null && StringUtils.isNotEmpty( branch.getName() ) )
        {
            throw new ScmException( "This SCM doesn't support branches." );
        }

        return executeChangeLogCommand( repo, fileSet, startDate, endDate, branch, datePattern, null, null );
    }

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern, ScmVersion startVersion,
                                                          ScmVersion endVersion )
        throws ScmException
    {
        PerforceScmProviderRepository p4repo = (PerforceScmProviderRepository) repo;
        String clientspec = PerforceScmProvider.getClientspecName( getLogger(), p4repo, fileSet.getBasedir() );
        Commandline cl = createCommandLine( p4repo, fileSet.getBasedir(), clientspec, null, startDate, endDate, startVersion, endVersion );

        String location = PerforceScmProvider.getRepoPath( getLogger(), p4repo, fileSet.getBasedir() );
        PerforceChangesConsumer consumer =
            new PerforceChangesConsumer( getLogger() );

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( PerforceScmProvider.clean( "Executing " + cl.toString() ) );
            }

            CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
            int exitCode = CommandLineUtils.executeCommandLine( cl, consumer, err );

            if ( exitCode != 0 )
            {
                String cmdLine = CommandLineUtils.toString( cl.getCommandline() );

                StringBuilder msg = new StringBuilder( "Exit code: " + exitCode + " - " + err.getOutput() );
                msg.append( '\n' );
                msg.append( "Command line was:" + cmdLine );

                throw new CommandLineException( msg.toString() );
            }
        }
        catch ( CommandLineException e )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "CommandLineException " + e.getMessage(), e );
            }
        }

        List<String> changes = consumer.getChanges();

        cl = PerforceScmProvider.createP4Command( p4repo, fileSet.getBasedir() );
        cl.createArg().setValue( "describe" );
        cl.createArg().setValue( "-s" );

        for( String change : changes )
        {
            cl.createArg().setValue( change );
        }

        PerforceDescribeConsumer describeConsumer =
            new PerforceDescribeConsumer( location, datePattern, getLogger() );

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( PerforceScmProvider.clean( "Executing " + cl.toString() ) );
            }

            CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
            int exitCode = CommandLineUtils.executeCommandLine( cl, describeConsumer, err );

            if ( exitCode != 0 )
            {
                String cmdLine = CommandLineUtils.toString( cl.getCommandline() );

                StringBuilder msg = new StringBuilder( "Exit code: " + exitCode + " - " + err.getOutput() );
                msg.append( '\n' );
                msg.append( "Command line was:" + cmdLine );

                throw new CommandLineException( msg.toString() );
            }
        }
        catch ( CommandLineException e )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "CommandLineException " + e.getMessage(), e );
            }
        }

        ChangeLogSet cls = new ChangeLogSet( describeConsumer.getModifications(), null, null );
        cls.setStartVersion(startVersion);
        cls.setEndVersion(endVersion);
        return new ChangeLogScmResult( cl.toString(), cls );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                 String clientspec,
                                                 ScmBranch branch, Date startDate, Date endDate,
                                                 ScmVersion startVersion, ScmVersion endVersion )
    {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd:HH:mm:ss");
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        if ( clientspec != null )
        {
            command.createArg().setValue( "-c" );
            command.createArg().setValue( clientspec );
        }
        command.createArg().setValue( "changes" );
        command.createArg().setValue( "-t" );

        StringBuilder fileSpec = new StringBuilder("...");
        if ( startDate != null )
        {
            fileSpec.append( "@" )
                 .append( dateFormat.format(startDate) )
                 .append( "," );

            if ( endDate != null )
            {
                fileSpec.append( dateFormat.format(endDate) );
            }
            else
            {
                fileSpec.append( "now" );
            }
        }

        if ( startVersion != null )
        {
            fileSpec.append("@").append(startVersion.getName()).append(",");

            if ( endVersion != null )
            {
                fileSpec.append( endVersion.getName() );
            }
            else
            {
                fileSpec.append("now");
            }
        }

        command.createArg().setValue( fileSpec.toString() );

        return command;
    }
}
