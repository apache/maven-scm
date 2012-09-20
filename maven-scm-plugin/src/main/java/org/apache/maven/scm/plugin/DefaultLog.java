package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.scm.log.ScmLogger;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class DefaultLog
    implements ScmLogger
{
    private Log logger;

    public DefaultLog( Log logger )
    {
        this.logger = logger;
    }

    /** {@inheritDoc} */
    public boolean isDebugEnabled()
    {
        return logger.isDebugEnabled();
    }

    /** {@inheritDoc} */
    public void debug( String content )
    {
        logger.debug( content );
    }

    /** {@inheritDoc} */
    public void debug( String content, Throwable error )
    {
        logger.debug( content, error );
    }

    /** {@inheritDoc} */
    public void debug( Throwable error )
    {
        logger.debug( error );
    }

    /** {@inheritDoc} */
    public boolean isInfoEnabled()
    {
        return logger.isInfoEnabled();
    }

    /** {@inheritDoc} */
    public void info( String content )
    {
        logger.info( content );
    }

    /** {@inheritDoc} */
    public void info( String content, Throwable error )
    {
        logger.info( content, error );
    }

    /** {@inheritDoc} */
    public void info( Throwable error )
    {
        logger.info( error );
    }

    /** {@inheritDoc} */
    public boolean isWarnEnabled()
    {
        return logger.isWarnEnabled();
    }

    /** {@inheritDoc} */
    public void warn( String content )
    {
        logger.warn( content );
    }

    /** {@inheritDoc} */
    public void warn( String content, Throwable error )
    {
        logger.warn( content, error );
    }

    /** {@inheritDoc} */
    public void warn( Throwable error )
    {
        logger.warn( error );
    }

    /** {@inheritDoc} */
    public boolean isErrorEnabled()
    {
        return logger.isErrorEnabled();
    }

    /** {@inheritDoc} */
    public void error( String content )
    {
        logger.error( content );
    }

    /** {@inheritDoc} */
    public void error( String content, Throwable error )
    {
        logger.error( content, error );
    }

    /** {@inheritDoc} */
    public void error( Throwable error )
    {
        logger.error( error );
    }
}
