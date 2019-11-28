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

package com.sample.app.entity;

import io.jmix.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity(name = "test_TestAppEntity")
@Table(name = "TEST_APP_ENTITY")
public class TestAppEntity extends StandardEntity {

    private static final long serialVersionUID = 8256929425690816623L;

    @Column(name = "NAME")
    private String name;

    @OneToMany(mappedBy = "appEntity")
    private List<TestAppEntityItem> items;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<TestAppEntityItem> getItems() {
        return items;
    }

    public void setItems(List<TestAppEntityItem> items) {
        this.items = items;
    }
}
