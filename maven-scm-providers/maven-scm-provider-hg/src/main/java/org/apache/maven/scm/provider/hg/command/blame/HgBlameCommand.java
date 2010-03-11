package org.apache.maven.scm.provider.hg.command.blame;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.blame.AbstractBlameCommand;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;

/**
 * @author Evgeny Mandrikov
 * @since 1.4
 */
public class HgBlameCommand
    extends AbstractBlameCommand
{
    public static final String BLAME_CMD = "blame";

    /**
     * {@inheritDoc}
     */
    public BlameScmResult executeBlameCommand( ScmProviderRepository repo, ScmFileSet workingDirectory,
                                               String filename )
        throws ScmException
    {
        String[] cmd = new String[]{BLAME_CMD, "--user",   // list the author
            "--date",   // list the date
            "--number", // list the revision number
            filename};
        HgBlameConsumer consumer = new HgBlameConsumer( getLogger() );
        ScmResult result = HgUtils.execute( consumer, getLogger(), workingDirectory.getBasedir(), cmd );
        return new BlameScmResult( consumer.getLines(), result );
    }
}
