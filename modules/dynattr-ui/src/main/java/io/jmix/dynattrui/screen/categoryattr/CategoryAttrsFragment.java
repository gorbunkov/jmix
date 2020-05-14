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

package io.jmix.dynattrui.screen.categoryattr;

import io.jmix.core.*;
import io.jmix.core.metamodel.datatypes.Datatypes;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import io.jmix.ui.UiComponents;
import io.jmix.ui.actions.Action;
import io.jmix.ui.components.GroupTable;
import io.jmix.ui.components.Label;
import io.jmix.ui.model.CollectionPropertyContainer;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.BooleanUtils;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;

@UiController("sys$CategoryAttribute.fragment")
@UiDescriptor("category-attrs-fragment.xml")
public class CategoryAttrsFragment extends ScreenFragment {

    @Inject
    protected Messages messages;
    @Inject
    protected CurrentAuthentication currentAuthentication;
    @Inject
    protected Metadata metadata;
    @Inject
    protected MetadataTools metadataTools;
    @Inject
    protected DataManager dataManager;
    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Inject
    protected FetchPlanRepository fetchPlanRepository;
    @Inject
    protected UiComponents uiComponents;
    @Inject
    protected MessageTools messageTools;

    @Inject
    protected CollectionPropertyContainer<CategoryAttribute> categoryAttributesDc;
    @Inject
    protected GroupTable<CategoryAttribute> categoryAttrsTable;

    @Install(to = "categoryAttrsTable.defaultValue", subject = "columnGenerator")
    protected Label<String> categoryAttrsTableDefaultValueColumnGenerator(CategoryAttribute attribute) {
        String defaultValue = "";

        AttributeType dataType = attribute.getDataType();
        switch (dataType) {
            case BOOLEAN:
                Boolean b = attribute.getDefaultBoolean();
                if (b != null)
                    defaultValue = BooleanUtils.isTrue(b)
                            ? messages.getMessage("trueString")
                            : messages.getMessage("falseString");
                break;
            case DATE:
                Date dateTime = attribute.getDefaultDate();
                if (dateTime != null) {
                    String dateTimeFormat = Datatypes.getFormatStringsNN(currentAuthentication.getLocale()).getDateTimeFormat();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
                    defaultValue = simpleDateFormat.format(dateTime);
                } else if (BooleanUtils.isTrue(attribute.getDefaultDateIsCurrent())) {
                    defaultValue = messages.getMessage(CategoryAttrsFragment.class, "categoryAttrsTable.currentDate");
                }
                break;
            case DATE_WITHOUT_TIME:
                LocalDate dateWoTime = attribute.getDefaultDateWithoutTime();
                if (dateWoTime != null) {
                    String dateWoTimeFormat = Datatypes.getFormatStringsNN(currentAuthentication.getLocale()).getDateFormat();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateWoTimeFormat);
                    defaultValue = dateWoTime.format(formatter);
                } else if (BooleanUtils.isTrue(attribute.getDefaultDateIsCurrent())) {
                    defaultValue = messages.getMessage(CategoryAttrsFragment.class, "categoryAttrsTable.currentDate");
                }
                break;
            case DECIMAL:
                BigDecimal defaultDecimal = attribute.getDefaultDecimal();
                if (defaultDecimal != null) {
                    defaultValue = defaultDecimal.toString();
                }
                break;
            case DOUBLE:
                Double defaultDouble = attribute.getDefaultDouble();
                if (defaultDouble != null) {
                    defaultValue = defaultDouble.toString();
                }
                break;
            case ENTITY:
                Class<?> entityClass = attribute.getJavaType();
                if (entityClass != null) {
                    defaultValue = "";
                    if (attribute.getObjectDefaultEntityId() != null) {
                        MetaClass metaClass = metadata.getClass(entityClass);
                        LoadContext<Entity> lc = new LoadContext(attribute.getJavaType());
                        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.MINIMAL);
                        lc.setFetchPlan(fetchPlan);
                        String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass);
                        lc.setQueryString(String.format("select e from %s e where e.%s = :entityId", metaClass.getName(), pkName))
                                .setParameter("entityId", attribute.getObjectDefaultEntityId());
                        Entity<?> entity = dataManager.load(lc);
                        if (entity != null) {
                            defaultValue = metadataTools.getInstanceName(entity);
                        }
                    }
                } else {
                    defaultValue = messages.getMessage(CategoryAttrsFragment.class, "categoryAttrsTable.entityNotFound");
                }
                break;
            case ENUMERATION:
                defaultValue = attribute.getEnumeration();
                break;
            case INTEGER:
                Integer defaultInt = attribute.getDefaultInt();
                if (defaultInt != null) {
                    defaultValue = defaultInt.toString();
                }
                break;
            case STRING:
                defaultValue = attribute.getDefaultString();
                break;
        }

        Label<String> defaultValueLabel = uiComponents.create(Label.TYPE_STRING);
        defaultValueLabel.setValue(defaultValue);
        return defaultValueLabel;
    }

    @Install(to = "categoryAttrsTable.dataType", subject = "valueProvider")
    protected String categoryAttrsTableDataTypeValueProvider(CategoryAttribute categoryAttribute) {
        String dataType;
        if (BooleanUtils.isTrue(categoryAttribute.getIsEntity())) {
            Class javaType = categoryAttribute.getJavaType();
            if (javaType != null) {
                MetaClass metaClass = metadata.getClass(javaType);
                dataType = messageTools.getEntityCaption(metaClass);
            } else {
                dataType = messages.getMessage("classNotFound");
            }
        } else {
            String key = AttributeType.class.getSimpleName() + "." + categoryAttribute.getDataType().toString();
            dataType = messages.getMessage(AttributeType.class, key);
        }

        return dataType;
    }

    @Install(to = "categoryAttrsTable.create", subject = "afterCommitHandler")
    protected void categoryAttrsTableCreateAfterCommitHandler(CategoryAttribute categoryAttribute) {
        int orderNo = getMaxOrderNo(categoryAttribute) + 1;
        categoryAttributesDc.getItem(categoryAttribute.getId()).setOrderNo(orderNo);
    }

    @Subscribe("categoryAttrsTable.moveUp")
    protected void onCategoryAttrsTableMoveUp(Action.ActionPerformedEvent event) {
        Set<CategoryAttribute> selectedItem = categoryAttrsTable.getSelected();
        if (selectedItem.isEmpty()) {
            return;
        }

        CategoryAttribute selectedAttribute = selectedItem.iterator().next();
        Integer selectedAttributeOrderNo = selectedAttribute.getOrderNo();

        CategoryAttribute prevAttribute = getPrevAttribute(selectedAttributeOrderNo);
        if (prevAttribute != null) {
            selectedAttribute.setOrderNo(prevAttribute.getOrderNo());
            prevAttribute.setOrderNo(selectedAttributeOrderNo);
            sortCategoryAttrsTableByOrderNo();
        }
    }

    @Subscribe("categoryAttrsTable.moveDown")
    protected void onCategoryAttrsTableMoveDown(Action.ActionPerformedEvent event) {
        Set<CategoryAttribute> selectedItem = categoryAttrsTable.getSelected();
        if (selectedItem.isEmpty()) {
            return;
        }

        CategoryAttribute selectedAttribute = selectedItem.iterator().next();
        Integer selectedAttributeOrderNo = selectedAttribute.getOrderNo();

        CategoryAttribute nextAttribute = getNextAttribute(selectedAttributeOrderNo);
        if (nextAttribute != null) {
            selectedAttribute.setOrderNo(nextAttribute.getOrderNo());
            nextAttribute.setOrderNo(selectedAttributeOrderNo);
            sortCategoryAttrsTableByOrderNo();
        }
    }

    protected int getMaxOrderNo(CategoryAttribute categoryAttribute) {
        return categoryAttributesDc.getItems()
                .stream()
                .filter(attribute -> !attribute.equals(categoryAttribute))
                .mapToInt(CategoryAttribute::getOrderNo)
                .max()
                .orElse(0);
    }

    protected CategoryAttribute getPrevAttribute(Integer orderNo) {
        return categoryAttributesDc.getMutableItems()
                .stream()
                .filter(categoryAttribute -> orderNo.compareTo(categoryAttribute.getOrderNo()) > 0)
                .max(Comparator.comparing(CategoryAttribute::getOrderNo))
                .orElse(null);
    }

    protected CategoryAttribute getNextAttribute(Integer orderNo) {
        return categoryAttributesDc.getMutableItems()
                .stream()
                .filter(categoryAttribute -> orderNo.compareTo(categoryAttribute.getOrderNo()) < 0)
                .min(Comparator.comparing(CategoryAttribute::getOrderNo))
                .orElse(null);
    }

    protected void sortCategoryAttrsTableByOrderNo() {
        categoryAttributesDc.getSorter().sort(Sort.by(Sort.Direction.ASC, "orderNo"));
    }
}
