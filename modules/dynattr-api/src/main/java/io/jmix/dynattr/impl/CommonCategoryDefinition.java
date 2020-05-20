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

package io.jmix.dynattr.impl;

import com.google.common.base.Strings;
import io.jmix.core.Entity;
import io.jmix.core.commons.util.ReflectionHelper;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.CategoryDefinition;
import io.jmix.dynattr.impl.model.Category;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CommonCategoryDefinition implements CategoryDefinition {
    protected final Category category;
    protected final List<AttributeDefinition> attributes;

    public CommonCategoryDefinition(Category category) {
        this.category = category;
        if (category.getCategoryAttrs() != null) {
            this.attributes = Collections.unmodifiableList(category.getCategoryAttrs().stream()
                    .map(CommonAttributeDefinition::new)
                    .collect(Collectors.toList()));
        } else {
            this.attributes = Collections.emptyList();
        }
    }

    @Override
    public String getId() {
        return category.getId().toString();
    }

    @Override
    public String getName() {
        return category.getName();
    }

    @Override
    public boolean isDefault() {
        return Boolean.TRUE.equals(category.getIsDefault());
    }

    @Override
    public String getEntityType() {
        return category.getEntityType();
    }

    @Override
    public Collection<AttributeDefinition> getAttributeDefinitions() {
        return attributes;
    }

    @Override
    public Entity getSource() {
        return category;
    }
}
