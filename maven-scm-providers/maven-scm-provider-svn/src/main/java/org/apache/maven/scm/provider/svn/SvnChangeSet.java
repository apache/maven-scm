package org.apache.maven.scm.provider.svn;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;

import java.util.Iterator;

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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeSet
    extends ChangeSet
{
    public boolean containsFilename( String filename, ScmProviderRepository repository )
    {
        SvnScmProviderRepository repo = (SvnScmProviderRepository) repository;

        String url = repo.getUrl();

        if ( !url.endsWith( "/" ) )
        {
            url += "/";
        }

        String currentFile = url + filename;

        if ( getFiles() != null )
        {
            for ( Iterator i = getFiles().iterator(); i.hasNext(); )
            {
                ChangeFile file = (ChangeFile) i.next();

                if ( currentFile.endsWith( file.getName() ) )
                {
                    return true;
                }
            }
        }

        return false;
    }
}
