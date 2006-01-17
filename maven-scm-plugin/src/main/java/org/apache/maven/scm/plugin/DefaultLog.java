package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.scm.log.ScmLogger;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class DefaultLog
    implements ScmLogger
{
    private Log logger;

    public DefaultLog( Log logger )
    {
        this.logger = logger;
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#debug(java.lang.String)
     */
    public void debug( String content )
    {
        logger.debug( content );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug( String content, Throwable error )
    {
        logger.debug( content, error );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#debug(java.lang.Throwable)
     */
    public void debug( Throwable error )
    {
        logger.debug( error );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isInfoEnabled()
     */
    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#info(java.lang.String)
     */
    public void info( String content )
    {
        logger.info( content );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#info(java.lang.String, java.lang.Throwable)
     */
    public void info( String content, Throwable error )
    {
        logger.info( content, error );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#info(java.lang.Throwable)
     */
    public void info( Throwable error )
    {
        logger.info( error );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isWarnEnabled()
     */
    public boolean isWarnEnabled()
    {
        return logger.isWarnEnabled();
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#warn(java.lang.String)
     */
    public void warn( String content )
    {
        logger.warn( content );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#warn(java.lang.String, java.lang.Throwable)
     */
    public void warn( String content, Throwable error )
    {
        logger.warn( content, error );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#warn(java.lang.Throwable)
     */
    public void warn( Throwable error )
    {
        logger.warn( error );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isErrorEnabled()
     */
    public boolean isErrorEnabled()
    {
        return logger.isErrorEnabled();
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#error(java.lang.String)
     */
    public void error( String content )
    {
        logger.error( content );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#error(java.lang.String, java.lang.Throwable)
     */
    public void error( String content, Throwable error )
    {
        logger.error( content, error );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#error(java.lang.Throwable)
     */
    public void error( Throwable error )
    {
        logger.error( error );
    }
}
