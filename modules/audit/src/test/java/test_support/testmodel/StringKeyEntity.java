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

package test_support.testmodel;

import io.jmix.core.metamodel.annotations.ModelProperty;

import javax.persistence.*;

@Entity(name = "test$StringKeyEntity")
@Table(name = "TEST_STRING_KEY")
public class StringKeyEntity implements io.jmix.core.Entity {

    private static final long serialVersionUID = 871701970234815437L;

    @Id
    @Column(name = "CODE")
    private String code;

    @Column(name = "NAME")
    protected String name;

    @ModelProperty
    @Transient
    protected String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
