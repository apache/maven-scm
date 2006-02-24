package org.apache.maven.scm.command.checkin;

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

import java.util.List;


/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CheckInScmResult
    extends ScmResult
{
    private List checkedInFiles;

    public CheckInScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public CheckInScmResult( String commandLine, List checkedInFiles )
    {
        super( commandLine, null, null, true );

        this.checkedInFiles = checkedInFiles;
    }

    public CheckInScmResult(List checkedInFiles, ScmResult result)
    {
    	super(result);

    	this.checkedInFiles = checkedInFiles;
    }

    public List getCheckedInFiles()
    {
        return checkedInFiles;
    }
}
