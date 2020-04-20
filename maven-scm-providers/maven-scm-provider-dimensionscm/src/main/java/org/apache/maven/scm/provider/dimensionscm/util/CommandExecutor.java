package org.apache.maven.scm.provider.dimensionscm.util;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Executes Dimensions CM commands.
 */
public class CommandExecutor
{

    private List<ScmFile> addedFiles = new ArrayList<>();

    private CommandExecutor()
    {
    }

    private static class SingletonHelper
    {
        private static final CommandExecutor INSTANCE = new CommandExecutor();
    }

    public static CommandExecutor getInstance()
    {
        return SingletonHelper.INSTANCE;
    }

    public boolean executeCmd( List<String> cmdToExec, ScmLogger logger ) throws ScmException
    {
        return execute( cmdToExec, logger, false, false );
    }

    public List<ScmFile> executeCmdWithParse( List<String> cmdToExec, ScmLogger logger ) throws ScmException
    {
        boolean ok = execute( cmdToExec, logger, false, true );
        if ( !ok )
        {
            addedFiles = new ArrayList<>();
        }
        return addedFiles;
    }

    public boolean executeCmdQuiet( List<String> cmdToExec ) throws ScmException
    {
        return execute( cmdToExec, null, true, false );
    }

    private boolean execute( List<String> cmdToExec, ScmLogger logger,
        boolean quiet, boolean parseAddedFiles ) throws ScmException
    {
        try
        {

            if ( cmdToExec == null || cmdToExec.isEmpty() )
            {
                throw new ScmException( "Command can't be empty." );
            }
            Process process = new ProcessBuilder( cmdToExec ).start();

            StreamReader outputStream = new StreamReader( process.getInputStream(), quiet, logger, parseAddedFiles );
            StreamReader errorStream = new StreamReader( process.getErrorStream(), quiet, logger );

            errorStream.start();
            outputStream.start();

            int w = process.waitFor();

            errorStream.join();
            outputStream.join();

            return w == 0;
        } 
        catch ( IOException | InterruptedException e )
        {
            throw new ScmException( "Error while executing command " + e.getMessage() );
        }
    }

    private class StreamReader extends Thread
    {
        InputStream is;
        boolean quiet;
        ScmLogger logger;
        boolean parse;

        StreamReader( InputStream is, boolean quiet, ScmLogger logger )
        {
            this.is = is;
            this.quiet = quiet;
            this.logger = logger;
        }

        StreamReader( InputStream is, boolean quiet, ScmLogger logger, boolean parseAddedFiles )
        {
            this( is, quiet, logger );
            this.parse = parseAddedFiles;
        }

        public void run()
        {
            try
            {
                BufferedReader br = new BufferedReader( new InputStreamReader( is, StandardCharsets.UTF_8 ) );
                String line;
                while ( ( line = br.readLine() ) != null )
                {
                    if ( !quiet )
                    {
                        logger.info( line );
                    }
                    if ( parse )
                    {
                        parseAddedFiles( line );
                    }
                }
            }
            catch ( IOException ioe )
            {
                ioe.printStackTrace();
            }
        }

        private void parseAddedFiles( String line )
        {
            if ( line.contains( "Creating new item revision for" ) )
            {
                String fileName = StringUtils.substringBetween( line, "Creating new item revision for '", "'" ).trim();
                addedFiles.add( new ScmFile( fileName, ScmFileStatus.ADDED ) );
            }

            if ( line.contains( "Adding file" ) )
            {
                String fileName = StringUtils.substringBetween( line, "Adding file '", "'" ).trim();
                addedFiles.add( new ScmFile( fileName, ScmFileStatus.ADDED ) );
            }
        }
    }

}
