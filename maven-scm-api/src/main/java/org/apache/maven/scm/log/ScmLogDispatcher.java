package org.apache.maven.scm.log;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ScmLogDispatcher
    implements ScmLogger
{
    private List listeners = new ArrayList();

    public void addListener( ScmLogger logger )
    {
        listeners.add( logger );
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#debug(java.lang.String, java.lang.Throwable)
     */
    public void debug( String content, Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.debug( content, error );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#debug(java.lang.String)
     */
    public void debug( String content )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.debug( content );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#debug(java.lang.Throwable)
     */
    public void debug( Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.debug( error );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#error(java.lang.String, java.lang.Throwable)
     */
    public void error( String content, Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.error( content, error );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#error(java.lang.String)
     */
    public void error( String content )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.error( content );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#error(java.lang.Throwable)
     */
    public void error( Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.error( error );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#info(java.lang.String, java.lang.Throwable)
     */
    public void info( String content, Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.info( content, error );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#info(java.lang.String)
     */
    public void info( String content )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.info( content );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#info(java.lang.Throwable)
     */
    public void info( Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.info( error );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            if ( logger.isDebugEnabled() )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isErrorEnabled()
     */
    public boolean isErrorEnabled()
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            if ( logger.isErrorEnabled() )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isInfoEnabled()
     */
    public boolean isInfoEnabled()
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            if ( logger.isInfoEnabled() )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#isWarnEnabled()
     */
    public boolean isWarnEnabled()
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            if ( logger.isWarnEnabled() )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#warn(java.lang.String, java.lang.Throwable)
     */
    public void warn( String content, Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.warn( content, error );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#warn(java.lang.String)
     */
    public void warn( String content )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.warn( content );
        }
    }

    /**
     * @see org.apache.maven.scm.log.ScmLogger#warn(java.lang.Throwable)
     */
    public void warn( Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.warn( error );
        }
    }

}
