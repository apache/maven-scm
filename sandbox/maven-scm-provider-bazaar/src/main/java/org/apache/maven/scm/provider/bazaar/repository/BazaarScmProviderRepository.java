package org.apache.maven.scm.provider.bazaar.repository;

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

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarScmProviderRepository
    extends ScmProviderRepository
{
    private final String uri;

    public BazaarScmProviderRepository( String url )
    {
        this.uri = toURI( url );
    }

    public String getURI()
    {
        return uri;
    }

    private String toURI( String orgURL )
    {
        String newURI = orgURL;

        //Only file urls needs special handling
        if ( orgURL.startsWith( "file" ) )
        {
            newURI = orgURL.substring( "file://".length() );
            String fileSeparator = System.getProperty( "file.separator" );
            newURI = StringUtils.replace( newURI, "/", fileSeparator );
            File tmpFile = new File( newURI );
            String newURI2 = newURI.substring( fileSeparator.length() );
            File tmpFile2 = new File( newURI2 );
            if ( !tmpFile.exists() && !tmpFile2.exists() )
            {
                // TODO make fail fast
            }

            newURI = tmpFile2.exists() ? newURI2 : newURI;
        }

        return newURI;
    }
}
