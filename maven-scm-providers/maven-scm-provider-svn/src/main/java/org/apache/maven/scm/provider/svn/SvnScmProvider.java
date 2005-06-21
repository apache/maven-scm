package org.apache.maven.scm.provider.svn;

/*
 * Copyright 2003-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
    //
    // ----------------------------------------------------------------------

    private static class ScmUrlParserResult
    {
        List messages = new ArrayList();

        ScmProviderRepository repository;
    }

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
    	throws ScmRepositoryException
    {
        ScmUrlParserResult result = parseScmUrl( scmSpecificUrl );

        if ( result.messages.size() > 0 )
        {
            throw new ScmRepositoryException( "The scm url is invalid.", result.messages );
        }

        return result.repository;
    }

    public List validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        ScmUrlParserResult result = parseScmUrl( scmSpecificUrl );

        return result.messages;
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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ScmUrlParserResult parseScmUrl( String scmSpecificUrl )
    {
        ScmUrlParserResult result = new ScmUrlParserResult();

        int at = scmSpecificUrl.indexOf( "@" );

        String url;

        String user = null;

        String password = null;

        if ( at >= 1 )
        {
            user = scmSpecificUrl.substring( 0, at );

            url = scmSpecificUrl.substring( at + 1 );
        }
        else
        {
            url = scmSpecificUrl;
        }

        // ----------------------------------------------------------------------
        // Do some sanity checking of the SVN url
        // ----------------------------------------------------------------------

        // todo: this could possibly be generalized for all providers.
        if ( url.startsWith( "file" ) )
        {
            if ( !url.startsWith( "file:///" ) && !url.startsWith( "file://localhost/" ) )
            {
                result.messages.add( "A svn 'file' url must be on the form 'file:///' or 'file://localhost/'." );

                return result;
            }
        }
        else if ( url.startsWith( "https" ) )
        {
            if ( !url.startsWith( "https://" ) )
            {
                result.messages.add( "A svn 'http' url must be on the form 'https://'." );

                return result;
            }
        }
        else if ( url.startsWith( "http" ) )
        {
            if ( !url.startsWith( "http://" ) )
            {
                result.messages.add( "A svn 'http' url must be on the form 'http://'." );

                return result;
            }
        }
        else if ( url.startsWith( "svn+ssh" ) )
        {
            if ( !url.startsWith( "svn+ssh://" ) )
            {
                result.messages.add( "A svn 'svn+ssh' url must be on the form 'svn+ssh://'." );

                return result;
            }
        }
        else if ( url.startsWith( "svn" ) )
        {
            if ( !url.startsWith( "svn://" ) )
            {
                result.messages.add( "A svn 'svn' url must be on the form 'svn://'." );

                return result;
            }
        }

        result.repository = new SvnScmProviderRepository( url, user, password );

        return result;
    }
}
