package org.apache.maven.scm.provider.starteam.command.checkin;

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
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamCheckInCommand
    extends AbstractCheckInCommand
    implements StarteamCommand
{
    // ----------------------------------------------------------------------
    // AbstractCheckInCommand Implementation
    // ----------------------------------------------------------------------

    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      String tag )
        throws ScmException
    {

        //work around until maven-scm-api allow this
        String issueType = System.getProperty( "maven.scm.issue.type" );
        String issueValue = System.getProperty( "maven.scm.issue.value" );
        String deprecatedIssue = System.getProperty( "maven.scm.issue" );

        if ( deprecatedIssue != null && deprecatedIssue.trim().length() > 0 )
        {
            issueType = "cr";
            issueValue = deprecatedIssue;
        }

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamCheckInConsumer consumer = new StarteamCheckInConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        List checkInFiles = fileSet.getFileList();

        if ( checkInFiles.size() == 0 )
        {
            Commandline cl = createCommandLine( repository, fileSet, message, tag, issueType, issueValue );

            int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

            if ( exitCode != 0 )
            {
                return new CheckInScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
            }
        }
        else
        {
            //update only interested files already on the local disk
            for ( int i = 0; i < checkInFiles.size(); ++i )
            {
                ScmFileSet checkInFile = new ScmFileSet( fileSet.getBasedir(), (File) checkInFiles.get( i ) );

                Commandline cl = createCommandLine( repository, checkInFile, message, tag, issueType, issueValue );

                int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

                if ( exitCode != 0 )
                {
                    return new CheckInScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(),
                                                 false );
                }
            }
        }

        return new CheckInScmResult( null, consumer.getCheckedInFiles() );

    }


    public static Commandline createCommandLine( StarteamScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                 String tag, String issueType, String issueValue )
    {

        List args = new ArrayList();
        if ( message != null && message.length() != 0 )
        {
            args.add( "-r" );
            args.add( message );
        }

        if ( tag != null && tag.length() != 0 )
        {
            args.add( "-vl" );
            args.add( tag );
        }

        if ( issueType != null && issueType.trim().length() > 0 )
        {
            args.add( "-" + issueType.trim() );
            if ( issueValue != null && issueValue.trim().length() > 0 )
            {
                args.add( issueValue.trim() );
            }
        }

        boolean checkinDirectory = fileSet.getFileList().size() == 0;
        if ( !checkinDirectory )
        {
            if ( fileSet.getFileList().size() != 0 )
            {
                File subFile = (File) fileSet.getFileList().get( 0 );
                checkinDirectory = subFile.isDirectory();
            }
        }

        if ( checkinDirectory )
        {
            args.add( "-f" );
            args.add( "NCI" );
        }

        return StarteamCommandLineUtils.createStarteamCommandLine( "ci", args, fileSet, repo );

    }

}
