/*
 * Copyright (c) 2008-2019 Haulmont.
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
 *
 */

package io.jmix.rest.api.controllers;

import io.jmix.rest.api.service.UserSessionControllerManager;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

@RestController("jmix_UserSessionController")
@RequestMapping(path = "/rest/user-session")
public class UserSessionController {

    @Inject
    protected UserSessionControllerManager localeControllerManager;

    @PutMapping("/locale")
    public void setSessionLocale(HttpServletRequest request) {
        localeControllerManager.setSessionLocale(request);
    }
}
