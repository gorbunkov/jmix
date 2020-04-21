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

package io.jmix.core.security;

import io.jmix.core.entity.BaseUser;

/**
 * We cannot user {@link UserRepository} in {@link UserSessionSource} because of circular dependency. When
 * UserSessionSource is moved to cuba module, the ServiceUserRepository must be removed
 */
//todo MG remove when it is not required by UserSessionFactory
public interface ServiceUserRepository {

    String NAME = "jmix_ServiceUserRepository";

    BaseUser getSystemUser();

    BaseUser getAnonymousUser();

}
