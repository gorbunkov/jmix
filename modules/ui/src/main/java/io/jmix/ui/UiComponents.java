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

package io.jmix.ui;

import com.google.common.reflect.TypeToken;
import io.jmix.core.BeanLocator;
import io.jmix.ui.component.Component;

/**
 * Factory to create UI components in client independent manner.
 * <br>
 * An instance of the factory can be injected into screen controllers or obtained through {@link BeanLocator}.
 */
public interface UiComponents {

    String NAME = "jmix_UiComponents";

    /**
     * Create a component instance by its name.
     *
     * @param name component name. It is usually defined in NAME constant inside the component interface,
     *             e.g. {@link io.jmix.ui.component.Label#NAME}.
     *             It is also usually equal to component's XML name.
     * @return component instance for the current client type (web or desktop)
     */
    <T extends Component> T create(String name);

    /**
     * Create a component instance by its type.
     *
     * @param type component type
     * @return component instance for the current client type (web or desktop)
     */
    <T extends Component> T create(Class<T> type);

    /**
     * Create a component instance by its type.
     *
     * @param type component type token
     * @return component instance for the current client type (web or desktop)
     * @see io.jmix.ui.component.Label#TYPE_DEFAULT
     * @see io.jmix.ui.component.TextField#TYPE_DEFAULT
     */
    <T extends Component> T create(TypeToken<T> type);
}