package org.apache.maven.scm.provider;

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
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class ScmProviderRepository
{
    /**
     * Get the concept of a model from this repository.
     * By default, this is just the last path element of the URL, but it may be overridden by the provider.
     * @see #deriveModuleFromUrl(String)
     *
     * @return the module name, a relative path
     */
    public abstract String getModule();

    /**
     * Derive the default module from an SCM URL. This will be the last path element in the URL.
     *
     * @param url the url
     * @return the module
     */
    protected static String deriveModuleFromUrl( String url )
    {
        if ( url == null || url.length() == 0  || "/".equals( url ) )
        {
            return "";
        }

        int index = url.lastIndexOf( '/' );
        return ( index >= 0 ? url.substring( index + 1 ) : "" );
    }
}
