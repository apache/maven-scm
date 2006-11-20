package org.apache.maven.scm.provider.starteam.command.update;

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.starteam.StarteamScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.update.UpdateCommandTckTest;


public class StarteamUpdateCommandTckTest 
    extends UpdateCommandTckTest 
{
	public String getScmUrl()
	{
		return StarteamScmTestUtils.getTestScmUrl();
	}
	
    public void initRepo() throws Exception
    {
    	File initialImportDirectory =  StarteamScmTestUtils.getIniatialImportTestDataDirectory( getBasedir() );
    	
    	StarteamScmTestUtils.initRepo( getScmManager(), getScmUrl(), getWorkingCopy(), initialImportDirectory );
    }
    

}
