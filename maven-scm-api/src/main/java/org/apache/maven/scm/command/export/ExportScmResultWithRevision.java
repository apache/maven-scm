package org.apache.maven.scm.command.export;

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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class ExportScmResultWithRevision
    extends ExportScmResult
{
    private static final long serialVersionUID = -7962912849216079039L;

    private String revision;

    public ExportScmResultWithRevision( String commandLine, String providerMessage, String commandOutput,
                                        String revision, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );

        this.revision = revision;
    }

    public ExportScmResultWithRevision( String commandLine, List<ScmFile> exportedFiles, String revision )
    {
        super( commandLine, exportedFiles );

        this.revision = revision;
    }

    public String getRevision()
    {
        return revision;
    }
}
