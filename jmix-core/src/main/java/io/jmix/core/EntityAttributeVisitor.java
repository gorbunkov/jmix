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

package io.jmix.core;

import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.entity.Entity;

/**
 * Visitor to be submitted to {@link MetadataTools#traverseAttributes(Entity, EntityAttributeVisitor)}.
 *
 */
public interface EntityAttributeVisitor {

    /**
     * Visits an entity attribute.
     *
     * @param entity    entity instance
     * @param property  meta-property pointing to the visited attribute
     */
    void visit(Entity entity, MetaProperty property);

    /**
     * Optionally indicates, whether the property has to be visited
     * @param property  meta-property that is about to be visited
     * @return          false if the property has to be visited
     */
    default boolean skip(MetaProperty property) {
        return false;
    }
}
