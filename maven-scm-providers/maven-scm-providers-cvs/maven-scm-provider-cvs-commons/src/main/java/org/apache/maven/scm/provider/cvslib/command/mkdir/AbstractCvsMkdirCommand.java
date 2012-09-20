package org.apache.maven.scm.provider.cvslib.command.mkdir;

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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.mkdir.AbstractMkdirCommand;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 *
 */
public abstract class AbstractCvsMkdirCommand
    extends AbstractMkdirCommand
{
    /** {@inheritDoc} */
    protected MkdirScmResult executeMkdirCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message,
                                                  boolean createInLocal )
        throws ScmException
    {
        CommandParameters parameters = new CommandParameters();

        parameters.setString( CommandParameter.MESSAGE, message == null ? "" : message );

        parameters.setString( CommandParameter.BINARY, "false" );

        // just invoke add command
        Command cmd = getAddCommand();
        cmd.setLogger( getLogger() );

        ScmResult addResult = cmd.execute( repository, fileSet, parameters );

        if ( !addResult.isSuccess() )
        {
            return new MkdirScmResult( addResult.getCommandLine().toString(), "The cvs command failed.",
                                       addResult.getCommandOutput(), false );
        }
        
        List<ScmFile> addedFiles = new ArrayList<ScmFile>();
        
        for (File file : fileSet.getFileList()) 
        {
            ScmFile scmFile = new ScmFile( file.getPath(), ScmFileStatus.ADDED );
            addedFiles.add( scmFile );
        }

        return new MkdirScmResult( addResult.getCommandLine().toString(), addedFiles );
    }

    protected abstract Command getAddCommand();
}
