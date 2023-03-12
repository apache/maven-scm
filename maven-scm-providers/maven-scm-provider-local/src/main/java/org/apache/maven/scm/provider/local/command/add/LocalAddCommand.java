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
package org.apache.maven.scm.provider.local.command.add;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.LocalCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class LocalAddCommand extends AbstractAddCommand implements LocalCommand {
    /** {@inheritDoc} */
    protected ScmResult executeAddCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, String message, boolean binary) throws ScmException {
        LocalScmProviderRepository localRepo = (LocalScmProviderRepository) repository;

        List<ScmFile> fileList = new ArrayList<ScmFile>();
        for (File file : fileSet.getFileList()) {
            String path = file.getPath().replace('\\', '/');
            localRepo.addFile(path);
            fileList.add(new ScmFile(path, ScmFileStatus.ADDED));
        }

        // TODO: Also, ensure it is tested from the update test
        return new AddScmResult(null, fileList);
    }
}
