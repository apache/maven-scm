package org.apache.maven.scm.provider.integrity.command.diff;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.APISession;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * MKS Integrity implementation for Maven's AbstractDiffCommand
 * <br>Since MKS Integrity doesn't have a notion of arbitrarily differencing
 * by a revision across the sandbox, this command will difference the
 * current Sandbox working file against the server version.
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @since 1.6
 */
public class IntegrityDiffCommand
    extends AbstractDiffCommand
{
    /**
     * Since we can't arbitrarily apply the same start and end revisions to all files in the sandbox,
     * this command will be adapted to show differences between the local version and the repository
     */
    @Override
    public DiffScmResult executeDiffCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                             ScmVersion startRevision, ScmVersion endRevision )
        throws ScmException
    {
        DiffScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        APISession api = iRepo.getAPISession();
        getLogger().info( "Showing differences bettween local files in " + fileSet.getBasedir().getAbsolutePath()
                              + " and server project " + iRepo.getConfigruationPath() );

        // Since the si diff command is not completely API ready, we will use the CLI for this command
        Commandline shell = new Commandline();
        shell.setWorkingDirectory( fileSet.getBasedir() );
        shell.setExecutable( "si" );
        shell.createArg().setValue( "diff" );
        shell.createArg().setValue( "--hostname=" + api.getHostName() );
        shell.createArg().setValue( "--port=" + api.getPort() );
        shell.createArg().setValue( "--user=" + api.getUserName() );
        shell.createArg().setValue( "-R" );
        shell.createArg().setValue( "--filter=changed:all" );
        shell.createArg().setValue( "--filter=format:text" );
        IntegrityDiffConsumer shellConsumer = new IntegrityDiffConsumer( getLogger() );
        String commandLine = CommandLineUtils.toString( shell.getCommandline() );

        try
        {
            getLogger().debug( "Executing: " + commandLine );
            int exitCode = CommandLineUtils.executeCommandLine( shell, shellConsumer,
                                                                new CommandLineUtils.StringStreamConsumer() );
            boolean success = ( exitCode == 128 ? false : true );
            ScmResult scmResult = new ScmResult( commandLine, "", "Exit Code: " + exitCode, success );
            // Since we can't really parse the differences output, we'll just have to go by the command output
            // Returning a DiffScmResult(List changedFiles, Map differences, String patch, ScmResult result) to avoid
            // a NPE in org.codehaus.plexus.util.FileUtils.fileWrite(FileUtils.java:426)
            return new DiffScmResult( new ArrayList<ScmFile>(), new HashMap<String, CharSequence>(), "", scmResult );

        }
        catch ( CommandLineException cle )
        {
            getLogger().error( "Command Line Exception: " + cle.getMessage() );
            result = new DiffScmResult( commandLine, cle.getMessage(), "", false );
        }

        return result;
    }

}
