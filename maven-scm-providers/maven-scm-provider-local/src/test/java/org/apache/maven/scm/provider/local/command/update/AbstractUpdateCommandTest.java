package org.apache.maven.scm.provider.local.command.update;

/*
 * LICENSE
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.FileUtils;

/**
 * This test tests the update command.
 * 
 * It works like this:
 * 
 * <ol>
 *  <li>Check out the files to directory getWorkingCopy().
 *  <li>Check out the files to directory getUpdatingCopy().
 *  <li>Change the files in getWorkingCopy().
 *  <li>Commit the files in getWorkingCopy(). Note that the provider <b>must</b> not 
 *      use the check in command as it can be guaranteed to work as it's not yet tested.
 *  <li>Use the update command in getUpdatingCopy() to assert that the files
 *      that was supposed to be updated actually was updated.
 * </ol>
 * 
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractUpdateCommandTest
	extends ScmTestCase
{
    // ----------------------------------------------------------------------
    // Methods the test has to implement
    // ----------------------------------------------------------------------

    public abstract String getScmUrl( File workingDirectory, String moduleName )
    	throws Exception;

    /**
     * Copy the existing checked in repository to the working directory.
     * 
     * (src/test/repository/my-cvs-repository)
     * 
     * @throws Exception
     */
    public abstract void initRepo( File workingDirectory, String moduleName )
		throws Exception;

    /**
     * Checks out the files from the repositorty.
     * 
     * The checked out file system must look like this:
     * <ul>
     *   <li><code>pom.xml</code>
     *   <li><code>readme.txt</code>
     *   <li><code>src/main/java/Application.java</code>
     *   <li><code>src/test/java/Test.java</code>
     *   <li><code>src/test/resources</code>
     * </ul>
     * 
     * @throws Exception
     */
    public abstract void checkOut( File workingDirectory, String moduleName )
    	throws Exception;

    public abstract void commit( File workingDirectory, ScmRepository repository )
		throws Exception;

    // ----------------------------------------------------------------------
    // Directories the test must use
    // ----------------------------------------------------------------------

    private File getRepositoryRoot()
    {
        return new File( getTestFile( "target/scm-test" ), "repository" );
    }

    private File getWorkingCopy()
    {
        return new File( getTestFile( "target/scm-test" ), "working-copy" );
    }

    private File getUpdatingCopy()
    {
        return new File( getTestFile( "target/scm-test" ), "updating-copy" );
    }

    protected String getModule()
    {
        return "test-repo";
    }

    // ----------------------------------------------------------------------
    // The test implementation
    // ----------------------------------------------------------------------

    public void setUp()
    	throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( getRepositoryRoot() );

        FileUtils.deleteDirectory( getWorkingDirectory() );

        FileUtils.deleteDirectory( getUpdatingCopy() );
    }

    public void testUpdateCommand()
    	throws Exception
    {
        initRepo( getRepositoryRoot(), getModule() );

        checkOut( getWorkingCopy(), getModule() );

        checkOut( getUpdatingCopy(), getModule() );

        // ----------------------------------------------------------------------
        // Assert that the required files is there
        // ----------------------------------------------------------------------

        assertFile( getWorkingCopy(), getModule() + "/pom.xml" );

        assertFile( getWorkingCopy(), getModule() + "/readme.txt" );

        assertFile( getWorkingCopy(), getModule() + "/src/main/java/Application.java" );

        assertFile( getWorkingCopy(), getModule() + "/src/test/java/Test.java" );

        assertDirectory( getWorkingCopy(), getModule() + "/src/test/resources" );

        // ----------------------------------------------------------------------
        // Change the files
        // ----------------------------------------------------------------------

        /*
         * readme.txt is changed (changed file in the root directory)
         * project.xml is added (added file in the root directory)
         * src/test/resources is untouched (a empty directory is left untouched)
         * src/test/java is untouched (a non empty directory is left untouched)
         * src/test/java/org (a empty directory is added)
         * src/main/java/org/Foo.java (a non empty directory is added)
         */

        makeFile( getWorkingCopy(), getModule() + "/readme.txt", "changed readme.txt" );

        makeFile( getWorkingCopy(), getModule() + "/project.xml", "changed project.xml" );

        makeDirectory( getWorkingCopy(), getModule() + "/src/test/java/org" );

        makeFile( getWorkingCopy(), getModule() + "/src/main/java/org/Foo.java" );

        ScmManager scmManager = getScmManager();

        ScmRepository scmRepository = scmManager.makeScmRepository( getScmUrl( getRepositoryRoot(), getModule() ) );

        commit( getWorkingCopy(), scmRepository );

        // ----------------------------------------------------------------------
        // Update the project
        // ----------------------------------------------------------------------

        UpdateScmResult result = scmManager.update( scmRepository, getUpdatingCopy(), null );

        assertNotNull( "The command returned a null result.", result );

        assertTrue( "The command wasn't a success.", result.isSuccess() );

        assertNull( "The message wasn't null", result.getMessage() );

        assertNull( "The long message wasn't null", result.getLongMessage() );

        List updatedFiles = result.getUpdatedFiles();

        assertTrue( "Expected 3 files in the updated files list, was " + updatedFiles.size(), updatedFiles.size() == 3 );

        // ----------------------------------------------------------------------
        // Assert the files in the updated files list
        // ----------------------------------------------------------------------

        Iterator files = new TreeSet( updatedFiles ).iterator();

        ScmFile file = (ScmFile) files.next();

        assertEquals( "/" + getModule() + "/src/main/java/org/Foo.java", file.getPath() );

        assertEquals( ScmFileStatus.ADDED, file.getStatus() );

        file = (ScmFile) files.next();

        assertEquals( "/" + getModule() + "/readme.txt", file.getPath() );

        assertEquals( ScmFileStatus.UPDATED, file.getStatus() );

        file = (ScmFile) files.next();

        assertEquals( "/" + getModule() + "/project.xml", file.getPath() );

        assertEquals( ScmFileStatus.ADDED, file.getStatus() );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    private void assertFile( File root, String fileName )
    	throws Exception
    {
        File file = new File( root, fileName );

        assertTrue( "Missing file: '" + file.getAbsolutePath() + "'.", file.exists() );

        assertTrue( "File isn't a file: '" + file.getAbsolutePath() + "'.", file.isFile() );

        String expected = fileName;

        String actual = FileUtils.fileRead( file );

        assertEquals( "The file doesn't contain the expected contents. File: " + file.getAbsolutePath() + ", expected: '" + expected + "', actual: '" + actual + "'.", expected, actual );
    }

    private void assertDirectory( File root, String fileName )
    {
        File file = new File( root, fileName );

        assertTrue( "Missing file: '" + file.getAbsolutePath() + "'.", file.exists() );

        assertTrue( "File isn't a directory: '" + file.getAbsolutePath() + "'.", file.isDirectory() );
    }

    // ----------------------------------------------------------------------
    // Util methods
    // ----------------------------------------------------------------------

    protected void makeDirectory( File basedir, String fileName )
    {
        File dir = new File( basedir, fileName );

        if ( !dir.exists() )
        {
            assertTrue( dir.mkdirs() );
        }
    }

    protected void makeFile( File basedir, String fileName )
    	throws IOException
    {
        makeFile( basedir, fileName, fileName );
    }

    protected void makeFile( File basedir, String fileName, String contents )
    	throws IOException
    {
        File file = new File( basedir, fileName );

        File parent = file.getParentFile();

        if ( !parent.exists() )
        {
            assertTrue( parent.mkdirs() );
        }

        FileWriter writer = new FileWriter( file );

        writer.write( contents );

        writer.close();
    }
}
