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
package org.apache.maven.scm.provider.hg.command.tag;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.command.inventory.HgListConsumer;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;

/**
 * Tag
 *
 * @author <a href="mailto:ryan@darksleep.com">ryan daum</a>
 * @author Olivier Lamy
 *
 */
public class HgTagCommand extends AbstractTagCommand {

    protected TagScmResult executeTagCommand(
            ScmProviderRepository scmProviderRepository, ScmFileSet fileSet, String tag, String message)
            throws ScmException {
        return executeTagCommand(scmProviderRepository, fileSet, tag, new ScmTagParameters(message));
    }

    /**
     * {@inheritDoc}
     */
    protected TagScmResult executeTagCommand(
            ScmProviderRepository scmProviderRepository,
            ScmFileSet fileSet,
            String tag,
            ScmTagParameters scmTagParameters)
            throws ScmException {

        if (tag == null || tag.trim().isEmpty()) {
            throw new ScmException("tag must be specified");
        }

        if (!fileSet.getFileList().isEmpty()) {
            throw new ScmException(
                    "This provider doesn't support tagging subsets of a directory : " + fileSet.getFileList());
        }

        File workingDir = fileSet.getBasedir();

        // build the command
        String[] tagCmd = new String[] {
            HgCommandConstants.TAG_CMD, HgCommandConstants.MESSAGE_OPTION, scmTagParameters.getMessage(), tag
        };

        // keep the command about in string form for reporting
        StringBuilder cmd = joinCmd(tagCmd);
        HgTagConsumer consumer = new HgTagConsumer();
        ScmResult result = HgUtils.execute(consumer, workingDir, tagCmd);
        HgScmProviderRepository repository = (HgScmProviderRepository) scmProviderRepository;
        if (result.isSuccess()) {
            // now push
            // Push to parent branch if any

            if (repository.isPushChanges()) {
                if (!repository.getURI().equals(fileSet.getBasedir().getAbsolutePath())) {
                    String branchName = HgUtils.getCurrentBranchName(workingDir);
                    boolean differentOutgoingBranch = HgUtils.differentOutgoingBranchFound(workingDir, branchName);

                    String[] pushCmd = new String[] {
                        HgCommandConstants.PUSH_CMD,
                        differentOutgoingBranch ? HgCommandConstants.REVISION_OPTION + branchName : null,
                        repository.getURI()
                    };

                    result = HgUtils.execute(new HgConsumer(), fileSet.getBasedir(), pushCmd);
                }
            }
        } else {
            throw new ScmException("Error while executing command " + cmd.toString());
        }

        // do an inventory to return the files tagged (all of them)
        String[] listCmd = new String[] {HgCommandConstants.INVENTORY_CMD};
        HgListConsumer listconsumer = new HgListConsumer();
        result = HgUtils.execute(listconsumer, fileSet.getBasedir(), listCmd);
        if (result.isSuccess()) {
            List<ScmFile> files = listconsumer.getFiles();
            List<ScmFile> fileList = new ArrayList<>();
            for (ScmFile f : files) {
                if (!f.getPath().endsWith(".hgtags")) {
                    fileList.add(new ScmFile(f.getPath(), ScmFileStatus.TAGGED));
                }
            }

            return new TagScmResult(fileList, result);
        } else {
            throw new ScmException("Error while executing command " + cmd.toString());
        }
    }

    private StringBuilder joinCmd(String[] cmd) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < cmd.length; i++) {
            String s = cmd[i];
            result.append(s);
            if (i < cmd.length - 1) {
                result.append(" ");
            }
        }
        return result;
    }
}
