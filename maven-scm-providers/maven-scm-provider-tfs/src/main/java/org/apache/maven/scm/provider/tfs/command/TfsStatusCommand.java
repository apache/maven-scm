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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class TfsStatusCommand
    extends AbstractStatusCommand
{

    protected StatusScmResult executeStatusCommand( ScmProviderRepository r, ScmFileSet f )
        throws ScmException
    {
        TfsScmProviderRepository tfsRepo = (TfsScmProviderRepository) r;

        TfsCommand command = createCommand( tfsRepo, f );
        ChangedFileConsumer out = new ChangedFileConsumer( getLogger() );
        ErrorStreamConsumer err = new ErrorStreamConsumer();
        int status = command.execute( out, err );
        if ( status != 0 || err.hasBeenFed() )
            return new StatusScmResult( command.getCommandline(), "Error code for TFS status command - " + status,
                                        err.getOutput(), false );
        Iterator iter = out.getChangedFiles().iterator();
        getLogger().debug( "Iterating" );
        while ( iter.hasNext() )
        {
            ScmFile file = (ScmFile) iter.next();
            getLogger().debug( file.getPath() + ":" + file.getStatus() );
        }
        return new StatusScmResult( command.getCommandline(), out.getChangedFiles() );
    }

    TfsCommand createCommand( TfsScmProviderRepository r, ScmFileSet f )
    {
        String url = r.getServerPath();
        String workspace = r.getWorkspace();
        TfsCommand command = new TfsCommand( "status", r, f, getLogger() );
        if ( workspace != null && !workspace.trim().equals( "" ) )
            command.addArgument( "-workspace:" + workspace );
        command.addArgument( "-recursive" );
        command.addArgument( "-format:detailed" );
        command.addArgument( url );
        return command;
    }
}

class ChangedFileConsumer
    implements StreamConsumer
{

    private ScmLogger logger;

    static final String KEY_CHANGE = "Change";

    static final String KEY_LOCAL_ITEM = "Local item";

    static final String CHANGE_EDIT = "edit";

    static final String CHANGE_ADD = "add";

    HashMap values = new HashMap();

    ArrayList changedFiles = new ArrayList();

    public ChangedFileConsumer( ScmLogger logger )
    {
        this.logger = logger;
    }

    public void consumeLine( String line )
    {
        if ( line.contains( ":" ) )
        {
            String[] s = line.split( ":", 2 );
            if ( s.length > 1 )
                values.put( s[0].trim(), s[1].trim() );
        }
        if ( line.trim().equals( "" ) )
        {
            extractChangedFile();
        }
        logger.debug( "line -" + line );
    }

    private void extractChangedFile()
    {
        String change = getChange();
        if ( change != null )
        {
            ScmFileStatus stat = ScmFileStatus.UNKNOWN;
            if ( change.equals( ChangedFileConsumer.CHANGE_EDIT ) )
                stat = ScmFileStatus.MODIFIED;
            if ( change.equals( ChangedFileConsumer.CHANGE_ADD ) )
                stat = ScmFileStatus.ADDED;
            changedFiles.add( new ScmFile( getLocalPath(), stat ) );
            values.clear();
        }
    }

    public List getChangedFiles()
    {
        if ( values.size() > 0 )
        {
            extractChangedFile();
        }
        return changedFiles;
    }

    private String getChange()
    {
        return (String) values.get( KEY_CHANGE );
    }

    private String getLocalPath()
    {
        String local = (String) values.get( KEY_LOCAL_ITEM );
        if ( local != null )
        {
            local = local.split( "]", 2 )[1].trim();
        }
        return local;
    }

}
