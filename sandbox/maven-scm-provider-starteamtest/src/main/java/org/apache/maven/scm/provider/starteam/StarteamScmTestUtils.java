package org.apache.maven.scm.provider.starteam;

import java.io.File;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

import com.starbase.starteam.Folder;
import com.starbase.starteam.Project;
import com.starbase.starteam.StarTeamFinder;
import com.starbase.starteam.StarTeamURL;
import com.starbase.starteam.View;

public class StarteamScmTestUtils 
{
    public static String testScmUrl;
        
	public static String getTestScmUrl()
	{
		if ( testScmUrl == null )
		{
			testScmUrl = System.getProperty( "maven.scm.provider.starteamtest.url","scm:starteam:Administrator:Administrator@somehost:49201/replay/junk" ); 
		}
		
		return testScmUrl;
	}

	public static File getIniatialImportTestDataDirectory( String basedir )
	{
		return new File( basedir, "src/test/initial-import" );
	}
	
	public static void initRepo( ScmManager scmManager, String scmUrl, File workingCopy, File initialImportDirectory )
	    throws Exception
	{   		
        FileUtils.copyDirectoryStructure( initialImportDirectory, workingCopy );

        // now we need to remove all internal .svn
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( workingCopy );
        scanner.setIncludes( DirectoryScanner.DEFAULTEXCLUDES );
        scanner.scan();
        String[] dirs = scanner.getIncludedDirectories();
        for ( int i = 0; i < dirs.length; ++i )
        {
            FileUtils.forceDelete( ( new File( workingCopy, dirs[i] ) ) );
        }

        String starteamUrl = scmUrl.substring( 13 );
        Project project = StarTeamFinder.openProject( starteamUrl );
        
        Assert.assertNotNull( "Unable to find Starteam project in: " + starteamUrl,  project );  
        
        View view = StarTeamFinder.openView( starteamUrl );
        
        if ( view != null )
        {
            view.remove();
        }

        View rootView = project.getDefaultView();
        
        //StarTeamURL starteamUrl = new StarTeamURL( starteamUrl );
        
        String viewName = new StarTeamURL( starteamUrl ).getFolders().nextToken();
        
        view = new View( rootView, viewName, "testView", "/tmp/tmp" );
        
        view.update();
        
            createFolderTree( view.getRootFolder(), workingCopy );
            importTree( scmManager, scmUrl, view.getRootFolder(), workingCopy );
                
	}	
	
    /**
     * create folder tree based on datadir tree
     * @param folder
     */
    private static void createFolderTree( Folder parentFolder,  File dataDir )
    {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setBasedir( dataDir );
        scanner.setExcludes( DirectoryScanner.DEFAULTEXCLUDES );
        scanner.setIncludes( new String [] {"*"} );
        scanner.scan();
        String [] dirs = scanner.getIncludedDirectories();
        for ( int i = 0; i < dirs.length; ++i )
        {
            Folder folder = new Folder( parentFolder, dirs[i], dirs[i] );
            folder.update();
            createFolderTree( folder, new File( dataDir, dirs[i] ) );
        }
    }	
    
    /**
     * import an directory into starteam
     * @param folder
     */
    private static void importTree( ScmManager scmManager, String scmUrl, Folder parentFolder,  File dataDir ) throws Exception
    {
        ScmFileSet scmFileSet = new ScmFileSet( dataDir, "**", null );
        
        ScmRepository mavenScmRepository = scmManager.makeScmRepository( scmUrl );
        ScmProvider provider = scmManager.getProviderByUrl( scmUrl );
        
        provider.add(  mavenScmRepository, scmFileSet );
        
    }        
    
}
