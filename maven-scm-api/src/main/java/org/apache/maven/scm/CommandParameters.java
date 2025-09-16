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
package org.apache.maven.scm;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Olivier Lamy
 */
public class CommandParameters implements Serializable {
    private static final long serialVersionUID = -7346070735958137283L;

    private Map<String, Object> parameters = new HashMap<>();

    // ----------------------------------------------------------------------
    // String
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as String.
     *
     * @param parameter the parameter
     * @return the parameter value as a String
     * @throws ScmException if the parameter doesn't exist
     */
    public String getString(CommandParameter parameter) throws ScmException {
        Object object = getObject(String.class, parameter);

        return object.toString();
    }

    /**
     * Return the parameter value or the default value if it doesn't exist.
     *
     * @param parameter    the parameter
     * @param defaultValue the default value
     * @return the parameter value as a String
     * @throws ScmException if the value is in the wrong type
     */
    public String getString(CommandParameter parameter, String defaultValue) throws ScmException {
        Object object = getObject(String.class, parameter, null);

        if (object == null) {
            return defaultValue;
        }

        return object.toString();
    }

    /**
     * Set a parameter value.
     *
     * @param parameter the parameter name
     * @param value     the value of the parameter
     * @throws ScmException if the parameter already exist
     */
    public void setString(CommandParameter parameter, String value) throws ScmException {
        setObject(parameter, value);
    }

    // ----------------------------------------------------------------------
    // Int
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as int.
     *
     * @param parameter the parameter
     * @return the parameter value as a String
     * @throws ScmException if the parameter doesn't exist
     */
    public int getInt(CommandParameter parameter) throws ScmException {
        return ((Integer) getObject(Integer.class, parameter)).intValue();
    }

    /**
     * Return the parameter value as int or the default value if it doesn't exist.
     *
     * @param parameter    the parameter
     * @param defaultValue the default value
     * @return the parameter value as an int
     * @throws ScmException if the value is in the wrong type
     */
    public int getInt(CommandParameter parameter, int defaultValue) throws ScmException {
        Integer value = ((Integer) getObject(Integer.class, parameter, null));

        if (value == null) {
            return defaultValue;
        }

        return value.intValue();
    }

    /**
     * Set a parameter value.
     *
     * @param parameter the parameter name
     * @param value     the value of the parameter
     * @throws ScmException if the parameter already exist
     */
    public void setInt(CommandParameter parameter, int value) throws ScmException {
        setObject(parameter, Integer.valueOf(value));
    }

    // ----------------------------------------------------------------------
    // Date
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as Date.
     *
     * @param parameter the parameter
     * @return the parameter value as a Date
     * @throws ScmException if the parameter doesn't exist
     */
    public Date getDate(CommandParameter parameter) throws ScmException {
        return (Date) getObject(Date.class, parameter);
    }

    /**
     * Return the parameter value as String or the default value if it doesn't exist.
     *
     * @param parameter    the parameter
     * @param defaultValue the defaultValue
     * @return the parameter value as a Date
     * @throws ScmException if the value is in the wrong type
     */
    public Date getDate(CommandParameter parameter, Date defaultValue) throws ScmException {
        return (Date) getObject(Date.class, parameter, defaultValue);
    }

    /**
     * Set a parameter value.
     *
     * @param parameter the parameter name
     * @param date      the value of the parameter
     * @throws ScmException if the parameter already exist
     */
    public void setDate(CommandParameter parameter, Date date) throws ScmException {
        setObject(parameter, date);
    }

    // ----------------------------------------------------------------------
    // Boolean
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as boolean.
     *
     * @param parameter the parameter
     * @return the parameter value as a boolean
     * @throws ScmException if the parameter doesn't exist
     */
    public boolean getBoolean(CommandParameter parameter) throws ScmException {
        return Boolean.valueOf(getString(parameter)).booleanValue();
    }

    /**
     * Return the parameter value as boolean.
     *
     * @param parameter    the parameter
     * @param defaultValue default value if parameter not exists
     * @return the parameter value as a boolean
     * @throws ScmException if the parameter doesn't exist
     * @since 1.7
     */
    public boolean getBoolean(CommandParameter parameter, boolean defaultValue) throws ScmException {
        return Boolean.parseBoolean(getString(parameter, Boolean.toString(defaultValue)));
    }

    // ----------------------------------------------------------------------
    // ScmVersion
    // ----------------------------------------------------------------------

    /**
     * Return the parameter value as ScmVersion.
     *
     * @param parameter the parameter
     * @return the parameter value as a ScmVersion
     * @throws ScmException if the parameter doesn't exist
     */
    public ScmVersion getScmVersion(CommandParameter parameter) throws ScmException {
        return (ScmVersion) getObject(ScmVersion.class, parameter);
    }

    /**
     * Return the parameter value as ScmVersion or the default value.
     *
     * @param parameter    the parameter
     * @param defaultValue the default value
     * @return the parameter value as a ScmVersion
     * @throws ScmException if the parameter doesn't exist
     */
    public ScmVersion getScmVersion(CommandParameter parameter, ScmVersion defaultValue) throws ScmException {
        return (ScmVersion) getObject(ScmVersion.class, parameter, defaultValue);
    }

    /**
     * Set a parameter value.
     *
     * @param parameter  the parameter name
     * @param scmVersion the tbranch/tag/revision
     * @throws ScmException if the parameter already exist
     */
    public void setScmVersion(CommandParameter parameter, ScmVersion scmVersion) throws ScmException {
        setObject(parameter, scmVersion);
    }

    // ----------------------------------------------------------------------
    // File[]
    // ----------------------------------------------------------------------

    /**
     * @param parameter not null
     * @return an array of files
     * @throws ScmException if any
     */
    public File[] getFileArray(CommandParameter parameter) throws ScmException {
        return (File[]) getObject(File[].class, parameter);
    }

    /**
     * @param parameter    not null
     * @param defaultValue could be null
     * @return an array of files
     * @throws ScmException if any
     */
    public File[] getFileArray(CommandParameter parameter, File[] defaultValue) throws ScmException {
        return (File[]) getObject(File[].class, parameter, defaultValue);
    }

    public ScmTagParameters getScmTagParameters(CommandParameter parameter) throws ScmException {
        return (ScmTagParameters) getObject(ScmTagParameters.class, parameter, new ScmTagParameters());
    }

    public void setScmTagParameters(CommandParameter parameter, ScmTagParameters scmTagParameters) throws ScmException {
        setObject(parameter, scmTagParameters);
    }

    public void setScmBranchParameters(CommandParameter parameter, ScmBranchParameters scmBranchParameters)
            throws ScmException {
        setObject(parameter, scmBranchParameters);
    }

    public ScmBranchParameters getScmBranchParameters(CommandParameter parameter) throws ScmException {
        return (ScmBranchParameters) getObject(ScmBranchParameters.class, parameter, new ScmBranchParameters());
    }

    // ----------------------------------------------------------------------
    // SigningOption (Git specific)
    // ----------------------------------------------------------------------
    /**
     * The sign option for a commit or tag.
     * <p>
     * This is only relevant for SCM providers that support signing commits/tags, such as Git.
     * </p>
     *
     * @see <a href="https://git-scm.com/book/en/v2/Git-Tools-Signing-Your-Work">Git Tools - Signing Your Work</a>
     */
    public enum SignOption {
        /**
         * Signs the commit/tag irrespective of the Git configuration setting {@code commit.gpgSign} or {@code tag.gpgSign}.
         * Only has an effect for supported SCM providers. Others may be silently ignoring this setting.
         */
        FORCE_SIGN,
        /**
         * Just uses the default value in the Git configuration for setting {@code commit.gpgSign} or {@code tag.gpgSign}.
         * Only has an effect for supported SCM providers. Others may be silently ignoring this setting.
         */
        DEFAULT,
        /**
         * Does not sign the commit/tag irrespective of the Git configuration setting {@code commit.gpgSign} or {@code tag.gpgSign}.
         */
        FORCE_NO_SIGN
    }

    public void setSignOption(CommandParameter parameter, SignOption signOption) throws ScmException {
        setObject(parameter, signOption);
    }

    /**
     * Return the sign option.
     *
     * @param parameter the parameter
     * @return the sign option or null if not set
     * @throws ScmException if the parameter has the wrong type
     */
    public SignOption getSignOption(CommandParameter parameter) throws ScmException {
        return getObject(SignOption.class, parameter, null);
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Return the value object.
     *
     * @param clazz     the type of the parameter value
     * @param parameter the parameter
     * @return the parameter value
     * @throws ScmException if the parameter doesn't exist
     */
    private <T> T getObject(Class<T> clazz, CommandParameter parameter) throws ScmException {
        T object = getObject(clazz, parameter, null);

        if (object == null) {
            throw new ScmException("Missing parameter: '" + parameter.getName() + "'.");
        }

        return object;
    }

    /**
     * Return the value object or the default value if it doesn't exist.
     *
     * @param clazz        the type of the parameter value
     * @param parameter    the parameter
     * @param defaultValue the defaultValue
     * @return the parameter value
     * @throws ScmException if the defaultValue is in the wrong type
     */
    @SuppressWarnings("unchecked")
    private <T> T getObject(Class<T> clazz, CommandParameter parameter, T defaultValue) throws ScmException {
        Object object = parameters.get(parameter.getName());

        if (object == null) {
            return defaultValue;
        }

        if (clazz != null && !clazz.isAssignableFrom(object.getClass())) {
            throw new ScmException("Wrong parameter type for '" + parameter.getName() + ". " + "Expected: "
                    + clazz.getName() + ", got: " + object.getClass().getName());
        }

        return (T) object;
    }

    /**
     * Set the parameter value.
     *
     * @param parameter the parameter
     * @param value     the parameter value
     * @throws ScmException if the parameter already exist
     */
    private void setObject(CommandParameter parameter, Object value) throws ScmException {
        Object object = getObject(null, parameter, null);

        if (object != null) {
            throw new ScmException("The parameter is already set: " + parameter.getName());
        }

        parameters.put(parameter.getName(), value);
    }

    /**
     * Removes a parameter, silent if it didn't exist.
     *
     * @param parameter to remove
     */
    public void remove(CommandParameter parameter) {
        parameters.remove(parameter.getName());
    }
}
