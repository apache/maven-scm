package org.apache.maven.scm.provider.svn;

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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class SvnChangeSet
    extends ChangeSet
{

    private static final long serialVersionUID = -4454710577968060741L;

    public SvnChangeSet()
    {
        super();
    }

    public SvnChangeSet( String strDate, String userDatePattern, String comment, String author,
                         List<ChangeFile> files )
    {
        super( strDate, userDatePattern, comment, author, files );
    }

    public SvnChangeSet( Date date, String comment, String author, List<ChangeFile> files )
    {
        super( date, comment, author, files );
    }

    /** {@inheritDoc} */
    public boolean containsFilename( String filename, ScmProviderRepository repository )
    {
        SvnScmProviderRepository repo = (SvnScmProviderRepository) repository;

        String url = repo.getUrl();

        if ( !url.endsWith( "/" ) )
        {
            url += "/";
        }

        String currentFile = url + StringUtils.replace( filename, "\\", "/" );

        if ( getFiles() != null )
        {
            for ( Iterator<ChangeFile> i = getFiles().iterator(); i.hasNext(); )
            {
                ChangeFile file = i.next();

                if ( currentFile.endsWith( StringUtils.replace( file.getName(), "\\", "/" ) ) )
                {
                    return true;
                }
            }
        }

        return false;
    }
}
