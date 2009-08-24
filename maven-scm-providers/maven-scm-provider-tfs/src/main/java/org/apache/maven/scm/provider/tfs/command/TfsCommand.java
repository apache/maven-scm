package org.apache.maven.scm.provider.tfs.command;

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
import java.util.Iterator;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.apache.maven.scm.provider.tfs.command.consumer.FileListConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

public class TfsCommand
{

    private ScmLogger logger;

    private Commandline command;

    public TfsCommand( String cmd, ScmProviderRepository r, ScmFileSet f, ScmLogger logger )
    {
        command = new Commandline();
        command.setExecutable( "tf" );
        if ( f != null )
        {
            command.setWorkingDirectory( f.getBasedir().getAbsolutePath() );
        }
        
        command.createArg().setValue( cmd );
        
        if ( r.getUser() != null )
        {
            command.createArg().setValue( "-login:" + r.getUser() + "," + r.getPassword() );
        }
        this.logger = logger;
    }

    public void addArgument( ScmFileSet f )
    {
        info( "files: " + f.getBasedir().getAbsolutePath() );
        Iterator iter = f.getFileList().iterator();
        while ( iter.hasNext() )
        {
            command.createArg().setValue( ( (File) iter.next() ).getPath() );
        }
    }

    public void addArgument( String s )
    {
        command.createArg().setValue( s );
    }

    public int execute( StreamConsumer out, ErrorStreamConsumer err )
        throws ScmException
    {
        info( "Command line - " + getCommandString() );
        int status;
        try
        {
            status = CommandLineUtils.executeCommandLine( command, out, err );
        }
        catch ( CommandLineException e )
        {
            throw new ScmException( "Error while executing TFS command line - " + getCommandString(), e );
        }
        info( "err - " + err.getOutput() );
        if ( out instanceof StringStreamConsumer )
        {
            StringStreamConsumer sc = (StringStreamConsumer) out;
            debug( sc.getOutput() );
        }
        if ( out instanceof FileListConsumer )
        {
            FileListConsumer f = (FileListConsumer) out;
            for ( Iterator i = f.getFiles().iterator(); i.hasNext(); )
            {
                ScmFile file = (ScmFile) i.next();
                debug( file.getPath() );
            }
        }

        return status;
    }

    public String getCommandString()
    {
        return command.toString();
    }
    
    public Commandline getCommandline() {
        return command;
    }

    private void info( String message )
    {
        if ( logger != null )
            logger.info( message );
    }

    private void debug( String message )
    {
        if ( logger != null )
            logger.debug( message );
    }

}
