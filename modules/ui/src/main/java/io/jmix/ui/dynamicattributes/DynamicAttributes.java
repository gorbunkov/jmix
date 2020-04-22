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

package io.jmix.ui.dynamicattributes;

import io.jmix.core.metamodel.model.MetaClass;

import javax.annotation.Nullable;
import java.util.Collection;

// todo dummy component to observe the surface of dynamic attributes usage

public interface DynamicAttributes {

    /**
     * Get all categories linked with metaClass from cache
     */
    Collection<Category> getCategoriesForMetaClass(MetaClass metaClass);

    /**
     * Get all categories attributes for metaClass from cache
     */
    Collection<CategoryAttribute> getAttributesForMetaClass(MetaClass metaClass);

    /**
     * Get certain category attribute for metaClass by attribute's code
     */
    @Nullable
    CategoryAttribute getAttributeForMetaClass(MetaClass metaClass, String code);
}
