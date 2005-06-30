package org.apache.maven.scm.log;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
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
