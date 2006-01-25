package org.apache.maven.scm.command.edit;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * 
 */
public abstract class AbstractEditCommand
    extends AbstractCommand
{
    public ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        return executeEditCommand( repository, fileSet );
    }

    protected abstract ScmResult executeEditCommand( ScmProviderRepository repository, ScmFileSet fileSet )
        throws ScmException;
}
