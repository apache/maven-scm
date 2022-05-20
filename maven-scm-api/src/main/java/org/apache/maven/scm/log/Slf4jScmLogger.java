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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * {@link ScmLogger} backed by SLF4J.
 *
 * @since 2.0.0-M2
 */
public class Slf4jScmLogger
        implements ScmLogger
{
    private final Logger logger;

    public Slf4jScmLogger( final Class<?> owner )
    {
        this( LoggerFactory.getLogger( requireNonNull( owner ) ) );
    }

    public Slf4jScmLogger( final Logger logger )
    {
        this.logger = requireNonNull( logger );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDebugEnabled()
    {
        return this.logger.isDebugEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug( String content )
    {
        this.logger.debug( content );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug( String content, Throwable error )
    {
        this.logger.debug( content, error );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void debug( Throwable error )
    {
        this.logger.debug( "", error );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInfoEnabled()
    {
        return this.logger.isInfoEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info( String content )
    {
        this.logger.info( content );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info( String content, Throwable error )
    {
        this.logger.info( content, error );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void info( Throwable error )
    {
        this.logger.info( "", error );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWarnEnabled()
    {
        return this.logger.isWarnEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn( String content )
    {
        this.logger.warn( content );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn( String content, Throwable error )
    {
        this.logger.warn( content, error );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void warn( Throwable error )
    {
        this.logger.warn( "", error );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorEnabled()
    {
        return this.logger.isErrorEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error( String content )
    {
        this.logger.error( content );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error( String content, Throwable error )
    {
        this.logger.error( content, error );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void error( Throwable error )
    {
        this.logger.error( "", error );
    }
}
