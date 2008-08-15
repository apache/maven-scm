package org.apache.maven.scm.provider.accurev.commands.add;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevScmProvider;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * @version $Id$
 */
public class AccuRevAddCommand extends AbstractAddCommand
{
    private final String accurevExecutable;

    public AccuRevAddCommand( String accurevExec )
    {
        accurevExecutable = accurevExec;
    }

    /** {@inheritDoc} */
    protected ScmResult executeAddCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                           String message, boolean binary ) throws ScmException
    {
        Commandline cl = new Commandline();
        cl.setExecutable( accurevExecutable );
        cl.setWorkingDirectory( fileSet.getBasedir().getPath() );
        cl.addArguments( new String[]{"add"} );
        ArrayList params = new ArrayList();
        AccuRevScmProvider.appendHostToParamsIfNeeded( (AccuRevScmProviderRepository) repository, params );
        cl.addArguments( (String[]) params.toArray( new String[params.size()] ) );

        cl.addArguments( makeFileArgs( fileSet.getFileList() ) );

        final List filesAdded = new ArrayList();
        final CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        try
        {
            if ( 0 != CommandLineUtils.executeCommandLine( cl, new AddCommandStreamConsumer( stdout, filesAdded ),
                stdout ) )
            {
                return new AddScmResult( cl.toString(), null, stdout.getOutput(), false );
            }
        }
        catch ( CommandLineException e )
        {
            throw new ScmRepositoryException( "Cannot exeucute add command", e );
        }
        return new AddScmResult( cl.toString(), filesAdded );
    }

    private String[] makeFileArgs( List/*File*/ fileList )
    {
        ArrayList res = new ArrayList();
        for ( int i = 0; i < fileList.size(); i++ )
        {
            File file = (File) fileList.get( i );
            res.add( file.getAbsolutePath() );
        }
        return (String[]) res.toArray( new String[res.size()] );
    }

}
