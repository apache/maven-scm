package org.apache.maven.scm;

import java.io.File;
import java.util.Arrays;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class ScmFileSet
{
    private File basedir;

    private ScmFile[] files;

    public ScmFileSet( File basedir )
    {
        this.basedir = basedir;
        this.files = null;
    }

    public ScmFileSet( File basedir, String includes, String excludes )
    {
        this.basedir = basedir;

        // TODO: resolve files
    }

    public ScmFileSet( File basedir, ScmFile[] files )
    {
        this.basedir = basedir;
        this.files = files;
    }

    public File getBasedir()
    {
        return basedir;
    }

    public ScmFile[] getFiles()
    {
        return this.files;
    }

    public String toString() {
        return "basedir = " + basedir + "; files = " + Arrays.asList(files);
    }
}
