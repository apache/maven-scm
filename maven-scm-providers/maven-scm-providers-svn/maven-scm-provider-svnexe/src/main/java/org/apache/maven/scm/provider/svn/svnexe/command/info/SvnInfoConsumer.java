/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.provider.svn.svnexe.command.info;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.util.AbstractConsumer;

/**
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 *
 */
public class SvnInfoConsumer extends AbstractConsumer {
    private final List<InfoItem> infoItems = new ArrayList<>();

    private InfoItem currentItem = new InfoItem();

    /** {@inheritDoc} */
    public void consumeLine(String s) {
        if (s.equals("")) {
            if (currentItem != null) {
                infoItems.add(currentItem);
            }

            currentItem = new InfoItem();
        } else if (s.startsWith("Path: ")) {
            currentItem.setPath(getValue(s));
        } else if (s.startsWith("URL: ")) {
            currentItem.setURL(getValue(s));
        } else if (s.startsWith("Repository Root: ")) {
            currentItem.setRepositoryRoot(getValue(s));
        } else if (s.startsWith("Repository UUID: ")) {
            currentItem.setRepositoryUUID(getValue(s));
        } else if (s.startsWith("Revision: ")) {
            currentItem.setRevision(getValue(s));
        } else if (s.startsWith("Node Kind: ")) {
            currentItem.setNodeKind(getValue(s));
        } else if (s.startsWith("Schedule: ")) {
            currentItem.setSchedule(getValue(s));
        } else if (s.startsWith("Last Changed Author: ")) {
            currentItem.setLastChangedAuthor(getValue(s));
        } else if (s.startsWith("Last Changed Rev: ")) {
            currentItem.setLastChangedRevision(getValue(s));
        } else if (s.startsWith("Last Changed Date: ")) {
            currentItem.setLastChangedDate(getValue(s));
        }
    }

    private static String getValue(String s) {
        int idx = s.indexOf(": ");

        if (idx < 0) {
            // FIXME: Can't throw any exceptions in consumeLine..
            return null;
        } else {
            return s.substring(idx + 2);
        }
    }

    public List<InfoItem> getInfoItems() {
        return infoItems;
    }
}
