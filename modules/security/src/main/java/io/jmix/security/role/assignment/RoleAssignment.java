/*
 * Copyright 2020 Haulmont.
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

package io.jmix.security.role.assignment;

import com.google.common.base.Preconditions;
import io.jmix.core.entity.BaseUser;
import io.jmix.security.model.Role;

/**
 * Class stores a link between {@link BaseUser} and {@link Role}
 */
public class RoleAssignment {

    private final String userKey;
    private final String roleCode;

    public RoleAssignment(String userKey, String roleCode) {
        this.userKey = userKey;
        this.roleCode = roleCode;
    }

    public RoleAssignment(BaseUser user, Role role) {
        this(user, role.getClass());
    }

    public RoleAssignment(BaseUser user, Class<?> roleClass) {
        this.userKey = user.getKey();
        io.jmix.security.role.annotation.Role roleAnnotation = roleClass.getAnnotation(io.jmix.security.role.annotation.Role.class);
        Preconditions.checkNotNull(roleAnnotation, "Role class should have @Role annotation");
        this.roleCode = roleAnnotation.code();
    }

    public String getUserKey() {
        return userKey;
    }

    public String getRoleCode() {
        return roleCode;
    }
}