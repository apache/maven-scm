package org.apache.maven.scm.provider.starteam.command.checkin;

import java.io.File;

import org.apache.maven.scm.provider.starteam.StarteamScmTestUtils;
import org.apache.maven.scm.tck.command.checkin.CheckInCommandTckTest;


public class StarteamCheckinCommandTckTest 
    extends CheckInCommandTckTest 
{
	public String getScmUrl()
	{
		return StarteamScmTestUtils.getTestScmUrl();
	}
	
    public void initRepo() throws Exception
    {
    	File initialImportDirectory = new File( getBasedir(), "src/test/initial-import" );
    	StarteamScmTestUtils.initRepo( getScmManager(), getScmUrl(), getWorkingCopy(), initialImportDirectory );
    }
}
