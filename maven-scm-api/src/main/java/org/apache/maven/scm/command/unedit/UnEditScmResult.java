package org.apache.maven.scm.command.unedit;

import org.apache.maven.scm.ScmResult;

import java.util.List;

/**
 * 
 */
public class UnEditScmResult
    extends ScmResult
{
    private List unEditFiles;

    public UnEditScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public UnEditScmResult( String commandLine, List unEditFiles )
    {
        super( commandLine, null, null, true );

        this.unEditFiles = unEditFiles;
    }

    public List getUnEditFiles()
    {
        return unEditFiles;
    }

}
