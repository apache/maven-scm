package org.apache.maven.scm.provider.bazaar.repository;

import java.io.File;

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
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
