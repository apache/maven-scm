package org.apache.maven.scm;

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

    public void setString( CommandParameter parameter, String value )
        throws ScmException
    {
        setObject( parameter, value );
    }

    // ----------------------------------------------------------------------
    // Int
    // ----------------------------------------------------------------------

    public int getInt( CommandParameter parameter )
        throws ScmException
    {
        return ( (Integer) getObject( Integer.class, parameter ) ).intValue();
    }

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

    public void setInt( CommandParameter parameter, int value )
        throws ScmException
    {
        setObject( parameter, new Integer( value ) );
    }

    // ----------------------------------------------------------------------
    // Date
    // ----------------------------------------------------------------------

    public Date getDate( CommandParameter parameter )
        throws ScmException
    {
        return (Date)getObject( Date.class, parameter );
    }

    public Date getDate( CommandParameter parameter, Date defaultValue )
        throws ScmException
    {
        return (Date)getObject( Date.class, parameter, defaultValue );
    }

    public void setDate( CommandParameter parameter, Date date )
        throws ScmException
    {
        setObject( parameter, date );
    }

    // ----------------------------------------------------------------------
    // Boolean
    // ----------------------------------------------------------------------

    public boolean getBoolean( CommandParameter parameter )
        throws ScmException
    {
        return Boolean.valueOf( getString( parameter) ).booleanValue();
    }

    // ----------------------------------------------------------------------
    // File[]
    // ----------------------------------------------------------------------

    public File[] getFileArray( CommandParameter parameter )
        throws ScmException
    {
        return (File[]) getObject( File[].class, parameter );
    }

    public File[] getFileArray( CommandParameter parameter, File[] defaultValue )
        throws ScmException
    {
        return (File[]) getObject( File[].class, parameter, defaultValue );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

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
            throw new ScmException( "Wrong parameter type for '" + parameter.getName() + ". " +
                "Expected: " + clazz.getName() + ", got: " + object.getClass().getName() );
        }

        return object;
    }

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
