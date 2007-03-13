package org.apache.maven.scm.command.list;

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

import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.provider.ScmProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of {@link ScmProvider#list} operation
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ListScmResult
    extends ScmResult
{
    // XXX List of what?
    private List files;

    public ListScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );

        files = new ArrayList( 0 );
    }

    public ListScmResult( String commandLine, List/*<ScmFile>*/ files )
    {
        super( commandLine, null, null, true );

        this.files = files;
    }

    public ListScmResult( List/*<ScmFile>*/ files, ScmResult result )
    {
        super( result );

        this.files = files;
    }

    // XXX List of what?
    public List/*<ScmFile>*/ getFiles()
    {
        return files;
    }

}
