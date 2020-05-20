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

package io.jmix.ui.components;

import com.google.common.base.Strings;
import io.jmix.core.UuidProvider;
import io.jmix.core.Entity;
import io.jmix.core.entity.HasUuid;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotations.ModelProperty;
import io.jmix.core.metamodel.annotations.ModelObject;
import org.dom4j.Element;

import javax.persistence.Id;
import java.util.UUID;

@ModelObject(name = "sec$ScreenComponentDescriptor")
@SystemLevel
public class ScreenComponentDescriptor implements Entity, HasUuid {

    @Id
    protected UUID id;
    protected Element element;

    @ModelProperty
    protected ScreenComponentDescriptor parent;

    public ScreenComponentDescriptor(Element element, ScreenComponentDescriptor parent) {
        this.id = UuidProvider.createUuid();
        this.element = element;
        this.parent = parent;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getUuid() {
        return id;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.id = uuid;
    }

    @ModelProperty
    public String getCaption() {
        return toString();
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public ScreenComponentDescriptor getParent() {
        return parent;
    }

    public void setParent(ScreenComponentDescriptor parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        String id = element.attributeValue("id");
        id = Strings.isNullOrEmpty(id) ? element.attributeValue("property") : id;

        if (!Strings.isNullOrEmpty(id)) {
            sb.append(id).append(": ");
        }
        sb.append("<").append(element.getName()).append(">");

        return sb.toString();
    }
}
