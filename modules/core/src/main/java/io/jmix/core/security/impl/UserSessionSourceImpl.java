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

package io.jmix.core.security.impl;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Locale;
import java.util.UUID;

@Component(UserSessionSource.NAME)
public class UserSessionSourceImpl implements UserSessionSource {

//    @Inject
//    protected UserSessions userSessions;

    @Inject
    protected ServiceUserRepository serviceUserRepository;

    @Override
    public boolean checkCurrentUserSession() {
//        UserSession userSession = CurrentUserSession.get();
//        if (userSession == null) {
//            return false;
//        }
//        if (userSession.getAuthentication() instanceof SystemAuthenticationToken) {
//            return true;
//        }
//        UserSession session = userSessions.getAndRefresh(userSession.getId());
//        return session != null;
        //todo MG
        return true;
    }

    @Override
    public UserSession getUserSession() throws NoUserSessionException {
        Authentication authentication = CurrentAuthenticationHelper.get();

        UserSession session = new UserSession();
        if (authentication instanceof UserAuthentication) {
            session.setUser(((UserAuthentication) authentication).getUser());
            session.setLocale(((UserAuthentication) authentication).getLocale());
        } else if (authentication instanceof AnonymousAuthenticationToken ||
                authentication instanceof SystemAuthenticationToken) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof BaseUser) {
                session.setUser((BaseUser) authentication.getPrincipal());
                session.setLocale(Locale.getDefault());
            } else {
                session.setUser(serviceUserRepository.getSystemUser());
                session.setLocale(Locale.getDefault());
            }
        } else if (authentication == null) {
            //todo MG should null authentication be possible?
            //todo MG what user to return?
            session.setUser(serviceUserRepository.getSystemUser());
            session.setLocale(Locale.getDefault());
        } else {
            throw new RuntimeException("Authentication type is not supported: " + authentication.getClass().getCanonicalName());
        }
        return session;
    }

    @Override
    public UUID currentOrSubstitutedUserId() {
        // todo user substitution
        return UUID.fromString(getUserSession().getUser().getKey());
    }

    @Override
    public Locale getLocale() {
        return getUserSession().getLocale();
    }
}
