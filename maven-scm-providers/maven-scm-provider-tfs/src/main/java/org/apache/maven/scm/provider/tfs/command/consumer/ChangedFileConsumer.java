package org.apache.maven.scm.provider.tfs.command.consumer;

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
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class ChangedFileConsumer
    implements StreamConsumer
{

    private ScmLogger logger;

    private static final String KEY_CHANGE = "Change";

    private static final String KEY_LOCAL_ITEM = "Local item";

    private static final String CHANGE_EDIT = "edit";

    private static final String CHANGE_ADD = "add";

    private Map<String,String> values = new HashMap<String,String>();

    private List<ScmFile> changedFiles = new ArrayList<ScmFile>();

    public ChangedFileConsumer( ScmLogger logger )
    {
        this.logger = logger;
    }

    public void consumeLine( String line )
    {
        if ( line.indexOf( ":" ) >= 0 )
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

    public List<ScmFile> getChangedFiles()
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
