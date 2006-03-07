package org.apache.maven.scm.command.add;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of adding files to the SCM
 * 
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class AddScmResult
    extends ScmResult
{
    private List addedFiles;

    public AddScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );

        addedFiles = new ArrayList( 0 );
    }

    public AddScmResult( String commandLine, List addedFiles )
    {
        super( commandLine, null, null, true );

        if ( addedFiles == null )
        {
            throw new NullPointerException( "addedFiles can't be null" );
        }

        this.addedFiles = addedFiles;
    }

    public AddScmResult( List addedFiles, ScmResult result )
    {
        super( result );

        if ( addedFiles == null )
        {
            throw new NullPointerException( "addedFiles can't be null" );
        }

        this.addedFiles = addedFiles;
    }

    /**
     * List with all the added files in the SCM operation
     * 
     * @return non null list of added files
     */
    public List getAddedFiles()
    {
        return addedFiles;
    }
}
