package org.apache.maven.scm.command.edit;

import org.apache.maven.scm.ScmResult;

import java.util.List;

/**
 * 
 */
public class EditScmResult extends ScmResult
{
    private List editFiles;

    public EditScmResult( String commandLine, String providerMessage, String commandOutput, boolean success )
    {
        super( commandLine, providerMessage, commandOutput, success );
    }

    public EditScmResult( String commandLine, List editFiles )
    {
        super( commandLine, null, null, true );

        this.editFiles = editFiles;
    }

    public List getEditFiles()
    {
        return editFiles;
    }

}
