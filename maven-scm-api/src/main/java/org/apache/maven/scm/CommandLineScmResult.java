package org.apache.maven.scm;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CommandLineScmResult
    extends ScmResult
{
    private int exitCode;

    public CommandLineScmResult( int exitCode )
    {
        super.setSuccess( exitCode == 0 );

        this.exitCode = exitCode;
    }

    public int getExitCode()
    {
        return exitCode;
    }
}
