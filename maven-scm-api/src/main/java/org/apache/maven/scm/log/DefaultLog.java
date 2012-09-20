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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class DefaultLog
    implements ScmLogger
{

    /** {@inheritDoc} */
    public boolean isDebugEnabled()
    {
        return false;
    }

    /** {@inheritDoc} */
    public void debug( String content )
    {
    }

    /** {@inheritDoc} */
    public void debug( String content, Throwable error )
    {
    }

    /** {@inheritDoc} */
    public void debug( Throwable error )
    {
    }

    /** {@inheritDoc} */
    public boolean isInfoEnabled()
    {
        return true;
    }

    /** {@inheritDoc} */
    public void info( String content )
    {
        System.out.println( content );
    }

    /** {@inheritDoc} */
    public void info( String content, Throwable error )
    {
        System.out.println( content );
        error.printStackTrace();
    }

    /** {@inheritDoc} */
    public void info( Throwable error )
    {
        error.printStackTrace();
    }

    /** {@inheritDoc} */
    public boolean isWarnEnabled()
    {
        return true;
    }

    /** {@inheritDoc} */
    public void warn( String content )
    {
        System.out.println( content );
    }

    /** {@inheritDoc} */
    public void warn( String content, Throwable error )
    {
        System.out.println( content );
        error.printStackTrace();
    }

    /** {@inheritDoc} */
    public void warn( Throwable error )
    {
        error.printStackTrace();
    }

    /** {@inheritDoc} */
    public boolean isErrorEnabled()
    {
        return true;
    }

    /** {@inheritDoc} */
    public void error( String content )
    {
        System.out.print( "[ERROR] " + content );
    }

    /** {@inheritDoc} */
    public void error( String content, Throwable error )
    {
        System.out.println( "[ERROR] " + content );
        error.printStackTrace();
    }

    /** {@inheritDoc} */
    public void error( Throwable error )
    {
        error.printStackTrace();
    }
}
