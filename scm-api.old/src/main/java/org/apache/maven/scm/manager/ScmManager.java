package org.apache.maven.scm.manager;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.Scm;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.CommandWrapper;
import org.apache.maven.scm.repository.Repository;
import org.apache.maven.scm.repository.RepositoryInfo;

public interface ScmManager
{
    String ROLE = ScmManager.class.getName();

    void setRepositoryInfo( String scmUrl )
        throws ScmException;

    void setRepositoryInfo( RepositoryInfo repoInfo )
        throws ScmException;

    void checkout( String directory )
        throws Exception;

    void update( String directory )
        throws Exception;

    Command getCommand( String commandName )
        throws ScmException;
}