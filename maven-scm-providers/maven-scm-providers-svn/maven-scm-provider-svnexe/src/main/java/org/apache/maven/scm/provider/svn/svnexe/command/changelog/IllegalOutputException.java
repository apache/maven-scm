package org.apache.maven.scm.provider.svn.svnexe.command.changelog;

/**
 * Thrown when the output of an svn log command isn't recognized.
 */
public class IllegalOutputException
    extends RuntimeException
{

    /**
     * Create the exception with a message.
     * 
     * @param message the message.
     */
    public IllegalOutputException( final String message )
    {
        super( message );
    }
}
