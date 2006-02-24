package org.apache.maven.scm.command.diff;

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
import java.util.Map;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class DiffScmResult
    extends ScmResult
{
    private List changedFiles;

    private Map differences;

    private String patch;

    public DiffScmResult( String commandLine, List changedFiles, Map differences, String patch )
    {
        this( commandLine, null, null, true );
        this.changedFiles = changedFiles;
        this.differences = differences;
        this.patch = patch;
    }

    public DiffScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public DiffScmResult(List changedFiles, Map differences, String patch, ScmResult result)
    {
    	super(result);

    	this.changedFiles = changedFiles;

    	this.differences = differences;

    	this.patch = patch;
    }

    public List getChangedFiles()
    {
        return changedFiles;
    }

    public Map getDifferences()
    {
        return differences;
    }

    public String getPatch()
    {
        return patch;
    }
}
