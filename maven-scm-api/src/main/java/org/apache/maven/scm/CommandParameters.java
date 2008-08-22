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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CommandParameters
{
    private Map parameters = new HashMap();

    // ----------------------------------------------------------------------
    // String
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as String.
     *
     * @param parameter The parameter
     * @return The parameter value as a String
     * @throws ScmException if the parameter doesn't exist
     */
    public String getString( CommandParameter parameter )
        throws ScmException
    {
        Object object = getObject( String.class, parameter );

        if ( object == null )
        {
            throw new ScmException( "Missing parameter: '" + parameter.getName() + "'." );
        }

        return object.toString();
    }

    /**
     * Return the parameter value or the default value if it doesn't exist.
     *
     * @param parameter    The parameter
     * @param defaultValue The default value
     * @return The parameter value as a String
     * @throws ScmException if the value is in the wrong type
     */
    public String getString( CommandParameter parameter, String defaultValue )
        throws ScmException
    {
        Object object = getObject( String.class, parameter, null );

        if ( object == null )
        {
            return defaultValue;
        }

        return object.toString();
    }

    /**
     * Set a parameter value.
     *
     * @param parameter The parameter name
     * @param value     The value of the parameter
     * @throws ScmException if the parameter already exist
     */
    public void setString( CommandParameter parameter, String value )
        throws ScmException
    {
        setObject( parameter, value );
    }

    // ----------------------------------------------------------------------
    // Int
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as int.
     *
     * @param parameter The parameter
     * @return The parameter value as a String
     * @throws ScmException if the parameter doesn't exist
     */
    public int getInt( CommandParameter parameter )
        throws ScmException
    {
        return ( (Integer) getObject( Integer.class, parameter ) ).intValue();
    }

    /**
     * Return the parameter value as int or the default value if it doesn't exist.
     *
     * @param parameter The parameter
     * @return The parameter value as a int
     * @param defaultValue The defaultValue
     * @throws ScmException if the value is in the wrong type
     */
    public int getInt( CommandParameter parameter, int defaultValue )
        throws ScmException
    {
        Integer value = ( (Integer) getObject( Integer.class, parameter, null ) );

        if ( value == null )
        {
            return defaultValue;
        }

        return value.intValue();
    }

    /**
     * Set a parameter value.
     *
     * @param parameter The parameter name
     * @param value     The value of the parameter
     * @throws ScmException if the parameter already exist
     */
    public void setInt( CommandParameter parameter, int value )
        throws ScmException
    {
        setObject( parameter, new Integer( value ) );
    }

    // ----------------------------------------------------------------------
    // Date
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as Date.
     *
     * @param parameter The parameter
     * @return The parameter value as a Date
     * @throws ScmException if the parameter doesn't exist
     */
    public Date getDate( CommandParameter parameter )
        throws ScmException
    {
        return (Date) getObject( Date.class, parameter );
    }

    /**
     * Return the parameter value as String or the default value if it doesn't exist.
     *
     * @param parameter The parameter
     * @param defaultValue The defaultValue
     * @return The parameter value as a Date
     * @throws ScmException if the value is in the wrong type
     */
    public Date getDate( CommandParameter parameter, Date defaultValue )
        throws ScmException
    {
        return (Date) getObject( Date.class, parameter, defaultValue );
    }

    /**
     * Set a parameter value.
     *
     * @param parameter The parameter name
     * @param date      The value of the parameter
     * @throws ScmException if the parameter already exist
     */
    public void setDate( CommandParameter parameter, Date date )
        throws ScmException
    {
        setObject( parameter, date );
    }

    // ----------------------------------------------------------------------
    // Boolean
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as boolean.
     *
     * @param parameter The parameter
     * @return The parameter value as a String
     * @throws ScmException if the parameter doesn't exist
     */
    public boolean getBoolean( CommandParameter parameter )
        throws ScmException
    {
        return Boolean.valueOf( getString( parameter ) ).booleanValue();
    }

    // ----------------------------------------------------------------------
    // ScmVersion
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as ScmVersion.
     *
     * @param parameter The parameter
     * @return The parameter value as a ScmVersion
     * @throws ScmException if the parameter doesn't exist
     */
    public ScmVersion getScmVersion( CommandParameter parameter )
        throws ScmException
    {
        return (ScmVersion) getObject( ScmVersion.class, parameter );
    }

    /**
     * Return the parameter value as ScmVersion or the default value.
     *
     * @param parameter    The parameter
     * @param defaultValue The default value
     * @return The parameter value as a ScmVersion
     * @throws ScmException if the parameter doesn't exist
     */
    public ScmVersion getScmVersion( CommandParameter parameter, ScmVersion defaultValue )
        throws ScmException
    {
        return (ScmVersion) getObject( ScmVersion.class, parameter, defaultValue );
    }

    /**
     * Set a parameter value.
     *
     * @param parameter  The parameter name
     * @param scmVersion The tbranch/tag/revision
     * @throws ScmException if the parameter already exist
     */
    public void setScmVersion( CommandParameter parameter, ScmVersion scmVersion )
        throws ScmException
    {
        setObject( parameter, scmVersion );
    }

    // ----------------------------------------------------------------------
    // File[]
    // ----------------------------------------------------------------------

    /**
     * @param parameter not null
     * @return an array of files
     * @throws ScmException if any
     */
    public File[] getFileArray( CommandParameter parameter )
        throws ScmException
    {
        return (File[]) getObject( File[].class, parameter );
    }

    /**
     *
     * @param parameter not null
     * @param defaultValue could be null
     * @return an array of files
     * @throws ScmException if any
     */
    public File[] getFileArray( CommandParameter parameter, File[] defaultValue )
        throws ScmException
    {
        return (File[]) getObject( File[].class, parameter, defaultValue );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Return the value object.
     *
     * @param clazz     The type of the parameter value
     * @param parameter The parameter
     * @return The parameter value
     * @throws ScmException if the parameter doesn't exist
     */
    private Object getObject( Class clazz, CommandParameter parameter )
        throws ScmException
    {
        Object object = getObject( clazz, parameter, null );

        if ( object == null )
        {
            throw new ScmException( "Missing parameter: '" + parameter.getName() + "'." );
        }

        return object;
    }

    /**
     * Return the value object or the default value if it doesn't exist.
     *
     * @param clazz     The type of the parameter value
     * @param parameter The parameter
     * @param defaultValue The defaultValue
     * @return The parameter value
     * @throws ScmException if the defaultValue is in the wrong type
     */
    private Object getObject( Class clazz, CommandParameter parameter, Object defaultValue )
        throws ScmException
    {
        Object object = parameters.get( parameter.getName() );

        if ( object == null )
        {
            return defaultValue;
        }

        if ( clazz != null && !clazz.isAssignableFrom( object.getClass() ) )
        {
            throw new ScmException( "Wrong parameter type for '" + parameter.getName() + ". " + "Expected: "
                + clazz.getName() + ", got: " + object.getClass().getName() );
        }

        return object;
    }

    /**
     * Set the parameter value.
     *
     * @param parameter The parameter
     * @param value     The parameter value
     * @throws ScmException if the parameter already exist
     */
    private void setObject( CommandParameter parameter, Object value )
        throws ScmException
    {
        Object object = getObject( null, parameter, null );

        if ( object != null )
        {
            throw new ScmException( "The parameter is already set: " + parameter.getName() );
        }

        parameters.put( parameter.getName(), value );
    }
}
