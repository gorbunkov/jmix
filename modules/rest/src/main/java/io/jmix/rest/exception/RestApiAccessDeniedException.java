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

package io.jmix.rest.exception;

import io.jmix.core.Logging;
import io.jmix.core.security.LoginException;

/**
 * Exception that is thrown when REST API user that does not have permission to use REST API.
 */
@Logging(Logging.Type.BRIEF)
public class RestApiAccessDeniedException extends LoginException {

    public RestApiAccessDeniedException(String message) {
        super(message);
    }

    public RestApiAccessDeniedException(String template, Object... params) {
        super(template, params);
    }
}
