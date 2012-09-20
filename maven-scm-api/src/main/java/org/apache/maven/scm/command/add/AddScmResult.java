package org.apache.maven.scm.command.add;

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
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

/**
 * Result of adding files to the SCM
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class AddScmResult
    extends ScmResult
{
    private static final long serialVersionUID = 1L;
    
    private List<ScmFile> addedFiles;

    public AddScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );

        addedFiles = new ArrayList<ScmFile>( 0 );
    }

    public AddScmResult( String commandLine, List<ScmFile> addedFiles )
    {
        this( commandLine, null, null, true );

        if ( addedFiles == null )
        {
            throw new NullPointerException( "addedFiles can't be null" );
        }

        this.addedFiles = addedFiles;
    }

    public AddScmResult( List<ScmFile> addedFiles, ScmResult result )
    {
        super( result );

        if ( addedFiles == null )
        {
            throw new NullPointerException( "addedFiles can't be null" );
        }

        this.addedFiles = addedFiles;
    }

    /**
     * List with all the added files in the SCM operation.
     *
     * @return non null list of added files
     */
    public List<ScmFile> getAddedFiles()
    {
        return addedFiles;
    }
}
