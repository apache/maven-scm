package org.apache.maven.scm;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class NoSuchCommandScmException
    extends ScmException
{
    private String commandName;

    public NoSuchCommandScmException( String commandName )
    {
        super( "No such command '" + commandName + "'." );
    }

    public String getCommandName()
    {
        return commandName;
    }
}
