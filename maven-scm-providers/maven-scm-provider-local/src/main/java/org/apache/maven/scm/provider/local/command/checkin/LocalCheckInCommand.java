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
package org.apache.maven.scm.provider.local.command.checkin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.LocalCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.apache.maven.scm.util.FilenameUtils;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class LocalCheckInCommand extends AbstractCheckInCommand implements LocalCommand {
    /** {@inheritDoc} */
    protected CheckInScmResult executeCheckInCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, String message, ScmVersion version) throws ScmException {
        LocalScmProviderRepository repository = (LocalScmProviderRepository) repo;

        if (version != null && StringUtils.isNotEmpty(version.getName())) {
            throw new ScmException("The local scm doesn't support tags.");
        }

        File root = new File(repository.getRoot());

        String module = repository.getModule();

        File source = new File(root, module);

        File basedir = fileSet.getBasedir();

        if (!basedir.exists()) {
            throw new ScmException("The working directory doesn't exist (" + basedir.getAbsolutePath() + ").");
        }

        if (!root.exists()) {
            throw new ScmException("The base directory doesn't exist (" + root.getAbsolutePath() + ").");
        }

        if (!source.exists()) {
            throw new ScmException("The module directory doesn't exist (" + source.getAbsolutePath() + ").");
        }

        List<ScmFile> checkedInFiles = new ArrayList<>();

        try {
            // Only copy files newer than in the repo
            File repoRoot = new File(repository.getRoot(), repository.getModule());

            List<File> files = fileSet.getFileList();

            if (files.isEmpty()) {
                files = FileUtils.getFiles(basedir, "**", null, false);
            }

            for (File file : files) {
                String path = FilenameUtils.normalizeFilename(file.getPath());
                File repoFile = new File(repoRoot, path);
                file = new File(basedir, path);

                ScmFileStatus status;

                if (repoFile.exists()) {
                    String repoFileContents = FileUtils.fileRead(repoFile);

                    String fileContents = FileUtils.fileRead(file);

                    if (logger.isDebugEnabled()) {
                        logger.debug("fileContents:" + fileContents);
                        logger.debug("repoFileContents:" + repoFileContents);
                    }
                    if (fileContents.equals(repoFileContents)) {
                        continue;
                    }

                    status = ScmFileStatus.CHECKED_IN;
                } else if (repository.isFileAdded(path)) {
                    status = ScmFileStatus.CHECKED_IN;
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn("skipped unknown file in checkin:" + path);
                    }
                    // unknown file, skip
                    continue;
                }

                FileUtils.copyFile(file, repoFile);
                ScmFile scmFile = new ScmFile(path, status);
                logger.info(scmFile.toString());
                checkedInFiles.add(scmFile);
            }
        } catch (IOException ex) {
            throw new ScmException("Error while checking in the files.", ex);
        }

        return new CheckInScmResult(null, checkedInFiles);
    }
}
