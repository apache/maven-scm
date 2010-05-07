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

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Description;
import org.jmock.api.Action;
import org.jmock.api.Invocation;

public class AddElementsAction<T>
    implements Action
{
    private Collection<T> elements;

    private int parameter;

    public AddElementsAction( Collection<T> elements, int parameter )
    {
        this.elements = elements;
        this.parameter = parameter;
    }

    public void describeTo( Description description )
    {
        description.appendText( "adds " ).appendValueList( "", ", ", "", elements ).appendText( " to a collection" );
    }

    @SuppressWarnings("unchecked")
    public Object invoke( Invocation invocation )
        throws Throwable
    {
        ( (Collection<T>) invocation.getParameter( parameter ) ).addAll( elements );
        return null;
    }

    public static <T> Action addElementsTo( int parameter, T... newElements )
    {
        return new AddElementsAction<T>( Arrays.asList( newElements ), parameter );
    }
}
