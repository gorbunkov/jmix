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

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import io.jmix.core.*;
import io.jmix.core.entity.HasUuid;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.dynattr.AttributeType;
import io.jmix.dynattr.OptionsLoaderType;
import io.jmix.dynattr.impl.model.Category;
import io.jmix.dynattr.impl.model.CategoryAttribute;
import io.jmix.dynattr.impl.model.CategoryAttributeConfiguration;
import io.jmix.dynattrui.impl.model.ScreenAndComponent;
import io.jmix.dynattrui.screen.localization.AttributeLocalizationFragment;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Fragments;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.actions.Action;
import io.jmix.ui.components.*;
import io.jmix.ui.components.autocomplete.JpqlSuggestionFactory;
import io.jmix.ui.components.autocomplete.Suggestion;
import io.jmix.ui.components.data.options.MapOptions;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.DataContext;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

import static io.jmix.dynattr.AttributeType.BOOLEAN;
import static io.jmix.dynattr.AttributeType.DATE;
import static io.jmix.dynattr.AttributeType.DATE_WITHOUT_TIME;
import static io.jmix.dynattr.AttributeType.DECIMAL;
import static io.jmix.dynattr.AttributeType.DOUBLE;
import static io.jmix.dynattr.AttributeType.ENTITY;
import static io.jmix.dynattr.AttributeType.ENUMERATION;
import static io.jmix.dynattr.AttributeType.INTEGER;
import static io.jmix.dynattr.AttributeType.STRING;
import static io.jmix.dynattr.OptionsLoaderType.GROOVY;
import static io.jmix.dynattr.OptionsLoaderType.JPQL;
import static io.jmix.dynattr.OptionsLoaderType.SQL;
import static java.lang.String.format;
import static org.eclipse.persistence.jpa.jpql.parser.Expression.WHERE;

@UiController("sys$CategoryAttribute.edit")
@UiDescriptor("category-attrs-edit.xml")
@LoadDataBeforeShow
@EditedEntityContainer("categoryAttributeDc")
@DialogMode(forceDialog = true)
public class CategoryAttrsEdit extends StandardEditor<CategoryAttribute> {

    protected static final String DATA_TYPE_PROPERTY = "dataType";
    protected static final String DEFAULT_DATE_IS_CURRENT_PROPERTY = "defaultDateIsCurrent";
    protected static final String ENTITY_CLASS_PROPERTY = "entityClass";
    protected static final String JOIN_CLAUSE_PROPERTY = "joinClause";
    protected static final String LOOKUP_PROPERTY = "lookup";
    protected static final String NAME_PROPERTY = "name";
    protected static final String SCREEN_PROPERTY = "screen";
    protected static final String WHERE_CLAUSE_PROPERTY = "whereClause";

    protected static final String MAIN_TAB_NAME = "mainTab";
    protected static final String ONE_COLUMN_WIDTH = "610px";
    protected static final String TWO_COLUMNS_WIDTH = "854px";
    protected static final String MESSAGE_DIALOG_WIDTH = "560px";

    protected static final String CONFIGURATION_NUMBER_FORMAT_PATTERN_PROPERTY = "numberFormatPattern";
    protected static final String CONFIGURATION_OPTIONS_LOADER_TYPE_PROPERTY = "optionsLoaderType";

    protected static final Multimap<AttributeType, String> FIELDS_VISIBLE_FOR_TYPES = ArrayListMultimap.create();
    protected static final Set<AttributeType> SUPPORTED_OPTIONS_TYPES = ImmutableSet.of(STRING, DOUBLE, DECIMAL, INTEGER, ENTITY);

    static {
        FIELDS_VISIBLE_FOR_TYPES.put(BOOLEAN, "defaultBooleanField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "defaultStringField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(STRING, "rowsCountField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "defaultDoubleField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "minDoubleField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "maxDoubleField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DOUBLE, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "defaultDecimalField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "minDecimalField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "maxDecimalField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "numberFormatPatternField");
        FIELDS_VISIBLE_FOR_TYPES.put(DECIMAL, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "defaultIntField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "minIntField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "maxIntField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(INTEGER, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "defaultDateField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "defaultDateIsCurrentField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "defaultDateWithoutTimeField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "defaultDateIsCurrentField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(DATE_WITHOUT_TIME, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "enumerationBox");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "defaultStringField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "widthField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENUMERATION, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "entityClassField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "screenField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "lookupField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "defaultEntityIdField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "isCollectionField");
        FIELDS_VISIBLE_FOR_TYPES.put(ENTITY, "widthField");
    }

    @Inject
    protected CoreProperties coreProperties;
    @Inject
    protected Fragments fragments;
    @Inject
    protected MetadataTools metadataTools;
    @Inject
    protected MessageTools messageTools;
    @Inject
    protected Metadata metadata;
    @Inject
    protected DataManager dataManager;
    @Inject
    protected ReferenceToEntitySupport referenceToEntitySupport;
    @Inject
    protected FetchPlanRepository fetchPlanRepository;
    @Inject
    protected UiComponents uiComponents;
    @Inject
    protected Messages messages;
    @Inject
    protected ScreenBuilders screenBuilders;
    @Inject
    protected Dialogs dialogs;
    @Inject
    protected Notifications notifications;

    @Inject
    protected CheckBox lookupField;
    @Inject
    protected DateField<Date> defaultDateField;
    @Inject
    protected DateField<LocalDate> defaultDateWithoutTimeField;
    @Inject
    protected Form optionalAttributeForm;
    @Inject
    protected LinkButton constraintWizardField;
    @Inject
    protected LookupField<AttributeType> dataTypeField;
    @Inject
    protected LookupField<String> entityClassField;
    @Inject
    protected LookupField<String> screenField;
    @Inject
    protected LookupField<Boolean> defaultBooleanField;
    @Inject
    protected LookupField<OptionsLoaderType> optionsLoaderTypeField;
    @Inject
    protected PickerField<Entity> defaultEntityIdField; // TODO ListEditor
    @Inject
    protected SourceCodeEditor optionsLoaderScriptField;
    @Inject
    protected SourceCodeEditor joinClauseField;
    @Inject
    protected SourceCodeEditor whereClauseField;
    @Inject
    protected SourceCodeEditor validationScriptField;
    @Inject
    protected SourceCodeEditor recalculationScriptField;
    @Inject
    protected GroupTable<ScreenAndComponent> targetScreensTable;
    @Inject
    protected TabSheet tabSheet;
    @Inject
    protected TextField<String> codeField;

    @Inject
    protected CollectionContainer<ScreenAndComponent> targetScreensDc;
    @Inject
    protected InstanceContainer<CategoryAttributeConfiguration> configurationDc;

    protected AttributeLocalizationFragment localizationFragment;

    protected List<ScreenAndComponent> targetScreens = new ArrayList<>();

    @Subscribe
    protected void onInit(InitEvent event) {
        setDialogWindowWidth(ONE_COLUMN_WIDTH);

        initAttributeForm();
        initCalculatedValuesAndOptionsForm();
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        initTargetScreensTable();
        initCategoryAttributeConfigurationField();
        initLocalizationTab();

        // TODO ListEditor
        // dependsOnAttributesListEditor.setOptionsList(getAttributesOptions());

        setupNumberFormat();
        refreshAttributesUI();
        centerDialogWindow();
    }

    @Subscribe("tabSheet")
    protected void onTabSheetSelectedTabChange(TabSheet.SelectedTabChangeEvent event) {
        String tabName = event.getSelectedTab().getName();
        String dialogWidth;
        if (MAIN_TAB_NAME.equals(tabName) && getEditedEntity().getDataType() != null) {
            dialogWidth = TWO_COLUMNS_WIDTH;
        } else {
            dialogWidth = ONE_COLUMN_WIDTH;
        }
        setDialogWindowWidth(dialogWidth);
        centerDialogWindow();
    }

    @Subscribe("defaultEntityIdField")
    protected void onDefaultEntityIdFieldValueChange(HasValue.ValueChangeEvent<Entity> event) {
        Entity entity = event.getValue();
        Object objectDefaultEntityId = null;
        if (entity != null) {
            objectDefaultEntityId = referenceToEntitySupport.getReferenceId(entity);
        }

        getEditedEntity().setObjectDefaultEntityId(objectDefaultEntityId);
    }

    @Subscribe(id = "categoryAttributeDc", target = Target.DATA_CONTAINER)
    protected void onCategoryAttributeDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<CategoryAttribute> event) {
        String property = event.getProperty();
        if (DATA_TYPE_PROPERTY.equals(property)
                || LOOKUP_PROPERTY.equals(property)
                || DEFAULT_DATE_IS_CURRENT_PROPERTY.equals(property)
                || ENTITY_CLASS_PROPERTY.equals(property)) {
            refreshAttributesUI();
            refreshAttributesValues();
        }

        if (event.getPrevValue() == null
                && DATA_TYPE_PROPERTY.equals(property)) {
            centerDialogWindow();
        }

        if (NAME_PROPERTY.equals(property)) {
            refreshCodeFieldValue();
        }

        if (SCREEN_PROPERTY.equals(property)
                || JOIN_CLAUSE_PROPERTY.equals(property)
                || WHERE_CLAUSE_PROPERTY.equals(property)) {
            // todo: dynamic attributes (init picker field)
            //dynamicAttributesGuiTools.initEntityPickerField(defaultEntityIdField, e.getItem());
        }
    }

    @Subscribe(id = "configurationDc", target = Target.DATA_CONTAINER)
    protected void onConfigurationDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<CategoryAttributeConfiguration> event) {
        String property = event.getProperty();
        if (CONFIGURATION_NUMBER_FORMAT_PATTERN_PROPERTY.equals(property)) {
            setupNumberFormat();
        }

        if (CONFIGURATION_OPTIONS_LOADER_TYPE_PROPERTY.equals(property)) {
            refreshAttributesUI();
            refreshAttributesValues();
        }
    }

    // TODO ListEditor
    @Subscribe("editEnumerationBtn")
    protected void onEditEnumerationBtnClick(Button.ClickEvent event) {
        AttributeEnumerationScreen enumerationScreen = screenBuilders.screen(this)
                .withScreenClass(AttributeEnumerationScreen.class)
                .withAfterCloseListener(afterScreenCloseEvent -> {
                    if (afterScreenCloseEvent.closedWith(StandardOutcome.COMMIT)) {
                        AttributeEnumerationScreen screen = afterScreenCloseEvent.getScreen();
                        getEditedEntity().setEnumeration(screen.getEnumeration());
                        getEditedEntity().setEnumerationLocales(screen.getEnumerationLocales());
                    }
                })
                .build();

        enumerationScreen.setEnumeration(getEditedEntity().getEnumeration());
        enumerationScreen.setEnumerationLocales(getEditedEntity().getEnumerationLocales());
        enumerationScreen.show();
    }

    @Install(to = "targetScreensDl", target = Target.DATA_LOADER)
    protected List<ScreenAndComponent> targetScreensDlLoadDelegate(LoadContext<ScreenAndComponent> loadContext) {
        return targetScreens;
    }

    @Subscribe("targetScreensTable.create")
    protected void onTargetScreensTableCreate(Action.ActionPerformedEvent event) {
        targetScreensDc.getMutableItems().add(metadata.create(ScreenAndComponent.class));
    }

    @Subscribe("constraintWizardField")
    protected void onConstraintWizardFieldClick(Button.ClickEvent event) {
        CategoryAttribute attribute = getEditedEntity();
        Class<?> entityClass = attribute.getJavaType();

        if (entityClass == null) {
            notifications.create()
                    .withCaption(messages.getMessage(CategoryAttrsEdit.class, "selectEntityType"))
                    .show();
            return;
        }

        MetaClass metaClass = metadata.getClass(entityClass);

        /* TODO filter support
        FakeFilterSupport filterSupport = new FakeFilterSupport(this, metaClass);
        Filter fakeFilter = filterSupport.createFakeFilter();
        FilterEntity filterEntity = filterSupport.createFakeFilterEntity(attribute.getFilterXml());
        ConditionsTree conditionsTree = filterSupport.createFakeConditionsTree(fakeFilter, filterEntity);

        Map<String, Object> params = new HashMap<>();
        params.put("filter", fakeFilter);
        params.put("filterEntity", filterEntity);
        params.put("conditionsTree", conditionsTree);
        params.put("useShortConditionForm", true);

        FilterEditor filterEditor = (FilterEditor) openWindow("filterEditor", OpenType.DIALOG, params);
        filterEditor.addCloseListener(actionId -> {
            if (!COMMIT_ACTION_ID.equals(actionId)) {
                return;
            }

            filterEntity.setXml(filterParser.getXml(filterEditor.getConditions(), Param.ValueProperty.DEFAULT_VALUE));

            if (filterEntity.getXml() != null) {
                Element element = dom4JTools.readDocument(filterEntity.getXml()).getRootElement();
                com.haulmont.cuba.core.global.filter.FilterParser filterParser =
                        new com.haulmont.cuba.core.global.filter.FilterParser(element);
                String jpql = new SecurityJpqlGenerator().generateJpql(filterParser.getRoot());
                attribute.setWhereClause(jpql);
                Set<String> joins = filterParser.getRoot().getJoins();
                if (!joins.isEmpty()) {
                    String joinsStr = new TextStringBuilder().appendWithSeparators(joins, " ").toString();
                    attribute.setJoinClause(joinsStr);
                }
                attribute.setFilterXml(filterEntity.getXml());
            }
        });*/
    }

    protected void initAttributeForm() {
        defaultBooleanField.setOptionsMap(getBooleanOptions());
        dataTypeField.setOptionsMap(getDataTypeOptions());
        entityClassField.setOptionsMap(getEntityOptions());
        validationScriptField.setContextHelpIconClickHandler(e -> showMessageDialog(
                messages.getMessage(CategoryAttrsEdit.class, "validationScript"),
                messages.getMessage(CategoryAttrsEdit.class, "validationScriptHelp")
        ));
    }

    protected void initCalculatedValuesAndOptionsForm() {
        recalculationScriptField.setContextHelpIconClickHandler(e -> showMessageDialog(
                messages.getMessage(CategoryAttrsEdit.class, "recalculationScript"),
                messages.getMessage(CategoryAttrsEdit.class, "recalculationScriptHelp")
        ));

        /* TODO ListEditor
        dependsOnAttributesListEditor = uiComponents.create(ListEditor.NAME);
        dependsOnAttributesListEditor.setValueSource(new DatasourceValueSource(configurationDs, "dependsOnAttributes"));
        dependsOnAttributesListEditor.setWidth(fieldWidth);
        dependsOnAttributesListEditor.setFrame(frame);
        dependsOnAttributesListEditor.setItemType(ListEditor.ItemType.ENTITY);
        dependsOnAttributesListEditor.setEntityName("sys$CategoryAttribute");
        dependsOnAttributesListEditor.addValidator(categoryAttributes -> {
            if (recalculationScript.getValue() != null && CollectionUtils.isEmpty(categoryAttributes)) {
                throw new ValidationException(getMessage("dependsOnAttributesValidationMsg"));
            }
        });

        calculatedAttrsAndOptionsFieldGroup.getFieldNN("dependsOnAttributes").setComponent(dependsOnAttributesListEditor);*/

        whereClauseField.setSuggester((source, text, cursorPosition) -> requestHint(whereClauseField, cursorPosition));
        joinClauseField.setSuggester((source, text, cursorPosition) -> requestHint(joinClauseField, cursorPosition));
    }

    protected void initTargetScreensTable() {
        loadTargetScreens();

        Category category = getEditedEntity().getCategory();
        if (category != null) {
            MetaClass categorizedEntityMetaClass = metadata.getClass(getEditedEntity().getCategory().getEntityType());
            Map<String, String> optionsMap = new HashMap<>();
        /* TODO screensHelper
        Map<String, String> optionsMap = categorizedEntityMetaClass != null ?
                new HashMap<>(screensHelper.getAvailableScreens(categorizedEntityMetaClass.getJavaClass(), true)) :
                new HashMap<>();*/

            targetScreensTable.addGeneratedColumn(
                    "screen",
                    entity -> {
                        LookupField<String> lookupField = uiComponents.create(LookupField.class);
                        lookupField.setOptionsMap(optionsMap);
                        //noinspection RedundantCast
                        lookupField.setNewOptionHandler((Consumer<String>) caption -> {
                            if (caption != null && !optionsMap.containsKey(caption)) {
                                optionsMap.put(caption, caption);
                                lookupField.setValue(caption);
                            }
                        });
                        lookupField.setRequired(true);
                        lookupField.setWidth("100%");
                        return lookupField;
                    }
            );
        }
    }

    protected void loadTargetScreens() {
        targetScreens.clear();
        Set<String> targetScreensSet = getEditedEntity().getTargetScreensSet();
        for (String targetScreen : targetScreensSet) {
            ScreenAndComponent screenAndComponent = metadata.create(ScreenAndComponent.class);
            String screen;
            String component = null;

            if (targetScreen.contains("#")) {
                String[] split = targetScreen.split("#");
                screen = split[0];
                component = split[1];
            } else {
                screen = targetScreen;
            }

            screenAndComponent.setScreen(screen);
            screenAndComponent.setComponent(component);

            targetScreens.add(screenAndComponent);
        }
    }

    protected void initCategoryAttributeConfigurationField() {
        CategoryAttribute attribute = getEditedEntity();
        CategoryAttributeConfiguration configuration = attribute.getConfiguration();

        if (ENTITY.equals(attribute.getDataType())
                && Boolean.TRUE.equals(attribute.getLookup())
                && configuration.getOptionsLoaderType() == null) {
            optionsLoaderTypeField.setValue(JPQL);
        }
    }

    protected void initLocalizationTab() {
        if (coreProperties.getAvailableLocales().size() > 1) {
            TabSheet.Tab localizationTab = tabSheet.getTab("localizationTab");
            localizationTab.setVisible(true);

            VBoxLayout localizationTabComponent = (VBoxLayout) tabSheet.getTabComponent("localizationTab");
            localizationFragment = fragments.create(this, AttributeLocalizationFragment.class);
            localizationFragment.setNameMsgBundle(getEditedEntity().getNameMsgBundle());
            localizationFragment.setDescriptionMsgBundle(getEditedEntity().getDescriptionsMsgBundle());

            Fragment fragment = localizationFragment.getFragment();
            fragment.setWidth(Component.FULL_SIZE);
            fragment.setHeight("250px");
            localizationTabComponent.add(fragment);
        }
    }

    protected void setupNumberFormat() {
        /* TODO dynamicAttributesGuiTools
        Datatype datatype = dynamicAttributesGuiTools.getCustomNumberDatatype(getItem());
        if (datatype != null) {
            defaultDecimal.setDatatype(datatype);
            minDecimal.setDatatype(datatype);
            maxDecimal.setDatatype(datatype);

            defaultDecimal.setValue(defaultDecimal.getValue());
            minDecimal.setValue(minDecimal.getValue());
            maxDecimal.setValue(maxDecimal.getValue());
        }*/
    }

    protected void refreshAttributesUI() {
        CategoryAttribute categoryAttribute = getEditedEntity();
        CategoryAttributeConfiguration configuration = categoryAttribute.getConfiguration();

        for (Component component : optionalAttributeForm.getOwnComponents()) {
            component.setVisible(false);
        }

        AttributeType attributeType = dataTypeField.getValue();
        Collection<String> visibleFields = FIELDS_VISIBLE_FOR_TYPES.get(attributeType);
        for (String componentId : visibleFields) {
            optionalAttributeForm.getComponentNN(componentId).setVisible(true);
        }
        if (MAIN_TAB_NAME.equals(tabSheet.getSelectedTab().getName()) && !visibleFields.isEmpty()) {
            setDialogWindowWidth(TWO_COLUMNS_WIDTH);
            optionalAttributeForm.setVisible(true);
        }

        if (ENTITY.equals(attributeType)) {
            if (!Strings.isNullOrEmpty(entityClassField.getValue())) {
                Class<?> javaClass = categoryAttribute.getJavaType();

                if (javaClass != null) {
                    defaultEntityIdField.setEditable(true);
                    defaultEntityIdField.setMetaClass(metadata.getClass(javaClass));
                    // todo: dynamic attributes (init picker field) and screensHelper
                    //dynamicAttributesGuiTools.initEntityPickerField(defaultEntityId, attribute);
                    //screenField.setOptionsMap(screensHelper.getAvailableBrowserScreens(entityClass));
                    refreshDefaultEntityIdFieldValue();
                }
            } else {
                defaultEntityIdField.setEditable(false);
            }
            screenField.setVisible(!lookupField.isChecked());
        }

        if (DATE.equals(attributeType)) {
            defaultDateField.setVisible(!Boolean.TRUE.equals(categoryAttribute.getDefaultDateIsCurrent()));
        }

        if (DATE_WITHOUT_TIME.equals(attributeType)) {
            defaultDateWithoutTimeField.setVisible(!Boolean.TRUE.equals(categoryAttribute.getDefaultDateIsCurrent()));
        }

        OptionsLoaderType optionsType = configuration.getOptionsLoaderType();

        boolean jpqlLoaderVisible = optionsType == JPQL;
        joinClauseField.setVisible(jpqlLoaderVisible);
        whereClauseField.setVisible(jpqlLoaderVisible);
        constraintWizardField.setVisible(jpqlLoaderVisible);

        boolean scriptLoaderVisible = optionsType == SQL
                || optionsType == GROOVY;
        optionsLoaderScriptField.setVisible(scriptLoaderVisible);

        if (optionsType == GROOVY) {
            optionsLoaderScriptField.setContextHelpIconClickHandler(e -> showMessageDialog(
                    messages.getMessage(CategoryAttrsEdit.class, "optionsLoaderGroovyScript"),
                    messages.getMessage(CategoryAttrsEdit.class, "optionsLoaderGroovyScriptHelp")));
            optionsLoaderScriptField.setMode(SourceCodeEditor.Mode.Groovy);
        } else if (optionsType == SQL) {
            optionsLoaderScriptField.setContextHelpIconClickHandler(e -> showMessageDialog(
                    messages.getMessage(CategoryAttrsEdit.class, "optionsLoaderSqlScript"),
                    messages.getMessage(CategoryAttrsEdit.class, "optionsLoaderSqlScriptHelp")));
            optionsLoaderScriptField.setMode(SourceCodeEditor.Mode.SQL);
        } else if (optionsType == JPQL) {
            joinClauseField.setContextHelpIconClickHandler(e -> showMessageDialog(
                    messages.getMessage(CategoryAttrsEdit.class, "joinClause"),
                    messages.getMessage(CategoryAttrsEdit.class, "joinClauseHelp")));
            whereClauseField.setContextHelpIconClickHandler(e -> showMessageDialog(
                    messages.getMessage(CategoryAttrsEdit.class, "whereClause"),
                    messages.getMessage(CategoryAttrsEdit.class, "whereClauseHelp")));
        } else {
            optionsLoaderScriptField.setContextHelpIconClickHandler(null);
            optionsLoaderScriptField.setMode(SourceCodeEditor.Mode.Text);
        }

        optionsLoaderTypeField.setEnabled(Boolean.TRUE.equals(categoryAttribute.getLookup()));
        optionsLoaderTypeField.setRequired(Boolean.TRUE.equals(categoryAttribute.getLookup()));
        optionsLoaderTypeField.setOptionsMap(getLoaderOptions());
    }

    protected void refreshAttributesValues() {
        AttributeType attributeType = dataTypeField.getValue();
        CategoryAttribute categoryAttribute = getEditedEntity();
        CategoryAttributeConfiguration configuration = categoryAttribute.getConfiguration();

        if (ENTITY.equals(attributeType)) {
            if (!Strings.isNullOrEmpty(categoryAttribute.getEntityClass())) {
                Map<String, String> options = ((MapOptions<String>) screenField.getOptions()).getItemsCollection();
                categoryAttribute.setScreen(options.containsValue(categoryAttribute.getScreen()) ? categoryAttribute.getScreen() : null);
            }
            if (configuration.getOptionsLoaderType() == SQL) {
                configuration.setOptionsLoaderType(JPQL);
            }
        } else if (configuration.getOptionsLoaderType() == JPQL) {
            configuration.setOptionsLoaderType(null);
        }

        if (DATE.equals(attributeType)) {
            if (Boolean.TRUE.equals(categoryAttribute.getDefaultDateIsCurrent())) {
                categoryAttribute.setDefaultDate(null);
            }
        }

        if (DATE_WITHOUT_TIME.equals(attributeType)) {
            if (Boolean.TRUE.equals(categoryAttribute.getDefaultDateIsCurrent())) {
                categoryAttribute.setDefaultDateWithoutTime(null);
            }
        }

        if (BOOLEAN.equals(attributeType)) {
            categoryAttribute.setIsCollection(null);
        }

        if (categoryAttribute.getDataType() == null
                || !SUPPORTED_OPTIONS_TYPES.contains(categoryAttribute.getDataType())) {
            categoryAttribute.setLookup(false);
        }

        if (!Boolean.TRUE.equals(categoryAttribute.getLookup())) {
            configuration.setOptionsLoaderType(null);
            configuration.setOptionsLoaderScript(null);
            categoryAttribute.setWhereClause(null);
            categoryAttribute.setJoinClause(null);
        } else {
            OptionsLoaderType optionsType = configuration.getOptionsLoaderType();
            if (optionsType == JPQL) {
                configuration.setOptionsLoaderScript(null);
            } else if (optionsType == GROOVY || optionsType == SQL) {
                categoryAttribute.setWhereClause(null);
                categoryAttribute.setJoinClause(null);
            } else if (optionsType == null) {
                configuration.setOptionsLoaderScript(null);
                categoryAttribute.setWhereClause(null);
                categoryAttribute.setJoinClause(null);
                if (categoryAttribute.getDataType() == ENTITY) {
                    configuration.setOptionsLoaderType(JPQL);
                }
            }
        }
    }

    protected void refreshDefaultEntityIdFieldValue() {
        CategoryAttribute attribute = getEditedEntity();
        Class<?> javaClass = getEditedEntity().getJavaType();
        if (javaClass != null) {
            MetaClass metaClass = metadata.getClass(javaClass);
            if (attribute.getObjectDefaultEntityId() != null) {
                LoadContext<Entity> lc = new LoadContext(attribute.getJavaType());
                FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(metaClass, FetchPlan.MINIMAL);
                lc.setFetchPlan(fetchPlan);
                String pkName = referenceToEntitySupport.getPrimaryKeyForLoadingEntity(metaClass);
                lc.setQueryString(format("select e from %s e where e.%s = :entityId", metaClass.getName(), pkName))
                        .setParameter("entityId", attribute.getObjectDefaultEntityId());
                Entity entity = dataManager.load(lc);
                if (entity != null) {
                    defaultEntityIdField.setValue(entity);
                } else {
                    defaultEntityIdField.setValue(null);
                }
            }
        }
    }

    protected void refreshCodeFieldValue() {
        CategoryAttribute attribute = getEditedEntity();
        if (Strings.isNullOrEmpty(attribute.getCode()) && !Strings.isNullOrEmpty(attribute.getName())) {
            String categoryName = StringUtils.EMPTY;
            if (attribute.getCategory() != null) {
                categoryName = StringUtils.defaultString(attribute.getCategory().getName());
            }
            codeField.setValue(StringUtils.deleteWhitespace(categoryName + attribute.getName()));
        }
    }

    protected Map<String, Boolean> getBooleanOptions() {
        Map<String, Boolean> booleanOptions = new TreeMap<>();
        booleanOptions.put(messages.getMessage("trueString"), Boolean.TRUE);
        booleanOptions.put(messages.getMessage("falseString"), Boolean.FALSE);
        return booleanOptions;
    }

    protected Map<String, AttributeType> getDataTypeOptions() {
        Map<String, AttributeType> options = new TreeMap<>();
        AttributeType[] types = AttributeType.values();
        for (AttributeType attributeType : types) {
            String key = AttributeType.class.getSimpleName() + "." + attributeType.toString();
            options.put(messages.getMessage(AttributeType.class, key), attributeType);
        }
        return options;
    }

    protected Map<String, String> getEntityOptions() {
        Map<String, String> optionsMap = new TreeMap<>();
        for (MetaClass metaClass : metadataTools.getAllPersistentMetaClasses()) {
            if (!metadataTools.isSystemLevel(metaClass)) {
                if (metadataTools.hasCompositePrimaryKey(metaClass)
                        && !HasUuid.class.isAssignableFrom(metaClass.getJavaClass())) {
                    continue;
                }
                optionsMap.put(messageTools.getDetailedEntityCaption(metaClass), metaClass.getJavaClass().getName());
            }
        }

        return optionsMap;
    }

    protected Map<String, OptionsLoaderType> getLoaderOptions() {
        CategoryAttribute attribute = getEditedEntity();
        Map<String, OptionsLoaderType> options = new TreeMap<>();
        for (OptionsLoaderType type : OptionsLoaderType.values()) {
            if (attribute.getDataType() != ENTITY && type == JPQL) {
                continue;
            }
            if (attribute.getDataType() == ENTITY && type == SQL) {
                continue;
            }
            String key = OptionsLoaderType.class.getSimpleName() + "." + type.toString();
            options.put(messages.getMessage(OptionsLoaderType.class, key), type);
        }
        return options;
    }

    protected List<CategoryAttribute> getAttributesOptions() {
        List<CategoryAttribute> optionsList = new ArrayList<>();
        CategoryAttribute attribute = getEditedEntity();
        List<CategoryAttribute> categoryAttributes = attribute.getCategory().getCategoryAttrs();
        if (categoryAttributes != null) {
            optionsList.addAll(categoryAttributes);
            optionsList.remove(attribute);
        }
        return optionsList;
    }

    protected void showMessageDialog(String caption, String message) {
        dialogs.createMessageDialog(Dialogs.MessageType.CONFIRMATION)
                .withCaption(caption)
                .withMessage(message)
                .withContentMode(ContentMode.HTML)
                .withModal(false)
                .withWidth(MESSAGE_DIALOG_WIDTH)
                .show();
    }

    protected List<Suggestion> requestHint(SourceCodeEditor sender, int senderCursorPosition) {
        String joinStr = joinClauseField.getValue();
        String whereStr = whereClauseField.getValue();

        // CAUTION: the magic entity name!  The length is three character to match "{E}" length in query
        String entityAlias = "a39";

        int queryPosition = -1;
        Class<?> javaClassForEntity = getEditedEntity().getJavaType();
        if (javaClassForEntity == null) {
            return new ArrayList<>();
        }

        String queryStart = format("select %s from %s %s ", entityAlias, metadata.getClass(javaClassForEntity), entityAlias);

        StringBuilder queryBuilder = new StringBuilder(queryStart);
        if (StringUtils.isNotEmpty(joinStr)) {
            if (sender == joinClauseField) {
                queryPosition = queryBuilder.length() + senderCursorPosition - 1;
            }
            if (!StringUtils.containsIgnoreCase(joinStr, "join") && !StringUtils.contains(joinStr, ",")) {
                queryBuilder.append("join ").append(joinStr);
                queryPosition += "join ".length();
            } else {
                queryBuilder.append(joinStr);
            }
        }
        if (StringUtils.isNotEmpty(whereStr)) {
            if (sender == whereClauseField) {
                queryPosition = queryBuilder.length() + WHERE.length() + senderCursorPosition;
            }
            queryBuilder.append(WHERE)
                    .append(" ")
                    .append(whereStr);
        }
        String query = queryBuilder.toString();
        query = query.replace("{E}", entityAlias);

        return JpqlSuggestionFactory.requestHint(query, queryPosition, sender.getAutoCompleteSupport(), senderCursorPosition);
    }

    protected void centerDialogWindow() {
        DialogWindow dialogWindow = (DialogWindow) getWindow();
        dialogWindow.center();
    }

    protected void setDialogWindowWidth(String width) {
        DialogWindow dialogWindow = (DialogWindow) getWindow();
        dialogWindow.setDialogWidth(width);
    }

    /* TODO validationEvent
    @Subscribe
    protected void onValidation(ValidationEvent event) {
        ValidationErrors validationErrors = new ValidationErrors();
        CategoryAttribute attribute = getEditedEntity();
        AttributeType dataType = attribute.getDataType();
        CategoryAttributeConfiguration configuration = attribute.getConfiguration();

        if (INTEGER.equals(dataType)) {
            ValidationErrors errors = validateNumbers(
                    INTEGER,
                    configuration.getMinInt(),
                    configuration.getMaxInt(),
                    attribute.getDefaultInt()
            );
            validationErrors.addAll(errors);
        } else if (DOUBLE.equals(dataType)) {
            ValidationErrors errors = validateNumbers(
                    DOUBLE,
                    configuration.getMinDouble(),
                    configuration.getMaxDouble(),
                    attribute.getDefaultDouble()
            );
            validationErrors.addAll(errors);
        } else if (DECIMAL.equals(dataType)) {
            ValidationErrors errors = validateNumbers(
                    DECIMAL,
                    configuration.getMinDecimal(),
                    configuration.getMaxDecimal(),
                    attribute.getDefaultDecimal()
            );
            validationErrors.addAll(errors);
        }

        CollectionDatasource<CategoryAttribute, UUID> parent
                = (CollectionDatasource<CategoryAttribute, UUID>) ((DatasourceImplementation) attributeDs).getParent();
        if (parent != null) {
            CategoryAttribute categoryAttribute = getItem();
            for (UUID id : parent.getItemIds()) {
                CategoryAttribute ca = parent.getItemNN(id);
                if (ca.getName().equals(categoryAttribute.getName())
                        && (!ca.equals(categoryAttribute))) {
                    errors.add(getMessage("uniqueName"));
                    return;
                } else if (ca.getCode() != null && ca.getCode().equals(categoryAttribute.getCode())
                        && (!ca.equals(categoryAttribute))) {
                    errors.add(getMessage("uniqueCode"));
                    return;
                }
            }
        }

        event.addErrors(validationErrors);
    }*/

    protected ValidationErrors validateNumbers(AttributeType type, Number minNumber, Number maxNumber, Number defaultNumber) {
        ValidationErrors validationErrors = new ValidationErrors();
        if (minNumber != null
                && maxNumber != null
                && compareNumbers(type, minNumber, maxNumber) > 0) {
            validationErrors.add(messages.getMessage("minGreaterThanMax"));
        } else if (defaultNumber != null) {
            if (minNumber != null
                    && compareNumbers(type, minNumber, defaultNumber) > 0) {
                validationErrors.add("defaultLessThanMin");
            }

            if (maxNumber != null
                    && compareNumbers(type, maxNumber, defaultNumber) < 0) {
                validationErrors.add("defaultGreaterThanMax");
            }
        }

        return validationErrors;
    }

    protected int compareNumbers(AttributeType type, Number first, Number second) {
        if (INTEGER.equals(type)) {
            return Integer.compare((Integer) first, (Integer) second);
        } else if (DOUBLE.equals(type)) {
            return Double.compare((Double) first, (Double) second);
        } else if (DECIMAL.equals(type)) {
            return ((BigDecimal) first).compareTo((BigDecimal) second);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Subscribe(target = Target.DATA_CONTEXT)
    protected void onPreCommit(DataContext.PreCommitEvent event) {
        preCommitLocalizationFields();
        preCommitTargetScreensField();
        preCommitConfiguration();
    }

    protected void preCommitLocalizationFields() {
        if (localizationFragment != null) {
            getEditedEntity().setLocaleNames(localizationFragment.getNameMsgBundle());
            getEditedEntity().setLocaleDescriptions(localizationFragment.getDescriptionMsgBundle());
        }
    }

    protected void preCommitTargetScreensField() {
        CategoryAttribute attribute = getEditedEntity();
        StringBuilder stringBuilder = new StringBuilder();
        for (ScreenAndComponent screenAndComponent : targetScreensDc.getItems()) {
            if (StringUtils.isNotBlank(screenAndComponent.getScreen())) {
                stringBuilder.append(screenAndComponent.getScreen());
                if (StringUtils.isNotBlank(screenAndComponent.getComponent())) {
                    stringBuilder.append("#");
                    stringBuilder.append(screenAndComponent.getComponent());
                }
                stringBuilder.append(",");
            }
        }

        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        attribute.setTargetScreens(stringBuilder.toString());
    }

    protected void preCommitConfiguration() {
        if (getScreenData().getDataContext().isModified(getEditedEntity().getCategory())) {
            getEditedEntity().setAttributeConfigurationJson(new Gson().toJson(configurationDc.getItemOrNull()));
        }
    }
}
