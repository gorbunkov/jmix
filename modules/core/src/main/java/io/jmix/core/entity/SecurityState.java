/*
 * Copyright 2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jmix.core.entity;

import com.google.common.collect.Multimap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Stores information about:
 * <ul>
 *   <li>data that has been filtered by row level security;
 * </ul>
 */
public class SecurityState implements Serializable {

    private static final long serialVersionUID = 6613320540189701505L;

    protected transient Multimap<String, Object> filteredData = null;

    protected String[] inaccessibleAttributes;

    protected String[] filteredAttributes;

    protected byte[] securityToken;

    public Multimap<String, Object> getFilteredData() {
        return filteredData;
    }

    public void setFilteredData(Multimap<String, Object> filteredData) {
        this.filteredData = filteredData;
    }

    public String[] getInaccessibleAttributes() {
        return inaccessibleAttributes;
    }

    public void setInaccessibleAttributes(String[] inaccessibleAttributes) {
        this.inaccessibleAttributes = inaccessibleAttributes;
    }

    public String[] getFilteredAttributes() {
        return filteredAttributes;
    }

    public void setFilteredAttributes(String[] filteredAttributes) {
        this.filteredAttributes = filteredAttributes;
    }

    public byte[] getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(byte[] securityToken) {
        this.securityToken = securityToken;
    }
}
