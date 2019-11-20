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

package io.jmix.ui.web.view.screens;


import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import com.haulmont.cuba.security.entity.User;

import javax.inject.Inject;

@UiController
@UiDescriptor("user-edit-embedded-view.xml")
@EditedEntityContainer("userDc")
@LoadDataBeforeShow
public class UserEditEmbeddedViewScreen extends StandardEditor<User> {

    @Inject
    public InstanceContainer<User> userDc;

}