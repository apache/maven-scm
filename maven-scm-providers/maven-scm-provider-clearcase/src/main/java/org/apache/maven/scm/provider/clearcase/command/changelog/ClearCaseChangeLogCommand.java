package org.apache.maven.scm.provider.clearcase.command.changelog;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ClearCaseChangeLogCommand
    extends AbstractChangeLogCommand
    implements ClearCaseCommand
{
    // ----------------------------------------------------------------------
    // AbstractChangeLogCommand Implementation
    // ----------------------------------------------------------------------

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, int numDays, String branch )
        throws ScmException
    {
        Commandline cl = createCommandLine( fileSet.getBasedir(), branch, startDate );

        ClearCaseChangeLogConsumer consumer = new ClearCaseChangeLogConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing cvs command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new ChangeLogScmResult( "The cleartool command failed.", stderr.getOutput(), false );
        }

        return new ChangeLogScmResult( consumer.getModifications() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( File workingDirectory, String branch, Date startDate )
    {
        Commandline command = new Commandline();
        command.setExecutable( "cleartool" );
        command.createArgument().setValue( "lshistory" );

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        StringBuffer format = new StringBuffer();
        format.append( "NAME:%En\\n" );
        format.append( "DATE:%Nd\\n" );
        format.append( "COMM:%-12.12o - %o - %c - Activity: %[activity]p\\n" );
        format.append( "USER:%-8.8u\\n" );

        command.createArgument().setValue( "-fmt" );
        command.createArgument().setValue( format.toString() );
        command.createArgument().setValue( "-recurse" );
        command.createArgument().setValue( "-nco" );

        if ( startDate != null )
        {
            SimpleDateFormat sdf = new SimpleDateFormat( "dd-MMM-yyyy", Locale.ENGLISH );

            String start = sdf.format( startDate );

            command.createArgument().setValue( "-since" );

            command.createArgument().setValue( start );
        }

        // TODO: End date?

        if ( branch != null )
        {
            command.createArgument().setValue( "-branch" );

            command.createArgument().setValue( branch );
        }

        return command;
    }
}
