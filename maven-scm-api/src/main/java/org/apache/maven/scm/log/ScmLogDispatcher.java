package org.apache.maven.scm.log;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    /** {@inheritDoc} */
    public void debug( String content, Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.debug( content, error );
        }
    }

    /** {@inheritDoc} */
    public void debug( String content )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.debug( content );
        }
    }

    /** {@inheritDoc} */
    public void debug( Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.debug( error );
        }
    }

    /** {@inheritDoc} */
    public void error( String content, Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.error( content, error );
        }
    }

    /** {@inheritDoc} */
    public void error( String content )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.error( content );
        }
    }

    /** {@inheritDoc} */
    public void error( Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.error( error );
        }
    }

    /** {@inheritDoc} */
    public void info( String content, Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            if ( logger.isInfoEnabled() )
            {
                logger.info( content, error );
            }
        }
    }

    /** {@inheritDoc} */
    public void info( String content )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            if ( logger.isInfoEnabled() )
            {
                logger.info( content );
            }
        }
    }

    /** {@inheritDoc} */
    public void info( Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            if ( logger.isInfoEnabled() )
            {
                logger.info( error );
            }
        }
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public void warn( String content, Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.warn( content, error );
        }
    }

    /** {@inheritDoc} */
    public void warn( String content )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.warn( content );
        }
    }

    /** {@inheritDoc} */
    public void warn( Throwable error )
    {
        for ( Iterator i = listeners.iterator(); i.hasNext(); )
        {
            ScmLogger logger = (ScmLogger) i.next();

            logger.warn( error );
        }
    }
}
