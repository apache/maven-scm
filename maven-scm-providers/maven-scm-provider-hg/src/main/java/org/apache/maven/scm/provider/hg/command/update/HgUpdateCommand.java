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
package org.apache.maven.scm.provider.hg.command.update;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.command.changelog.HgChangeLogCommand;
import org.apache.maven.scm.provider.hg.command.diff.HgDiffConsumer;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @author Olivier Lamy
 *
 */
public class HgUpdateCommand extends AbstractUpdateCommand {
    /** {@inheritDoc} */
    protected UpdateScmResult executeUpdateCommand(ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion tag)
            throws ScmException {
        File workingDir = fileSet.getBasedir();

        String[] updateCmd;
        // Update branch
        if (repo.isPushChanges()) {
            updateCmd = new String[] {
                HgCommandConstants.PULL_CMD,
                HgCommandConstants.REVISION_OPTION,
                tag != null && !StringUtils.isEmpty(tag.getName()) ? tag.getName() : "tip"
            };
        } else {
            updateCmd = new String[] {
                HgCommandConstants.UPDATE_CMD,
                tag != null && !StringUtils.isEmpty(tag.getName()) ? tag.getName() : "tip",
                HgCommandConstants.CLEAN_OPTION
            };
        }
        ScmResult updateResult = HgUtils.execute(new HgConsumer(), workingDir, updateCmd);

        if (!updateResult.isSuccess()) {
            return new UpdateScmResult(null, null, updateResult);
        }

        // Find changes from last revision
        int currentRevision = HgUtils.getCurrentRevisionNumber(workingDir);
        int previousRevision = currentRevision - 1;
        String[] diffCmd =
                new String[] {HgCommandConstants.DIFF_CMD, HgCommandConstants.REVISION_OPTION, "" + previousRevision};
        HgDiffConsumer diffConsumer = new HgDiffConsumer(workingDir);
        ScmResult diffResult = HgUtils.execute(diffConsumer, workingDir, diffCmd);

        // Now translate between diff and update file status
        List<ScmFile> updatedFiles = new ArrayList<>();
        List<CharSequence> changes = new ArrayList<>();
        List<ScmFile> diffFiles = diffConsumer.getChangedFiles();
        Map<String, CharSequence> diffChanges = diffConsumer.getDifferences();
        for (ScmFile file : diffFiles) {
            changes.add(diffChanges.get(file.getPath()));
            if (file.getStatus() == ScmFileStatus.MODIFIED) {
                updatedFiles.add(new ScmFile(file.getPath(), ScmFileStatus.PATCHED));
            } else {
                updatedFiles.add(file);
            }
        }

        if (repo.isPushChanges()) {
            String[] hgUpdateCmd = new String[] {HgCommandConstants.UPDATE_CMD};
            HgUtils.execute(new HgConsumer(), workingDir, hgUpdateCmd);
        }

        return new UpdateScmResultWithRevision(
                updatedFiles, new ArrayList<>(0), String.valueOf(currentRevision), diffResult);
    }

    protected ChangeLogCommand getChangeLogCommand() {
        return new HgChangeLogCommand();
    }
}
