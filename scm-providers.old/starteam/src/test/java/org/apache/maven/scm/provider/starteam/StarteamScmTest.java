package org.apache.maven.scm.provider.starteam;

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
import org.apache.maven.scm.provider.starteam.command.StarteamCommandWrapper;
import org.apache.maven.scm.provider.starteam.command.changelog.StarteamChangeLogCommand;
import org.apache.maven.scm.provider.starteam.repository.StarteamRepository;
import org.apache.maven.scm.ScmTestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamScmTest
    extends ScmTestCase
{
    public StarteamScmTest(String name)
    {
        super(name);
    }
    
    public void setupRepository()
    {
        repositoryInfo.setPassword("myPassword");
    }

    protected String getRepositoryDelimiter()
    {
        return ":";
    }

    protected String getSupportedScm()
    {
        return "starteam";
    }

    protected String getRepositoryUrl()
    {
        return "scm:starteam:myusername@myhost:1234/projecturl";
    }

    protected String getRepositoryClassName()
    {
        return "org.apache.maven.scm.provider.starteam.repository.StarteamRepository";
    }

    protected String getCommandWrapperClassName()
    {
        return "org.apache.maven.scm.provider.starteam.command.StarteamCommandWrapper";
    }
}