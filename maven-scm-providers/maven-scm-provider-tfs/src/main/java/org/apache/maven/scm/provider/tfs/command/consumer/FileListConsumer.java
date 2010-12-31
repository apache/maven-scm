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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class FileListConsumer
    implements StreamConsumer
{

    private boolean fed = false;

    protected String currentDir = "";

    private List<ScmFile> files = new ArrayList<ScmFile>();

    public void consumeLine( String line )
    {
        fed = true;
        if ( line.endsWith( ":" ) )
        {
            currentDir = line.substring( 0, line.lastIndexOf( ':' ) );
            ScmFile scmFile = new ScmFile( currentDir, ScmFileStatus.CHECKED_OUT );
            if ( !files.contains( scmFile ) )
            {
                files.add( scmFile );
            }
        }
        else if ( line.trim().equals( "" ) )
        {
            currentDir = "";
        }
        else if ( !currentDir.equals( "" ) && line.indexOf( " " ) >= 0 )
        {
            String filename = line.split( " " )[1];
            files.add( getScmFile( filename ) );
        }
        else
        {
            files.add( getScmFile( line ) );
        }
    }

    protected ScmFile getScmFile( String filename )
    {
        return new ScmFile( new File( currentDir, filename ).getAbsolutePath(), ScmFileStatus.CHECKED_OUT );
    }

    public List<ScmFile> getFiles()
    {
        return files;
    }

    public boolean hasBeenFed()
    {
        return fed;
    }
}
