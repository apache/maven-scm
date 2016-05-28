package org.apache.maven.scm.provider.git.gitexe.command;

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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.shell.Shell;

/**
 * Dummy implementation of {@link Shell} that invokes the executable directly to
 * avoid the command line length limitation on Windows.
 * <p>
 * The original idea was proposed in PLXUTILS-107. This implementation is
 * adapted from
 * https://github.com/gwt-maven-plugin/gwt-maven-plugin/blob/master/src/main/
 * java/org/codehaus/mojo/gwt/shell/JavaShell.java
 * </p>
 * 
 * @since 1.9.5
 */
public class FakeShell extends Shell
{
    @Override
    protected List<String> getRawCommandLine( String executable, String[] arguments )
    {
        List<String> commandLine = new ArrayList<String>( arguments.length + 1 );

        assert executable != null;
        commandLine.add( executable );

        if ( isQuotedArgumentsEnabled() )
        {
            char argumentQuoteDelimiter = getArgumentQuoteDelimiter();
            char[] escapeChars = getEscapeChars( isSingleQuotedExecutableEscaped(),
                    isDoubleQuotedExecutableEscaped() );
            char[] quotingTriggerChars = getQuotingTriggerChars();
            for ( String argument : arguments )
            {
                commandLine.add( StringUtils.quoteAndEscape( argument, argumentQuoteDelimiter, escapeChars,
                        quotingTriggerChars, '\\', false ) );
            }
        }
        else
        {
            for ( String argument : arguments )
            {
                commandLine.add( argument );
            }
        }

        return commandLine;
    }
}
