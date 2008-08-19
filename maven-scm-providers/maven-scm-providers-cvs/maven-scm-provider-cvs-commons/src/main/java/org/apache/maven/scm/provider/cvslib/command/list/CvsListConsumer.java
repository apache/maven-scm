package org.apache.maven.scm.provider.cvslib.command.list;

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Parses CVS/Entries format, for example, like
 * 
 * <pre>
 * /checkoutlist/1.9/Wed Jan 26 19:08:06 2005/-kkv/
 * /commitinfo/1.10/Tue Jan 11 01:25:34 2005/-kkv/
 * /config/1.15/Sun Jan 23 02:15:57 2005/-kkv/
 * D/directory1////
 * D/directory2////
 * </pre>
 * 
 * @author <a href="mailto:szakusov@emdev.ru">Sergey Zakusov</a>: implemented to fix "Unknown file status" problem
 */
public class CvsListConsumer implements StreamConsumer {

    private ScmLogger m_logger;
    private List      m_entries;

    /**
     * @param logger is a logger
     */
    public CvsListConsumer(ScmLogger logger) {

        m_logger = logger;
        m_entries = new LinkedList();
    }

    /**
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine(String i_line) {

        m_logger.debug(i_line);

        String[] params = i_line.split("/");
        if (params.length < 2) {
            if (StringUtils.isNotEmpty(i_line)) {
                m_logger.warn("Unable to parse it as CVS/Entries format: " + i_line + ".");
            }
        } else {
            m_entries.add(new ScmFile(params[1], ScmFileStatus.UNKNOWN));
        }
    }

    /**
     * @return Parse result
     */
    public List getEntries() {

        return m_entries;
    }
}
