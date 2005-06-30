package org.apache.maven.scm.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.plugin.AbstractMojo;
import org.codehaus.plexus.scm.ScmManager;

import java.io.File;

/**
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractScmMojo
    extends AbstractMojo
{
    /**
     * @parameter expression="${basedir}"
     * @required
     */
    private File workingDirectory;

    /**
     * @parameter expression="${component.org.codehaus.plexus.scm.ScmManager}"
     * @required
     * @readonly
     */
    private ScmManager manager;

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public ScmManager getScmManager()
    {
        return manager;
    }
}
