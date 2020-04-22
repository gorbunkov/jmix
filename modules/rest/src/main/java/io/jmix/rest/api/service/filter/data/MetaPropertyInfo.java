/*
 * Copyright (c) 2008-2019 Haulmont.
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

package io.jmix.rest.api.service.filter.data;


import io.jmix.core.*;
import io.jmix.core.metamodel.datatypes.Datatype;
import io.jmix.core.metamodel.datatypes.DatatypeRegistry;
import io.jmix.core.metamodel.datatypes.Datatypes;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;

public class MetaPropertyInfo {
    public String name;
    public MetaProperty.Type attributeType;
    public String type;
    public Range.Cardinality cardinality;
    public boolean mandatory;
    public boolean readOnly;
    boolean isPersistent;
    public String description;
    @Deprecated
    boolean isTransient;

    public MetaPropertyInfo(MetaProperty metaProperty) {
        MessageTools messageTools = AppBeans.get(MessageTools.class);
        DatatypeRegistry datatypeRegistry = AppBeans.get(DatatypeRegistry.class);
        MetadataTools metadataTools = AppBeans.get(MetadataTools.class);
        this.name = metaProperty.getName();
        this.attributeType = metaProperty.getType();
        switch (attributeType) {
            case DATATYPE:
                Datatype<Object> datatype = metaProperty.getRange().asDatatype();
                try {
                    this.type = datatypeRegistry.getId(datatype);
                } catch (Exception e) {
                    this.type = datatype.toString();
                }
                break;
            case ASSOCIATION:
            case COMPOSITION:
                this.type = metaProperty.getRange().asClass().getName();
                break;
            case ENUM:
                this.type = metaProperty.getRange().asEnumeration().getJavaClass().getName();
                break;
        }
        this.cardinality = metaProperty.getRange().getCardinality();
        this.readOnly = metaProperty.isReadOnly();
        this.mandatory = metaProperty.isMandatory();
        this.isPersistent = metadataTools.isPersistent(metaProperty);
        this.isTransient = metadataTools.isNotPersistent(metaProperty);
        this.description = messageTools.getPropertyCaption(metaProperty);
    }

    public boolean getPersistent() {
        return isPersistent;
    }

    @Deprecated
    public boolean getTransient() {
        return isTransient;
    }
}
