package org.apache.maven.scm.provider.local.metadata;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.providers.local.metadata.LocalScmMetadata;
import org.apache.maven.scm.providers.local.metadata.io.xpp3.LocalScmMetadataXpp3Reader;
import org.apache.maven.scm.providers.local.metadata.io.xpp3.LocalScmMetadataXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Utils for dealing with LocalScmMetadata
 * 
 * @author <a href="mailto:arne@degenring.de">Arne Degenring</a>
 * @version $Id$
 */
public class LocalScmMetadataUtils
{
    /** The name of the metadata file */
    public static final String FILENMAE = ".maven-scm-local";

    protected final ScmLogger logger;

    public LocalScmMetadataUtils( ScmLogger logger )
    {
        this.logger = logger;
    }

    /**
     * Builds LocalScmMetadata based on contents of repository
     */
    public LocalScmMetadata buildMetadata( File repository ) throws IOException
    {
        List repoFilenames = FileUtils.getFileNames( repository.getAbsoluteFile(), "**", null, false );
        LocalScmMetadata metadata = new LocalScmMetadata();
        metadata.setRepositoryFileNames( repoFilenames );
        return metadata;
    }

    /**
     * Writes metadata file
     */
    public void writeMetadata( File destinationDir, LocalScmMetadata metadata ) throws IOException
    {
        File metadataFile = new File( destinationDir, FILENMAE );
        metadataFile.createNewFile();
        Writer writer = new FileWriter( metadataFile );
        try
        {
            new LocalScmMetadataXpp3Writer().write( writer, metadata );
        }
        finally
        {
            IOUtil.close( writer );
        }
    }

    /**
     * Reads metadata file from given directory.
     * 
     * @param dir
     *            The directory that should contain the metadata file
     * @return LocalScmMetadata or <tt>null</tt> in case of problems
     */
    public LocalScmMetadata readMetadata( File dir )
    {
        File metadataFile = new File( dir, FILENMAE );
        if ( !metadataFile.exists() )
        {
            return null;
        }
        LocalScmMetadata result = null;
        Reader reader = null;
        try
        {
            reader = new FileReader( metadataFile );
            result = new LocalScmMetadataXpp3Reader().read( reader );
        }
        catch ( XmlPullParserException e )
        {
            logger.warn( "Could not interpret .maven-scm-local - ignoring", e );
            return null;
        }
        catch ( IOException e )
        {
            logger.warn( "Could not Read .maven-scm-local - ignoring", e );
        }
        finally
        {
            IOUtil.close( reader );
        }
        return result;
    }

}
