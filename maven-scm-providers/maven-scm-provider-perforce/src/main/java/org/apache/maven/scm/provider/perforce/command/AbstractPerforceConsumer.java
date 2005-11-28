package org.apache.maven.scm.provider.perforce.command;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author mperham
 * @version $Id$
 */
public abstract class AbstractPerforceConsumer
    implements StreamConsumer
{
    private StringWriter out = new StringWriter();

    protected PrintWriter output = new PrintWriter( out );

    public String getOutput()
    {
        output.flush();
        out.flush();
        return out.toString();
    }

}
