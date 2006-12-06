package org.apache.maven.scm.manager;

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

import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderStub;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.ScmRepositoryStub;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Stub implementation of ScmManager for unit testing purposes. It allows setting the expected results that the different methods will return.
 * More information about Stubs on <a href="http://martinfowler.com/bliki/TestDouble.html">Martin Fowler's TestDouble</a>
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ScmManagerStub
    implements ScmManager
{

    private ScmRepository scmRepository;

    private ScmProvider scmProvider;

    private List messages;

    /**
     * Creates a new stub with stub repository and provider, and empty list of messages
     */
    public ScmManagerStub()
    {
        setScmRepository( new ScmRepositoryStub() );
        setScmProvider( new ScmProviderStub() );
        setMessages( new ArrayList( 0 ) );
    }

    public void setScmProvider( ScmProvider scmProvider )
    {
        this.scmProvider = scmProvider;
    }

    public ScmProvider getScmProvider()
    {
        return scmProvider;
    }

    public void setScmRepository( ScmRepository scmRepository )
    {
        this.scmRepository = scmRepository;
    }

    public ScmRepository getScmRepository()
    {
        return scmRepository;
    }

    /**
     * Set the messages to return in validateScmRepository
     *
     * @param messages <code>List</code> of <code>String</code> objects
     */
    public void setMessages( List messages )
    {
        this.messages = messages;
    }

    /**
     * Get the messages to return in validateScmRepository
     *
     * @return <code>List</code> of <code>String</code> objects
     */
    public List getMessages()
    {
        return messages;
    }

    /**
     * @return getScmRepository()
     */
    public ScmRepository makeScmRepository( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        return getScmRepository();
    }

    /**
     * @return getScmRepository()
     */
    public ScmRepository makeProviderScmRepository( String providerType, File path )
        throws ScmRepositoryException, UnknownRepositoryStructure, NoSuchScmProviderException
    {
        return getScmRepository();
    }

    /**
     * Returns the same list as getMessages()
     *
     * @param scmUrl ignored
     * @return <code>List</code> of <code>String</code> objects, the same list returned by getMessages()
     */
    public List validateScmRepository( String scmUrl )
    {
        return getMessages();
    }

    /**
     * @return getScmProvider()
     */
    public ScmProvider getProviderByUrl( String scmUrl )
        throws ScmRepositoryException, NoSuchScmProviderException
    {
        return getScmProvider();
    }

    /**
     * @return getScmProvider()
     */
    public ScmProvider getProviderByType( String providerType )
        throws NoSuchScmProviderException
    {
        return getScmProvider();
    }

    /**
     * @return getScmProvider()
     */
    public ScmProvider getProviderByRepository( ScmRepository repository )
        throws NoSuchScmProviderException
    {
        return getScmProvider();
    }

}
