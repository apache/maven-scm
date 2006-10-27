package org.apache.maven.scm.provider.hg.command.status;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommand;

import java.io.File;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgStatusCommand
    extends AbstractStatusCommand
    implements HgCommand
{

    public HgStatusCommand()
    {
        super();
    }

    public StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {

        File workingDir = fileSet.getBasedir();
        HgStatusConsumer consumer = new HgStatusConsumer( getLogger(), workingDir );
        String[] statusCmd = new String[] { STATUS_CMD };
        ScmResult result = HgUtils.execute( consumer, getLogger(), workingDir, statusCmd );

        return new StatusScmResult( consumer.getStatus(), result );
    }
}
