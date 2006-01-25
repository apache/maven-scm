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
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class ScmProviderRepository
{
    private String user;

    private String password;

    private boolean persistCheckout = false;

    /**
     * @return The user.
     */
    public String getUser()
    {
        return user;
    }

    /**
     * Set the user.
     *
     * @param user The user
     */
    public void setUser( String user )
    {
        this.user = user;
    }

    /**
     * @return The password.
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Set the password.
     *
     * @param password The user password
     */
    public void setPassword( String password )
    {
        this.password = password;
    }

    /**
     * Will checkouts using this repository be persisted so they can
     * be refreshed in the future?  This property is of concern to SCMs
     * like Perforce and Clearcase where the server must track where a
     * user checks out to.  If false, the server entry (clientspec in Perforce
     * terminology) will be deleted after the checkout is complete so the
     * files will not be able to be updated.
     * <p/>
     * This setting can be overriden by using the system property
     * "maven.scm.persistcheckout" to true.
     * <p/>
     * The default is false.  See SCM-113 for more detail.
     */
    public boolean isPersistCheckout()
    {
        String persist = System.getProperty( "maven.scm.persistcheckout" );
        if ( persist != null )
        {
            return Boolean.valueOf( persist ).booleanValue();
        }
        return persistCheckout;
    }

    public void setPersistCheckout( boolean persistCheckout )
    {
        this.persistCheckout = persistCheckout;
    }
}
