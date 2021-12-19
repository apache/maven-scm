package org.apache.maven.scm.provider.git.command.remove;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.codehaus.plexus.util.FileUtils;

/**
 * This test tests the remove command.
 *
 * @author Georg Tsakumagos
 */
public abstract class RemoveCommandTckTest
    extends ScmTckTestCase
{
    /** {@inheritDoc} */
    public void initRepo()
        throws Exception
    {
        GitScmTestUtils.initRepo( "src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory() );
    }
    
    public void testCommandRemoveWithFile()
                    throws Exception
    {
        File workingDirectory = createTempDirectory();

        File toBeRemoved = new File( workingDirectory.getAbsolutePath() + File.separator + "toto.xml" );
        FileUtils.fileAppend( toBeRemoved.getAbsolutePath(), "data" );

        ScmFileSet fileSet =  new ScmFileSet( getWorkingCopy(), toBeRemoved);
        RemoveScmResult removeResult = getScmManager().remove( getScmRepository(), fileSet, getBasedir() );
        assertResultIsSuccess( removeResult );

        FileUtils.deleteDirectory( workingDirectory );
    }

    public void testCommandRemoveWithDirectory()
        throws Exception
    {
        File workingDirectory = createTempDirectory();

        File toBeRemoved = new File( workingDirectory.getAbsolutePath() + File.separator + "toto" );
        toBeRemoved.mkdir();

        ScmFileSet fileSet =  new ScmFileSet( getWorkingCopy(), toBeRemoved);
        RemoveScmResult removeResult = getScmManager().remove( getScmRepository(), fileSet, getBasedir() );
        assertResultIsSuccess( removeResult );
        
        FileUtils.deleteDirectory( workingDirectory );
    }

    public void testCommandRemoveWithTwoDirectory()
        throws Exception
    {
        File workingDirectory = createTempDirectory();

        File toBeRemoved1 = new File( workingDirectory.getAbsolutePath() + File.separator + "toto" );
        toBeRemoved1.mkdir();

        File toBeRemoved2 = new File( workingDirectory.getAbsolutePath() + File.separator + "tata" );
        toBeRemoved2.mkdir();

        ScmFileSet fileSet =  new ScmFileSet( getWorkingCopy(), Arrays.asList( toBeRemoved1, toBeRemoved2 ));
        RemoveScmResult removeResult = getScmManager().remove( getScmRepository(), fileSet, getBasedir() );
        assertResultIsSuccess( removeResult );
        FileUtils.deleteDirectory( workingDirectory );
    }

    private File createTempDirectory()
        throws IOException
    {
        File dir = File.createTempFile( "gitexe", "test" );
        dir.delete();
        dir.mkdir();
        return dir;
    }
}
