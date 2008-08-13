package org.apache.maven.scm.command.unedit;

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

import java.util.List;

/**
 * @version $Id$
 */
public class UnEditScmResult
    extends ScmResult
{
    private List unEditFiles;

    public UnEditScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public UnEditScmResult( String commandLine, List unEditFiles )
    {
        super( commandLine, null, null, true );

        this.unEditFiles = unEditFiles;
    }

    public UnEditScmResult( List unEditFiles, ScmResult result )
    {
        super( result );

        this.unEditFiles = unEditFiles;
    }

    public List getUnEditFiles()
    {
        return unEditFiles;
    }

}
