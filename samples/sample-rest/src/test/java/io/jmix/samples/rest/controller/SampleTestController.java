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

package io.jmix.samples.rest.controller;

import io.jmix.core.entity.BaseUser;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@RestController
@RequestMapping(value = "/myapi/sample")
public class SampleTestController {

    @Inject
    protected CurrentAuthentication currentAuthentication;

    @GetMapping(value = "/protectedMethod")
    public String protectedMethod() {
        BaseUser user = currentAuthentication.getUser();
        return "protectedMethod";
    }

    @GetMapping(value = "/unprotectedMethod")
    public String unprotectedMethod() {
        return "unprotectedMethod";
    }
}
