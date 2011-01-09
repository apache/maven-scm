package org.apache.maven.scm.command.mkdir;

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

import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

/**
 * Result of making directories in SCM.
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version $Id$
 */
public class MkdirScmResult
    extends ScmResult
{

    private static final long serialVersionUID = -8717329738246682608L;

    private String revision;
    
    private List<ScmFile> createdDirs;
   
    public MkdirScmResult( ScmResult scmResult )
    {
        super( scmResult );
    }

    public MkdirScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public MkdirScmResult( String commandLine, String revision )
    {
        this( commandLine, null, null, true );

        this.revision = revision;
    }
    
    public MkdirScmResult( String commandLine, List<ScmFile> createdDirs )
    {
        this( commandLine, null, null, true );
        
        this.createdDirs = createdDirs;
    }

    public MkdirScmResult( String revision, ScmResult result )
    {
        super( result );

        this.revision = revision;
    }
    
    public MkdirScmResult( List<ScmFile> createdDirs, ScmResult result )
    {
        super( result );
        
        this.createdDirs = createdDirs;
    }

    public String getRevision()
    {
        return revision;
    }
    
    public List<ScmFile> getCreatedDirs()
    {
        return createdDirs;
    }
}
