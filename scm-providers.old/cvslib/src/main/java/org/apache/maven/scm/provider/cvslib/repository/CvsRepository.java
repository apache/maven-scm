package org.apache.maven.scm.provider.cvslib.repository;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.AbstractRepository;
import org.codehaus.plexus.util.cli.EnhancedStringTokenizer;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsRepository extends AbstractRepository
{
    public static final int POS_SCM_SUBTYPE = 0;
    public static final int POS_SCM_USERHOST = 1;
    public static final int POS_SCM_PATH = 2;
    public static final int POS_SCM_MODULE = 3;

    private final static String SUBTYPE_LOCAL = "local";
    private final static String SUBTYPE_PSERVER = "pserver";
    private final static String SUBTYPE_LSERVER = "lserver";
    private final static String SUBTYPE_EXT = "ext";

    private String cvsroot;
    private String subtype;
    private String user;
    private String host;
    private String path;
    private String module;
    private String password;

    /**
     * @return The cvsroot.
     * @throws ScmException
     */
    public String getCvsRoot() throws ScmException
    {
        parseConnection();
        return cvsroot;
    }

    /**
     * @return The subtype (like pserver).
     * @throws ScmException
     */
    public String getSubType() throws ScmException
    {
        parseConnection();
        return subtype;
    }

    /**
     * @return The user.
     * @throws ScmException
     */
    public String getUser() throws ScmException
    {
        parseConnection();
        return user;
    }

    /**
     * @return The host.
     * @throws ScmException
     */
    public String getHost() throws ScmException
    {
        parseConnection();
        return host;
    }

    /**
     * @return The path.
     * @throws ScmException
     */
    public String getPath() throws ScmException
    {
        parseConnection();
        return path;
    }

    /**
     * @return The module name.
     * @throws ScmException
     */
    public String getModule() throws ScmException
    {
        parseConnection();
        return module;
    }

    public void parseConnection() throws ScmException
    {
        String[] tokens = splitConnection();

        if ( tokens.length < 4 )
        {
            throw new ScmException("The connection string contains to few tokens.");
        }

        if ( tokens[POS_SCM_SUBTYPE].equalsIgnoreCase( SUBTYPE_LOCAL ) )
        {
            // use the local repository directory eg. '/home/cvspublic'
            cvsroot = tokens[POS_SCM_PATH];
        }
        else if ( tokens[POS_SCM_SUBTYPE].equalsIgnoreCase( SUBTYPE_LSERVER ) )
        {
            //create the cvsroot as the local socket cvsroot
            cvsroot = tokens[POS_SCM_USERHOST] + ":" + tokens[POS_SCM_PATH];
        }
        else
        {
            //create the cvsroot as the remote cvsroot
            cvsroot =
                ":"
                + tokens[POS_SCM_SUBTYPE]
                + ":"
                + tokens[POS_SCM_USERHOST]
                + ":"
                + tokens[POS_SCM_PATH];
        }

        subtype = tokens[POS_SCM_SUBTYPE];

        if ( !subtype.equalsIgnoreCase( SUBTYPE_LOCAL ) )
        {
            String userhost = tokens[POS_SCM_USERHOST];
    
            int index = userhost.indexOf("@");

            if ( index == -1 )
            {
                throw new ScmException( "The userhost part must be on the form: <username>@<hostname>." );
            }
    
            user = userhost.substring( 0, index );
    
            host = userhost.substring( index + 1 );
        }

        path = tokens[POS_SCM_PATH];

        module = tokens[POS_SCM_MODULE];
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

	/**
	 * Splits an connection string into parts 
	 * @param connection
	 * @return
	 */
    public String[] splitConnection() throws ScmException
    {
    	if (getConnection() == null)
    	{
			throw new ScmException("The connection must be defined");
    	}
    	if (getDelimiter() == null)
    	{
			throw new ScmException("The delimiter must be defined");
		}

		EnhancedStringTokenizer tok = new EnhancedStringTokenizer(getConnection(), getDelimiter());

		String[] tokens = tokenizerToArray(tok);

		return tokens;
	}

	/**
	 * Converts a tokenizer to an array of strings
	 * @param tok
	 * @return String[]
	 */
	private static String[] tokenizerToArray(EnhancedStringTokenizer tok)
	{
		List l = new ArrayList();
		while (tok.hasMoreTokens())
		{
			l.add(tok.nextToken());
		}
		return (String[]) l.toArray(new String[l.size()]);
	}
}
