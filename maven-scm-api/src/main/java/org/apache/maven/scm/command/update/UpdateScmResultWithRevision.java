/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.command.update;

import java.util.List;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmResult;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 *
 */
public class UpdateScmResultWithRevision extends UpdateScmResult {
    private static final long serialVersionUID = 7644079089026359667L;

    private String revision;

    public UpdateScmResultWithRevision(
            String commandLine, String providerMessage, String commandOutput, String revision, boolean success) {
        super(commandLine, providerMessage, commandOutput, success);

        this.revision = revision;
    }

    public UpdateScmResultWithRevision(String commandLine, List<ScmFile> updatedFiles, String revision) {
        super(commandLine, updatedFiles);

        this.revision = revision;
    }

    public UpdateScmResultWithRevision(
            List<ScmFile> updatedFiles, List<ChangeSet> changes, String revision, ScmResult result) {
        super(updatedFiles, changes, result);

        this.revision = revision;
    }

    public String getRevision() {
        return revision;
    }
}
