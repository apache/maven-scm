package org.apache.maven.scm.provider.hg.command.remove;

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
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommand;

import java.io.File;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgRemoveCommand
    extends AbstractRemoveCommand
    implements HgCommand
{
    protected ScmResult executeRemoveCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {

        String[] command = new String[] { REMOVE_CMD };
        HgUtils.expandCommandLine( command, fileSet );

        File workingDir = fileSet.getBasedir();
        HgRemoveConsumer consumer = new HgRemoveConsumer( getLogger(), workingDir );

        ScmResult result = HgUtils.execute( consumer, getLogger(), workingDir, command );
        return new RemoveScmResult( consumer.getRemovedFiles(), result );
    }
}
