/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.core.model.common;

import io.jmix.core.entity.annotation.EnableRestore;
import io.jmix.core.metamodel.annotation.ModelProperty;

import javax.persistence.*;

@Entity(name = "test$AppFolder")
@Table(name = "TEST_APP_FOLDER")
@PrimaryKeyJoinColumn(name = "FOLDER_ID", referencedColumnName = "ID")
@DiscriminatorValue("A")
@EnableRestore
public class AppFolder extends AbstractSearchFolder {

    private static final long serialVersionUID = -3587493035203986325L;

    @Column(name = "VISIBILITY_SCRIPT", length = 200)
    protected String visibilityScript;

    @Column(name = "QUANTITY_SCRIPT", length = 200)
    protected String quantityScript;

    @ModelProperty
    @Transient
    protected Integer quantity;

    @Override
    public void copyFrom(AbstractSearchFolder srcFolder) {
        super.copyFrom(srcFolder);

        setVisibilityScript(((AppFolder) srcFolder).getVisibilityScript());
        setQuantityScript(((AppFolder) srcFolder).getQuantityScript());
    }

    public String getVisibilityScript() {
        return visibilityScript;
    }

    public void setVisibilityScript(String visibilityScript) {
        this.visibilityScript = visibilityScript;
    }

    public String getQuantityScript() {
        return quantityScript;
    }

    public void setQuantityScript(String quantityScript) {
        this.quantityScript = quantityScript;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String getCaption() {
        String s = getLocName();
        if (quantity == null) {
            return s;
        } else {
            return s + " (" + quantity + ")";
        }
    }

    @Override
    public String toString() {
        return getName() + " (" + quantity + ")";
    }
}