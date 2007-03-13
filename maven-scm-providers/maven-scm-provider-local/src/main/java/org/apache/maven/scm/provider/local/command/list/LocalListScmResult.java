package org.apache.maven.scm.provider.local.command.list;

import org.apache.maven.scm.command.list.ListScmResult;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class LocalListScmResult
    extends ListScmResult
{
    public LocalListScmResult( String commandLine, List files )
    {
        super( commandLine, files);
    }
}
