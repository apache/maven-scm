package org.apache.maven.scm.provider.jazz.command.status;

import static org.junit.Assert.*;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.log.ScmLogger;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class JazzStatusConsumerTest
{
    private JazzStatusConsumer statusConsumer;

    @Mock
    private ScmLogger scmLogger;

    @Before
    public void initMocks()
    {
        MockitoAnnotations.initMocks( this );
    }

    @Ignore
    // @todo fix JazzStatusConsumer to match the ScmFile restrictions
    public void testScmFilePath()
    {
        statusConsumer = new JazzStatusConsumer( null, scmLogger );
        statusConsumer.consumeLine( "      d-- /BogusTest/release.properties" );
        assertNotNull( statusConsumer.getChangedFiles() );
        assertEquals( 1, statusConsumer.getChangedFiles().size() );
        ScmFile changedFile = statusConsumer.getChangedFiles().get( 0 );
        assertEquals( "BogusTest/release.properties", changedFile.getPath() );
    }
}
