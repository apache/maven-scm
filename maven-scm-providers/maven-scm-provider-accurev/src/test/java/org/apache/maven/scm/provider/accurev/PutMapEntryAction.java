package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Map;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

public class PutMapEntryAction<K, V>
    implements Action
{

    private K key;

    private V value;

    private int parameter;

    public PutMapEntryAction( K key, V value, int parameter )
    {
        this.key = key;
        this.value = value;
        this.parameter = parameter;
    }

    public void describeTo( Description description )
    {
        description.appendText( "puts [" );
        description.appendValue( key );
        description.appendText( "," );
        description.appendValue( value );
        description.appendText( "] into a map" );
    }

    @SuppressWarnings("unchecked")
    public Object invoke( Invocation invocation )
        throws Throwable
    {
        ( (Map<K, V>) invocation.getParameter( parameter ) ).put( key, value );
        return null;
    }

    public static <K, V> Action putEntryTo( int parameter, K key, V value )
    {
        return new PutMapEntryAction<K, V>( key, value, parameter );
    }
}
