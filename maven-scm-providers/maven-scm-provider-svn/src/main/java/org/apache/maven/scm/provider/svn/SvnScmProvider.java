package org.apache.maven.scm.provider.svn;

/*
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
 */

import java.util.Map;

import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnScmProvider
    extends AbstractScmProvider
{
    /** @requirement org.apache.maven.scm.CvsCommand */
    private Map commands;

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, String delimiter )
    	throws ScmRepositoryException
    {
        int at = scmSpecificUrl.indexOf("@");

        String url;
        String user = null;
        String password = null;

        try
        {
            if (at >= 1)
            {
                user = scmSpecificUrl.substring( 0, at );
                url = scmSpecificUrl.substring( at + 1);
            }
            else
            {
                url = scmSpecificUrl;
            }
        }
        catch ( Exception e )
        {
            throw new ScmRepositoryException( e.getMessage() );
        }

        return new SvnScmProviderRepository( url, user, password );
    }

    // ----------------------------------------------------------------------
    // AbstractScmProvider Implementation
    // ----------------------------------------------------------------------

    protected Map getCommands()
    {
        return commands;
    }

    public String getScmType()
    {
        return "svn";
    }
}
