package org.apache.maven.scm.provider.integrity.command.blame;

/**
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

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to consume the standard output from running the IntegrityBlameCommand
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityBlameConsumer.java 1.2 2011/08/22 13:06:16EDT Cletus D'Souza (dsouza) Exp  $
 */
public class IntegrityBlameConsumer implements StreamConsumer {
    private ScmLogger logger;
    private List<BlameLine> blameList;
    private SimpleDateFormat dateFormat;

    /**
     * IntegrityBlameConsumer constructor requires a ScmLogger to log all the activity
     *
     * @param logger ScmLogger object
     */
    public IntegrityBlameConsumer(ScmLogger logger) {
        this.logger = logger;
        this.blameList = new ArrayList<BlameLine>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy z");
    }

    /**
     * {@inheritDoc}
     */
    public void consumeLine(String line) {
        // Parse the annotate output which should return the three pieces of data
        logger.debug(line);
        if (null != line && line.trim().length() > 0) {
            String[] tokens = StringUtils.split(line, "\t");
            if (tokens.length != 3) {
                logger.warn("Failed to parse line: " + line);
            } else {
                try {
                    blameList.add(new BlameLine(dateFormat.parse(tokens[0]), tokens[1], tokens[2]));
                } catch (ParseException e) {
                    logger.error("Failed to date string: " + tokens[0]);
                }
            }
        }
    }

    /**
     * Returns a list of BlameLine objects found from parsing the 'si annotate' output
     *
     * @return
     */
    public List<BlameLine> getBlameList() {
        return blameList;
    }
}
