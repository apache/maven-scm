package org.apache.maven.scm;

import org.codehaus.plexus.PlexusTestCase;
import org.apache.maven.scm.repository.RepositoryInfo;
import org.apache.maven.scm.repository.Repository;
import org.apache.maven.scm.command.CommandWrapper;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @version $Id$
 */
public abstract class ScmTestCase
    extends PlexusTestCase
{
    protected Scm scm;

    protected RepositoryInfo repositoryInfo;

    protected Repository repository;

    protected CommandWrapper commandWrapper;

    public ScmTestCase( String name )
    {
        super( name );
    }

    protected abstract String getSupportedScm();

    protected abstract String getRepositoryUrl();

    protected abstract String getRepositoryClassName();

    protected abstract String getCommandWrapperClassName();

    protected abstract String getRepositoryDelimiter();

    protected void setupRepository()
    {
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected void setUp()
        throws Exception
    {
        super.setUp();

        scm = (Scm) lookup( Scm.ROLE, getSupportedScm() );

        repositoryInfo = new RepositoryInfo( getRepositoryUrl() );

        setupRepository();

        repository = scm.createRepository( repositoryInfo );

        commandWrapper = scm.createCommandWrapper( repositoryInfo );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testRepositoryDelimiter()
    {
        assertEquals( getRepositoryDelimiter(), repositoryInfo.getDelimiter() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testGetSupportedScm()
    {
        assertEquals( getSupportedScm(), scm.getSupportedScm() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    protected boolean isRepositoryClassValid()
    {
        return repository.getClass().getName().equals( getRepositoryClassName() );
    }

    protected boolean isCommandWrapperClassValid()
    {
        return commandWrapper.getClass().getName().equals( getCommandWrapperClassName() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testCreateRepository()
    {
        try
        {
            repository = scm.createRepository( repositoryInfo );

            if ( !isRepositoryClassValid() )
            {
                fail( "Wrong repository class" );
            }

            assertEquals( repositoryInfo.getDelimiter(), repository.getDelimiter() );

            assertEquals( repositoryInfo.getPassword(), repository.getPassword() );

            assertEquals( repositoryInfo.getConnection(), repository.getConnection() );
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testCreateCommandWrapper()
    {
        try
        {
            if ( !isCommandWrapperClassValid() )
            {
                fail( "Wrong commandWrapper class" );
            }

            Repository repo = commandWrapper.getRepository();

            if ( !isRepositoryClassValid() )
            {
                fail( "Wrong repository class" );
            }

            assertEquals( repositoryInfo.getDelimiter(), repo.getDelimiter() );

            assertEquals( repositoryInfo.getPassword(), repo.getPassword() );

            assertEquals( repositoryInfo.getConnection(), repo.getConnection() );
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testCreateRepositoryWithWrongScmType()
    {
        try
        {
            RepositoryInfo repoInfo = new RepositoryInfo();

            repoInfo.setUrl( "scm:badscmtype:anUrl" );

            scm.createRepository( repoInfo );

            fail( "Exception should be caught while trying to create a repository with invalid repository information" );
        }
        catch ( Exception e )
        {
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testCreateCommand()
    {
        try
        {
            Command cmd = scm.createCommand( repositoryInfo, ChangeLogCommand.NAME );

            Repository repo = cmd.getRepository();

            if ( !isRepositoryClassValid() )
            {
                fail( "Wrong repository class" );
            }

            assertEquals( repositoryInfo.getDelimiter(), repo.getDelimiter() );

            assertEquals( repositoryInfo.getPassword(), repo.getPassword() );

            assertEquals( repositoryInfo.getConnection(), repo.getConnection() );
        }
        catch ( Exception e )
        {
            fail( e.getMessage() );
        }
    }
}