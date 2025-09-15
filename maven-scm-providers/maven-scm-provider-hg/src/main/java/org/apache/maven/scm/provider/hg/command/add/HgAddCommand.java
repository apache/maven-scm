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
package org.apache.maven.scm.provider.hg.command.add;

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;

/**
 * Add no recursive.
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgAddCommand extends AbstractAddCommand implements Command {
    /**
     * {@inheritDoc}
     */
    protected ScmResult executeAddCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, String message, boolean binary) throws ScmException {
        // String[] addCmd = new String[] { ADD_CMD, NO_RECURSE_OPTION };
        String[] addCmd = new String[] {HgCommandConstants.ADD_CMD, HgCommandConstants.VERBOSE_OPTION};
        addCmd = HgUtils.expandCommandLine(addCmd, fileSet);

        File workingDir = fileSet.getBasedir();
        HgAddConsumer consumer = new HgAddConsumer(workingDir);
        ScmResult result = HgUtils.execute(consumer, workingDir, addCmd);

        AddScmResult addScmResult = new AddScmResult(consumer.getAddedFiles(), result);

        // add in bogus 'added' results for empty directories.  only need to do this because the maven scm unit test
        // framework seems to think that this is the way we should behave.  it's pretty hacky. -rwd
        for (File workingFile : fileSet.getFileList()) {
            File file = new File(workingDir + "/" + workingFile.getPath());
            if (file.isDirectory() && file.listFiles().length == 0) {
                addScmResult.getAddedFiles().add(new ScmFile(workingFile.getPath(), ScmFileStatus.ADDED));
            }
        }

        return addScmResult;
    }
}
