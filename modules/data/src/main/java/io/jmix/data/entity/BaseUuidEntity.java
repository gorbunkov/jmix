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
package io.jmix.data.entity;

import io.jmix.core.UuidProvider;
import io.jmix.core.Entity;
import io.jmix.core.entity.HasUuid;
import io.jmix.core.metamodel.annotations.ModelObject;
import io.jmix.core.entity.annotation.UnavailableInSecurityConstraints;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

/**
 * Base class for entities with UUID identifier.
 * <p>
 * Inherit from it if you need an entity without optimistic locking, create, update and soft deletion info.
 */
@MappedSuperclass
@ModelObject(name = "sys_BaseUuidEntity")
@UnavailableInSecurityConstraints
public abstract class BaseUuidEntity implements Entity, HasUuid {

    private static final long serialVersionUID = -2217624132287086972L;

    @Id
    @Column(name = "ID")
    protected UUID id;

    public BaseUuidEntity() {
        id = UuidProvider.createUuid();
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

    public void setUuid(UUID uuid) {
        this.id = uuid;
    }
}
