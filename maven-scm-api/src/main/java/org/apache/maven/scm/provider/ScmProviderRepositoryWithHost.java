package org.apache.maven.scm.provider;

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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class ScmProviderRepositoryWithHost
    extends ScmProviderRepository
{
    private String host;

    private int port;

    private String privateKey;

    private String passphrase;

    public String getHost()
    {
        return host;
    }

    public void setHost( String host )
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort( int port )
    {
        this.port = port;
    }

    public String getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey( String privateKey )
    {
        this.privateKey = privateKey;
    }

    public String getPassphrase()
    {
        return passphrase;
    }

    public void setPassphrase( String passphrase )
    {
        this.passphrase = passphrase;
    }
}
