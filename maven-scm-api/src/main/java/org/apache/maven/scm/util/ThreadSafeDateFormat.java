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
package org.apache.maven.scm.util;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Thread-safe version of java.text.DateFormat.
 * You can declare it as a static final variable:
 *
 * @author Olivier Lamy
 * <code>
 * private static final ThreadSafeDateFormat DATE_FORMAT = new ThreadSafeDateFormat( DATE_PATTERN );
 * </code>
 */
public class ThreadSafeDateFormat extends DateFormat {
    private static final long serialVersionUID = 3786090697869963812L;

    private final String dateFormat;

    public ThreadSafeDateFormat(String sDateFormat) {
        dateFormat = sDateFormat;
    }

    private final ThreadLocal<SoftReference<SimpleDateFormat>> formatCache =
            new ThreadLocal<SoftReference<SimpleDateFormat>>() {
                public SoftReference<SimpleDateFormat> get() {
                    SoftReference<SimpleDateFormat> softRef = super.get();
                    if (softRef == null || softRef.get() == null) {
                        softRef = new SoftReference<>(new SimpleDateFormat(dateFormat));
                        super.set(softRef);
                    }
                    return softRef;
                }
            };

    private DateFormat getDateFormat() {
        return formatCache.get().get();
    }

    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition) {
        return getDateFormat().format(date, toAppendTo, fieldPosition);
    }

    public Date parse(String source, ParsePosition pos) {
        return getDateFormat().parse(source, pos);
    }
}
