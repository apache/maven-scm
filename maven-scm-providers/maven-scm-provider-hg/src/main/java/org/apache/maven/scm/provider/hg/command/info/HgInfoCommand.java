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
package org.apache.maven.scm.provider.hg.command.info;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;

/**
 * @author Olivier Lamy
 * @since 1.5
 */
public class HgInfoCommand extends AbstractCommand<InfoScmResult> {

    @Override
    protected InfoScmResult executeCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
        String[] revCmd = new String[] {HgCommandConstants.REVNO_CMD, "-i"};
        HgInfoConsumer consumer = new HgInfoConsumer();
        ScmResult scmResult = HgUtils.execute(consumer, fileSet.getBasedir(), revCmd);
        return new InfoScmResult(consumer.getInfoItems(), scmResult);
    }
}
