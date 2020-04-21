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

package io.jmix.core.security.impl;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.CurrentAuthenticationHelper;
import io.jmix.core.security.UserAuthentication;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.TimeZone;

@Component(CurrentAuthentication.NAME)
public class CurrentAuthenticationImpl implements CurrentAuthentication {

    @Nullable
    @Override
    public Authentication getAuthentication() {
        return CurrentAuthenticationHelper.get();
    }

    @Override
    public BaseUser getUser() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof BaseUser) {
                return (BaseUser) principal;
            } else {
                throw new RuntimeException("Authentication principal must be a user");
            }
        }
        //todo MG not null?
        return null;
    }

    @Override
    public Locale getLocale() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            if (authentication instanceof UserAuthentication) {
                return ((UserAuthentication) authentication).getLocale();
            }
        }
        return Locale.getDefault();
    }

    @Override
    public TimeZone getTimeZone() {
        //todo MG
        return TimeZone.getDefault();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
}
