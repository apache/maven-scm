package org.apache.maven.scm;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CommandParameter
{
    public final static CommandParameter BINARY = new CommandParameter( "binary" );

    public final static CommandParameter RECURSIVE = new CommandParameter( "recursive" );

    public final static CommandParameter MESSAGE = new CommandParameter( "message" );

    public final static CommandParameter BRANCH_NAME = new CommandParameter( "branchName" );

    public final static CommandParameter START_DATE = new CommandParameter( "startDate" );

    public final static CommandParameter END_DATE = new CommandParameter( "endDate" );

    public final static CommandParameter NUM_DAYS = new CommandParameter( "numDays" );

    public final static CommandParameter BRANCH = new CommandParameter( "branch" );

    public final static CommandParameter TAG = new CommandParameter( "tag" );

    public final static CommandParameter FILE = new CommandParameter( "file" );

    public final static CommandParameter FILES = new CommandParameter( "files" );

    public final static CommandParameter START_REVISION = new CommandParameter( "startRevision" );

    public final static CommandParameter END_REVISION = new CommandParameter( "endRevision" );

    private String name;
    
    public CommandParameter( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
