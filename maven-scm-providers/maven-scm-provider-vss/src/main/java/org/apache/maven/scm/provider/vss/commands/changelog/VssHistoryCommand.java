package org.apache.maven.scm.provider.vss.commands.changelog;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.vss.commands.VssCommandLineUtils;
import org.apache.maven.scm.provider.vss.commands.VssConstants;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VssHistoryCommand
    extends AbstractChangeLogCommand
{
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, String branch,
                                                          String datePattern )
        throws ScmException
    {
        VssScmProviderRepository repo = (VssScmProviderRepository) repository;

        Commandline cl = buildCmdLine( repo, fileSet, startDate, endDate );

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        VssChangeLogConsumer consumer = new VssChangeLogConsumer( repo, datePattern, getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode = VssCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

        if ( exitCode != 0 )
        {
            return new ChangeLogScmResult( cl.toString(), "The vss command failed.", stderr.getOutput(), false );
        }

        return new ChangeLogScmResult( cl.toString(),
                                       new ChangeLogSet( consumer.getModifications(), startDate, endDate ) );
    }

    public Commandline buildCmdLine( VssScmProviderRepository repo, ScmFileSet fileSet, Date startDate, Date endDate )
        throws ScmException
    {
        Commandline command = new Commandline();

        command.setWorkingDirectory( fileSet.getBasedir().getAbsolutePath() );

        try
        {
            command.addSystemEnvironment();
        }
        catch ( Exception e )
        {
            throw new ScmException( "Can't add system environment.", e );
        }

        command.addEnvironment( "SSDIR", repo.getVssdir() );

        String ssDir = VssCommandLineUtils.getSsDir();

        command.setExecutable( ssDir + VssConstants.SS_EXE );

        command.createArgument().setValue( VssConstants.COMMAND_HISTORY );

        command.createArgument().setValue( VssConstants.PROJECT_PREFIX + repo.getProject() );

        //User identification to get access to vss repository
        if ( repo.getUserPassword() != null )
        {
            command.createArgument().setValue( VssConstants.FLAG_LOGIN + repo.getUserPassword() );
        }

        //Display the history of an entire project list
        command.createArgument().setValue( VssConstants.FLAG_RECURSION );

        //Ignore: Do not ask for input under any circumstances.
        command.createArgument().setValue( VssConstants.FLAG_AUTORESPONSE_DEF );

        //Display only versions that fall within specified data range.
        if ( startDate != null )
        {
            SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy", Locale.ENGLISH );
            String dateRange = sdf.format( endDate ) + "~" + sdf.format( startDate );
            command.createArgument().setValue( VssConstants.FLAG_VERSION_DATE + dateRange );
        }
        return command;
    }
}
