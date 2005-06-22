/**
 * 
 */
package org.apache.maven.scm.log;

/**
 * @author Emmanuel Venisse
 *
 */
public class DefaultLog
    implements ScmLogger
{

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return false;
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#debug(java.lang.String)
     */
    public void debug( String content )
    {
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug( String content, Throwable error )
    {
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#debug(java.lang.Throwable)
     */
    public void debug( Throwable error )
    {
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isInfoEnabled()
     */
    public boolean isInfoEnabled()
    {
        return true;
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#info(java.lang.String)
     */
    public void info( String content )
    {
        System.out.println(content);
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#info(java.lang.String, java.lang.Throwable)
     */
    public void info( String content, Throwable error )
    {
        System.out.println(content);
        error.printStackTrace();
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#info(java.lang.Throwable)
     */
    public void info( Throwable error )
    {
        error.printStackTrace();
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isWarnEnabled()
     */
    public boolean isWarnEnabled()
    {
        return true;
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#warn(java.lang.String)
     */
    public void warn( String content )
    {
        System.out.println(content);
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#warn(java.lang.String, java.lang.Throwable)
     */
    public void warn( String content, Throwable error )
    {
        System.out.println(content);
        error.printStackTrace();
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#warn(java.lang.Throwable)
     */
    public void warn( Throwable error )
    {
        error.printStackTrace();
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isErrorEnabled()
     */
    public boolean isErrorEnabled()
    {
        return true;
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#error(java.lang.String)
     */
    public void error( String content )
    {
        System.out.print("[ERROR] " + content);
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#error(java.lang.String, java.lang.Throwable)
     */
    public void error( String content, Throwable error )
    {
        System.out.println( "[ERROR] " + content);
        error.printStackTrace();
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#error(java.lang.Throwable)
     */
    public void error( Throwable error )
    {
        error.printStackTrace();
    }

}
