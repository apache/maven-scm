package org.apache.maven.scm.provider.accurev.commands.checkout;

/*
 * Copyright 2008 AccuRev Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevScmProvider;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class AccuRevCheckOutUsingPopCommand extends AbstractCheckOutCommand implements BaseAccuRevCheckOutCommand
{
    private String accuRevExecutable;

    public AccuRevCheckOutUsingPopCommand( String executable )
    {
        this.accuRevExecutable = executable;
    }

    public String getMethodName()
    {
        return "pop";
    }

    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                        ScmVersion version ) throws ScmException
    {
        try
        {
            AccuRevScmProviderRepository accurevRepository = (AccuRevScmProviderRepository) repository;

            final CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
            //Update the workspace
            Commandline popCommandline = createPopStreamCommand( accurevRepository, fileSet, version );

            //Create checkout folder if missing
            if ( !fileSet.getBasedir().exists() )
            {
                fileSet.getBasedir().mkdirs();
            }

            final ArrayList checkedFiles = new ArrayList();
            int exitCode = CommandLineUtils.executeCommandLine( popCommandline, new StreamConsumer()
            {
                Pattern pattern = Pattern.compile( "Populating element (.*)" );

                public void consumeLine( String line )
                {
                    stdout.consumeLine( line );
                    //Collect list of files poped from the stream
                    Matcher m = pattern.matcher( line );
                    if ( m.matches() )
                    {
                        checkedFiles.add( m.group( 1 ) );
                    }
                }
            }, stdout );

            //Check if the update command successeed
            if ( exitCode != 0 )
            {
                return new CheckOutScmResult( popCommandline.toString(),
                    "The AccuRev command failed", stdout.getOutput(), false );
            }
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "Checked out stream \"" + accurevRepository.getStreamName() +
                    "\" contents to folder " + fileSet.getBasedir() );
                //TODO notify user that the checked files are not in workspace and no changes can be propagated to SCM
            }
            return new CheckOutScmResult( popCommandline.toString(), checkedFiles );
        }
        catch ( CommandLineException e )
        {
            throw new ScmException( "internal error" );
        }
    }

    protected Commandline createPopStreamCommand( AccuRevScmProviderRepository repository,
                                                  ScmFileSet fileSet, ScmVersion version )
    {
        //TODO Implement support of fileSet, version if applyable
        Commandline commandline = new Commandline();
        commandline.setExecutable( this.accuRevExecutable );
        ArrayList params = new ArrayList();
        //Append command name
        params.add( "pop" );
        //Append host if needed
        AccuRevScmProvider.appendHostToParamsIfNeeded( repository, params );
        //Set command parameters
        commandline.addArguments( (String[]) params.toArray( new String[params.size()] ) );
        //Append command parameters
        commandline.addArguments( new String[]{
            "-v", repository.getStreamName(), //name of the stream
            "-L", fileSet.getBasedir().getAbsolutePath(), //into folder
            "-R" //get files recursively
        } );
        List fileSetArguments = new ArrayList();
        String filelistParam = (String) repository.getParams().get( "include" );
        if ( null == filelistParam )
        {
            //Add all by default
            fileSetArguments.add( "." );
        }
        else
        {
            String[] elements = StringUtils.split( filelistParam, "," );
            fileSetArguments.addAll( Arrays.asList( elements ) );
        }
        commandline.addArguments( (String[]) fileSetArguments.toArray( new String[fileSetArguments.size()] ) );
        return commandline;
    }
}
