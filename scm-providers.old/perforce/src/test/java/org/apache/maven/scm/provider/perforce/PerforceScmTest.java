package org.apache.maven.scm.provider.perforce;

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

import junit.framework.TestCase;

import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.CommandWrapper;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.repository.Repository;
import org.apache.maven.scm.repository.RepositoryInfo;
import org.apache.maven.scm.provider.perforce.command.PerforceCommandWrapper;
import org.apache.maven.scm.provider.perforce.command.changelog.PerforceChangeLogCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceRepository;
import org.apache.maven.scm.ScmTestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class PerforceScmTest
    extends ScmTestCase
{
    public PerforceScmTest(String name)
    {
        super(name);
    }

    protected String getRepositoryDelimiter()
    {
        return ":";
    }

    protected String getSupportedScm()
    {
        return "perforce";
    }

    protected String getRepositoryUrl()
    {
        return "scm:perforce://depot/projects/name/";
    }

    protected String getRepositoryClassName()
    {
        return "org.apache.maven.scm.provider.perforce.repository.PerforceRepository";
    }

    protected String getCommandWrapperClassName()
    {
        return "org.apache.maven.scm.provider.perforce.command.PerforceCommandWrapper";
    }
}