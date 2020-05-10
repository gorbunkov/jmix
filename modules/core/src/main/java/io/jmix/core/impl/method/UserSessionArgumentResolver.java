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

package io.jmix.core.impl.method;

import io.jmix.core.security.UserSession;
import io.jmix.core.security.UserSessionSource;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Allows resolving the current {@link UserSession} as method argument.
 */
@Component(UserSessionArgumentResolver.NAME)
public class UserSessionArgumentResolver extends TypedArgumentResolver<UserSession> {

    public static final String NAME = "jmix_UserSessionArgumentResolver";

    @Inject
    protected UserSessionSource userSessionSource;

    public UserSessionArgumentResolver() {
        super(UserSession.class);
    }

    @Override
    public UserSession resolveArgument(MethodParameter parameter) {
        if (userSessionSource.checkCurrentUserSession()) {
            return userSessionSource.getUserSession();
        } else {
            return null;
        }
    }
}
