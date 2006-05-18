package org.apache.maven.scm.command.status;

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

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class StatusScmResult
    extends ScmResult
{
    private List changedFiles;

    public StatusScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );

        this.changedFiles = Collections.EMPTY_LIST;
    }

    public StatusScmResult( String commandLine, List changedFiles )
    {
        super( commandLine, null, null, true );

        if ( changedFiles == null )
        {
            throw new NullPointerException( "changedFiles can't be null." );
        }

        this.changedFiles = changedFiles;
    }

    public StatusScmResult( List changedFiles, ScmResult result )
    {
        super( result );

        if ( changedFiles == null )
        {
            throw new NullPointerException( "changedFiles can't be null." );
        }

        this.changedFiles = changedFiles;
    }

    public List getChangedFiles()
    {
        return changedFiles;
    }
}
