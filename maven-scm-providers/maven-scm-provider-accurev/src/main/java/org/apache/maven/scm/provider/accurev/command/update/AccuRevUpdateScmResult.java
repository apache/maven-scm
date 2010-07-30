package org.apache.maven.scm.provider.accurev.command.update;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;

/**
 * Carry information about before and after transaction ids so we can run the changelog
 * 
 * @author ggardner
 */
public class AccuRevUpdateScmResult
    extends UpdateScmResultWithRevision
{
    ;

    private static final long serialVersionUID = -4896981432286000329L;
    String fromRevision;

    /**
     * Failed constructor
     * 
     * @param commandLine
     * @param providerMessage
     * @param commandOutput
     */
    public AccuRevUpdateScmResult( String commandLine, String providerMessage, String commandOutput,
                                   String fromRevision, String toRevision, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, toRevision, success );
        this.fromRevision = fromRevision;
    }

    /**
     * Success constructor
     * 
     * @param startVersion
     * @param endVersion
     * @param commandLines
     * @param updatedFiles
     */
    public AccuRevUpdateScmResult( String commandLines,List<ScmFile> updatedFiles,String fromRevision, String toRevision  )
    {
        super( commandLines, updatedFiles, toRevision );
        this.fromRevision = fromRevision;
    }

    public String getFromRevision()
    {
        return fromRevision;
    }

    public String getToRevision()
    {
        return getRevision();
    }
}