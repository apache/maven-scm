package org.apache.maven.scm;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ScmResult
{
    public boolean success;

    public String message;

    public String longMessage;

    public static class Failure
        extends ScmResult
    {
        public Failure()
        {
            super.setSuccess( false );
        }
    }

    public ScmResult()
    {
        success = true;
    }

    public ScmResult( String message, String longMessage )
    {
        this.message = message;

        this.longMessage = longMessage;

        success = false;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess( boolean success )
    {
        this.success = success;
    }

    public String getMessage()
    {
        return message;
    }

    public String getLongMessage()
    {
        return longMessage;
    }
}
