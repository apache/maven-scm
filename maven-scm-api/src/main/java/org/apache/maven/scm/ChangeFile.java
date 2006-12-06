package org.apache.maven.scm;

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
 * A set of information about revisions of a file as returned by CVS's log
 * command
 *
 * @author <a href="mailto:dion@multitask.com.au">dIon Gillard </a>
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ChangeFile
{
    /**
     * the name of the file relative to the project directory.
     */
    private String name;

    /**
     * the latest revision of the file.
     */
    private String revision;

    /**
     * Constructor for the ChangeFile object without all details available
     *
     * @param name file name
     */
    public ChangeFile( String name )
    {
        setName( name );
    }

    /**
     * Constructor for the ChangeFile object
     *
     * @param name file name
     * @param rev  latest revision of the file
     */
    public ChangeFile( String name, String rev )
    {
        setName( name );

        setRevision( rev );
    }

    /**
     * Gets the name attribute of the ChangeLogFile object.
     *
     * @return the file name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the revision attribute of the ChangeLogFile object.
     *
     * @return the latest revision of the file
     */
    public String getRevision()
    {
        return revision;
    }

    /**
     * Setter for property name.
     *
     * @param name New value of property name.
     */
    public void setName( String name )
    {
        this.name = name;
    }

    /**
     * Setter for property revision.
     *
     * @param revision New value of property revision.
     */
    public void setRevision( String revision )
    {
        this.revision = revision;
    }

    /**
     * Provide a version of the object as a string for debugging purposes
     *
     * @return a {@link String}made up of the properties of the object
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer( getName() );

        if ( getRevision() != null )
        {
            buffer.append( ", " ).append( getRevision() );
        }

        return buffer.toString();
    }
}
