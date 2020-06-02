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
package io.jmix.ui.component.impl;

import com.google.common.collect.Lists;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaPropertyPath;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.component.Table;
import io.jmix.ui.component.columnmanager.GroupColumnManager;
import io.jmix.ui.component.data.GroupTableItems;
import io.jmix.ui.component.data.TableItems;
import io.jmix.ui.component.data.ValueConversionException;
import io.jmix.ui.component.table.GroupTableDataContainer;
import io.jmix.ui.component.table.TableDataContainer;
import io.jmix.ui.component.table.TableItemsEventsDelegate;
import io.jmix.ui.gui.data.GroupInfo;
import io.jmix.ui.settings.component.binder.ComponentSettingsBinder;
import io.jmix.ui.settings.component.binder.GroupTableSettingsBinder;
import io.jmix.ui.widget.JmixEnhancedTable.AggregationInputValueChangeContext;
import io.jmix.ui.widget.JmixGroupTable;
import io.jmix.ui.widget.JmixGroupTable.GroupAggregationContext;
import io.jmix.ui.widget.JmixGroupTable.GroupAggregationInputValueChangeContext;
import io.jmix.ui.widget.data.AggregationContainer;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.emptyToNull;
import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

@SuppressWarnings("deprecation")
public class WebGroupTable<E extends Entity> extends WebAbstractTable<JmixGroupTable, E>
        implements GroupTable<E>, GroupColumnManager {

    protected Map<Table.Column, GroupAggregationCells> groupAggregationCells = null;

    protected boolean rerender = true;
    protected boolean showItemsCountForGroup = true;

    protected GroupCellValueFormatter<E> groupCellValueFormatter;

    public WebGroupTable() {
        component = createComponent();
    }

    @Override
    public void setItems(TableItems<E> tableItems) {
        if (tableItems != null &&
                !(tableItems instanceof GroupTableItems)) {
            throw new IllegalArgumentException("GroupTable supports only GroupTableItems data binding");
        }

        super.setItems(tableItems);
    }

    @Override
    protected TableDataContainer<E> createTableDataContainer(TableItems<E> tableItems) {
        return new AggregatableGroupTableDataContainer<>((GroupTableItems<E>) tableItems, this);
    }

    protected class AggregatableGroupTableDataContainer<I> extends GroupTableDataContainer<I>
            implements AggregationContainer {

        protected List<Object> aggregationProperties = null;

        public AggregatableGroupTableDataContainer(GroupTableItems<I> tableSource,
                                                   TableItemsEventsDelegate<I> dataEventsDelegate) {
            super(tableSource, dataEventsDelegate);
        }

        @Override
        protected void doGroup(Object[] properties) {
            super.doGroup(properties);

            if (aggregationCells != null) {
                if (hasGroups()) {
                    if (groupAggregationCells == null) {
                        groupAggregationCells = new HashMap<>();
                    } else {
                        groupAggregationCells.clear();
                    }
                    fillGroupAggregationCells(groupAggregationCells);
                } else {
                    groupAggregationCells = null;
                }
            }
        }

        protected void fillGroupAggregationCells(Map<Table.Column, GroupAggregationCells> cells) {
            Collection roots = rootGroups();
            for (final Object rootGroup : roots) {
                __fillGroupAggregationCells(rootGroup, cells);
            }
        }

        protected void __fillGroupAggregationCells(Object groupId, Map<Table.Column, GroupAggregationCells> cells) {
            Set<Table.Column> aggregatableColumns = aggregationCells.keySet();

            for (final Column column : aggregatableColumns) {
                if (!columns.get(getGroupProperty(groupId)).equals(column)) {
                    GroupAggregationCells groupCells = cells.get(column);
                    if (groupCells == null) {
                        groupCells = new GroupAggregationCells();
                        cells.put(column, groupCells);
                    }
                    groupCells.addCell(groupId, "");
                }
            }

            if (hasChildren(groupId)) {
                Collection children = getChildren(groupId);
                for (Object child : children) {
                    __fillGroupAggregationCells(child, cells);
                }
            }
        }

        @Override
        public Collection getAggregationPropertyIds() {
            if (aggregationProperties != null) {
                return Collections.unmodifiableList(aggregationProperties);
            }
            return Collections.emptyList();
        }

        @Override
        public void addContainerPropertyAggregation(Object propertyId, Type type) {
            if (aggregationProperties == null) {
                aggregationProperties = new ArrayList<>();
            } else if (aggregationProperties.contains(propertyId)) {
                throw new IllegalStateException(String.format("Aggregation property %s already exists", propertyId));
            }
            aggregationProperties.add(propertyId);
        }

        @Override
        public void removeContainerPropertyAggregation(Object propertyId) {
            if (aggregationProperties != null) {
                aggregationProperties.remove(propertyId);
                if (aggregationProperties.isEmpty()) {
                    aggregationProperties = null;
                }
            }
        }

        @Override
        public Map<Object, Object> aggregate(Context context) {
            return __aggregate(this, context);
        }

        @Override
        public Map<Object, Object> aggregateValues(Context context) {
            return __aggregateValues(this, context);
        }
    }

    @Override
    protected void initComponent(JmixGroupTable component) {
        super.initComponent(component);

        component.setGroupPropertyValueFormatter((groupId, value) ->
                formatAggregatableGroupPropertyValue((GroupInfo<MetaPropertyPath>) groupId, value)
        );
    }

    protected JmixGroupTable createComponent() {
        return new JmixGroupTableExt();
    }

    @Override
    protected ComponentSettingsBinder getSettingsBinder() {
        return beanLocator.get(GroupTableSettingsBinder.NAME);
    }

    @Override
    protected Map<Object, Object> __handleAggregationResults(AggregationContainer.Context context,
                                                             Map<Object, Object> results) {
        if (context instanceof GroupAggregationContext) {
            GroupAggregationContext groupContext = (GroupAggregationContext) context;

            for (Map.Entry<Object, Object> entry : results.entrySet()) {
                Table.Column column = columns.get(entry.getKey());
                GroupAggregationCells cells;
                if ((cells = groupAggregationCells.get(column)) != null) {
                    String value = getFormattedValue(column, entry.getValue());
                    cells.cells.put(groupContext.getGroupId(), value);
                }
            }

            return results;
        } else {
            return super.__handleAggregationResults(context, results);
        }
    }

    protected Object[] getNewColumnOrder(Object[] newGroupProperties) {
        List<Object> allProps = Lists.newArrayList(component.getVisibleColumns()); // mutable list required
        List<Object> newGroupProps = Arrays.asList(newGroupProperties);

        allProps.removeAll(newGroupProps);
        allProps.addAll(0, newGroupProps);

        return allProps.toArray();
    }

    protected List<Object> collectPropertiesByColumns(String... columnIds) {
        List<Object> properties = new ArrayList<>(columnIds.length);

        for (String columnId : columnIds) {
            Column column = getColumn(columnId);

            if (column == null) {
                throw new IllegalArgumentException("There is no column with the given id: " + columnId);
            }

            properties.add(column.getId());
        }

        return properties;
    }

    protected void validateProperties(Object[] properties) {
        for (Object property : properties) {
            if (!(property instanceof MetaPropertyPath)) {
                throw new IllegalArgumentException("Only MetaPropertyPaths are supported by the \"groupBy\" method.");
            }
        }
    }

    @Override
    public void groupBy(Object[] properties) {
        checkNotNullArgument(properties);
        validateProperties(properties);

        if (uselessGrouping(properties)) {
            return;
        }

        component.groupBy(properties);
        component.setColumnOrder(getNewColumnOrder(properties));
    }

    @Override
    public void groupByColumns(String... columnIds) {
        checkNotNullArgument(columnIds);

        if (uselessGrouping(columnIds)) {
            return;
        }

        groupBy(collectPropertiesByColumns(columnIds).toArray());
    }

    @Override
    public void ungroupByColumns(String... columnIds) {
        checkNotNullArgument(columnIds);

        if (uselessGrouping(columnIds)) {
            return;
        }

        Object[] remainingGroups = CollectionUtils
                .removeAll(component.getGroupProperties(), collectPropertiesByColumns(columnIds))
                .toArray();

        groupBy(remainingGroups);
    }

    @Override
    public void ungroup() {
        groupBy(new Object[]{});
    }

    protected boolean uselessGrouping(Object[] newGroupProperties) {
        return (newGroupProperties == null || newGroupProperties.length == 0) &&
                component.getGroupProperties().isEmpty();
    }

    @Override
    public void setColumnGroupAllowed(String columnId, boolean allowed) {
        Column column = getColumnNN(columnId);
        setColumnGroupAllowed(column, allowed);
    }

    @Nonnull
    protected Column getColumnNN(String columnId) {
        Column column = getColumn(columnId);
        if (column == null) {
            throw new IllegalStateException(String.format("Column with id '%s' not found", columnId));
        }

        return column;
    }

    @Override
    public void setColumnGroupAllowed(Column column, boolean allowed) {
        checkNotNullArgument(column, "column must be non null");

        if (column.isGroupAllowed() != allowed) {
            column.setGroupAllowed(allowed);
        }
        component.setColumnGroupAllowed(column.getId(), allowed);
    }

    @Override
    public GroupCellValueFormatter<E> getGroupCellValueFormatter() {
        return groupCellValueFormatter;
    }

    @Override
    public void setGroupCellValueFormatter(GroupCellValueFormatter<E> formatter) {
        this.groupCellValueFormatter = formatter;
    }

    @Override
    public void expandAll() {
        component.expandAll();
    }

    @Override
    public void expand(GroupInfo groupId) {
        component.expand(groupId);
    }

    @Override
    public void expandPath(Entity item) {
        if (component.hasGroups()) {
            expandGroupsFor((Collection<GroupInfo>) component.rootGroups(), EntityValues.getId(item));
        }
    }

    protected void expandGroupsFor(Collection<GroupInfo> groupSlice, Object itemId) {
        for (GroupInfo g : groupSlice) {
            if (component.getGroupItemIds(g).contains(itemId)) {
                component.expand(g);

                if (component.hasChildren(g)) {
                    expandGroupsFor((Collection<GroupInfo>) component.getChildren(g), itemId);
                }
                return;
            }
        }
    }

    @Override
    public void collapseAll() {
        component.collapseAll();
    }

    @Override
    public void collapse(GroupInfo groupId) {
        component.collapse(groupId);
    }

    @Override
    public boolean isExpanded(GroupInfo groupId) {
        return component.isExpanded(groupId);
    }

    @Override
    public boolean isFixedGrouping() {
        return component.isFixedGrouping();
    }

    @Override
    public void setFixedGrouping(boolean fixedGrouping) {
        component.setFixedGrouping(fixedGrouping);
    }

    @Override
    public boolean isShowItemsCountForGroup() {
        return showItemsCountForGroup;
    }

    @Override
    public void setShowItemsCountForGroup(boolean showItemsCountForGroup) {
        this.showItemsCountForGroup = showItemsCountForGroup;
    }

    @Override
    protected String getGeneratedCellStyle(Object itemId, Object propertyId) {
        if (itemId instanceof GroupInfo) {
            GroupInfo groupInfo = (GroupInfo) itemId;

            String joinedStyle = styleProviders.stream()
                    .filter(sp -> sp instanceof GroupStyleProvider)
                    .map(sp -> ((GroupStyleProvider) sp).getStyleName(groupInfo))
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(" "));

            return emptyToNull(joinedStyle);
        } else {
            return super.getGeneratedCellStyle(itemId, propertyId);
        }
    }

    @Override
    public Map<Object, Object> getAggregationResults(GroupInfo info) {
        return component.aggregateValues(new GroupAggregationContext(component, info));
    }

    @Override
    public void selectAll() {
        if (isMultiSelect()) {
            if (getItems() instanceof GroupTableItems) {
                GroupTableItems<E> tableSource = (GroupTableItems<E>) getItems();
                Collection<?> itemIds = tableSource.hasGroups()
                        ? getAllItemIds(tableSource)
                        : tableSource.getItemIds();

                if (!itemIds.isEmpty()) {
                    // Filter items that don't exist in the datasource, e.g. GroupInfo
                    itemIds = itemIds.stream()
                            .filter(tableSource::containsId)
                            .collect(Collectors.toList());
                }

                component.setValue(itemIds);
                return;
            }
        }
        super.selectAll();
    }

    protected List<Object> getAllItemIds(GroupTableItems<E> tableSource) {
        List<GroupInfo> roots = tableSource.rootGroups();
        List<Object> result = new ArrayList<>();
        for (final GroupInfo root : roots) {
            result.add(root);
            collectItemIds(root, result);
        }
        return result;
    }

    protected void collectItemIds(GroupInfo groupId, final List<Object> itemIds) {
        GroupTableItems<E> groupTableSource = (GroupTableItems<E>) getItems();
        if (groupTableSource.hasChildren(groupId)) {
            final List<GroupInfo> children = groupTableSource.getChildren(groupId);
            for (final GroupInfo child : children) {
                itemIds.add(child);
                collectItemIds(child, itemIds);
            }
        } else {
            itemIds.addAll(groupTableSource.getGroupItemIds(groupId));
        }
    }

    @Override
    public void setAggregationDistributionProvider(AggregationDistributionProvider<E> distributionProvider) {
        this.distributionProvider = distributionProvider;

        component.setAggregationDistributionProvider(this::distributeGroupAggregation);
    }

    @SuppressWarnings("unchecked")
    protected boolean distributeGroupAggregation(AggregationInputValueChangeContext context) {
        if (distributionProvider != null) {
            String value = context.getValue();
            Object columnId = context.getColumnId();
            GroupInfo groupInfo = null;
            try {
                Object parsedValue = getParsedAggregationValue(value, columnId);
                Collection<E> scope = Collections.emptyList();

                if (context.isTotalAggregation()) {
                    TableItems<E> tableItems = getItems();
                    scope = tableItems == null ? Collections.emptyList() : tableItems.getItems();
                } else if (context instanceof GroupAggregationInputValueChangeContext) {
                    Object groupId = ((GroupAggregationInputValueChangeContext) context).getGroupInfo();
                    if (groupId instanceof GroupInfo) {
                        groupInfo = (GroupInfo) groupId;
                        scope = ((GroupTableItems) getItems()).getChildItems(groupInfo);
                    }
                }

                GroupAggregationDistributionContext<E> aggregationDistribution =
                        new GroupAggregationDistributionContext(getColumnNN(columnId.toString()),
                                parsedValue, scope, groupInfo, context.isTotalAggregation());
                distributionProvider.onDistribution(aggregationDistribution);
            } catch (ValueConversionException e) {
                showParseErrorNotification(e.getLocalizedMessage());
                return false; // rollback to previous value
            } catch (ParseException e) {
                showParseErrorNotification(messages.getMessage("validationFail"));
                return false; // rollback to previous value
            }
        }
        return true;
    }

    protected String formatAggregatableGroupPropertyValue(GroupInfo<MetaPropertyPath> groupId, @Nullable Object value) {
        String formattedValue = formatGroupPropertyValue(groupId, value);

        if (groupCellValueFormatter != null) {
            List<Entity> groupItems = component.getGroupItemIds(groupId).stream()
                    .map(itemId -> {
                        TableDataContainer container = (TableDataContainer) component.getContainerDataSource();
                        return (Entity) container.getInternalItem(itemId);
                    })
                    .collect(Collectors.toList());

            GroupCellContext<E> context = new GroupCellContext<>(groupId, value, formattedValue, (List<E>) groupItems);
            return groupCellValueFormatter.format(context);
        }

        if (showItemsCountForGroup) {
            int count = this.component.getGroupItemsCount(groupId);
            return String.format("%s (%d)", formattedValue == null ? "" : formattedValue, count);
        } else {
            return formattedValue == null ? "" : formattedValue;
        }
    }

    protected String formatGroupPropertyValue(GroupInfo<MetaPropertyPath> groupId, @Nullable Object value) {
        if (value == null) {
            return "";
        }

        MetaPropertyPath propertyPath = groupId.getProperty();
        Table.Column<E> column = columns.get(propertyPath);
        if (column != null) {
            if (column.getFormatter() != null) {
                return column.getFormatter().apply(value);
            } else if (column.getXmlDescriptor() != null) {
                String captionProperty = column.getXmlDescriptor().attributeValue("captionProperty"); // vaadin8 move to Column
                if (StringUtils.isNotEmpty(captionProperty)) {
                    Collection<?> children = component.getGroupItemIds(groupId);
                    if (children.isEmpty()) {
                        return null;
                    }

                    Object itemId = children.iterator().next();

                    TableDataContainer container = (TableDataContainer) component.getContainerDataSource();

                    Entity item = (Entity) container.getInternalItem(itemId);
                    Object captionValue = EntityValues.getValueEx(item, captionProperty);

                    // vaadin8 use metadataTools format with metaproperty
                    return metadataTools.format(captionValue);
                }
            }
        }

        return metadataTools.format(value, propertyPath.getMetaProperty());
    }

    protected static class GroupAggregationCells {
        protected Map<Object, String> cells = new HashMap<>();

        public void addCell(Object groupId, String value) {
            cells.put(groupId, value);
        }

        public String getValue(Object groupId) {
            return cells.get(groupId);
        }
    }

    @Override
    public void addColumn(Column<E> column) {
        super.addColumn(column);

        setColumnGroupAllowed(column, column.isGroupAllowed());
    }

    protected class JmixGroupTableExt extends JmixGroupTable {
        @Override
        protected boolean isNonGeneratedProperty(Object id) {
            return (id instanceof MetaPropertyPath);
        }

        @Override
        public void groupBy(Object[] properties) {
            groupBy(properties, rerender);
        }

        @Override
        protected LinkedHashSet<Object> getItemIdsInRange(Object startItemId, int length) {
            Set<Object> rootIds = super.getItemIdsInRange(startItemId, length);
            LinkedHashSet<Object> ids = new LinkedHashSet<>();
            for (Object itemId : rootIds) {
                if (itemId instanceof GroupInfo) {
                    if (!isExpanded(itemId)) {
                        Collection<?> itemIds = getGroupItemIds(itemId);
                        ids.addAll(itemIds);
                        expand(itemId, true);
                    }

                    List<GroupInfo> children = (List<GroupInfo>) getChildren(itemId);
                    for (GroupInfo groupInfo : children) {
                        if (!isExpanded(groupInfo)) {
                            expand(groupInfo, true);
                        }
                    }
                } else {
                    ids.add(itemId);
                }
            }
            return ids;
        }
    }
}
