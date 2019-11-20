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
package io.jmix.ui.components.impl;

import io.jmix.core.commons.events.Subscription;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.ui.ComponentsHelper;
import io.jmix.ui.components.*;
import io.jmix.ui.widgets.CubaGroupBox;
import io.jmix.ui.widgets.CubaHorizontalActionsLayout;
import io.jmix.ui.widgets.CubaOrderedActionsLayout;
import io.jmix.ui.widgets.CubaVerticalActionsLayout;
import com.vaadin.event.ShortcutListener;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbstractOrderedLayout;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class WebGroupBox extends WebAbstractComponent<CubaGroupBox> implements GroupBoxLayout {

    private static final String GROUPBOX_PANEL_STYLENAME = "c-panel-groupbox";

    protected List<Component> ownComponents = new ArrayList<>();

    protected Orientation orientation = Orientation.VERTICAL;

    protected boolean settingsEnabled = true;
    protected boolean settingsChanged = false;

    protected Map<ShortcutAction, ShortcutListener> shortcuts;

    public WebGroupBox() {
        component = new CubaGroupBox();
        component.addStyleName(GROUPBOX_PANEL_STYLENAME);
        component.setExpandChangeHandler(this::fireExpandStateChange);

        CubaVerticalActionsLayout container = new CubaVerticalActionsLayout();
        container.setStyleName("c-groupbox-inner");
        component.setContent(container);
    }

    @Override
    public void add(Component childComponent) {
        add(childComponent, ownComponents.size());
    }

    @Override
    public void add(Component childComponent, int index) {
        if (childComponent.getParent() != null && childComponent.getParent() != this) {
            throw new IllegalStateException("Component already has parent");
        }

        AbstractOrderedLayout newContent = null;
        if (orientation == Orientation.VERTICAL && !(component.getContent() instanceof CubaVerticalActionsLayout)) {
            newContent = new CubaVerticalActionsLayout();
        } else if (orientation == Orientation.HORIZONTAL && !(component.getContent() instanceof CubaHorizontalActionsLayout)) {
            newContent = new CubaHorizontalActionsLayout();
        }

        if (newContent != null) {
            newContent.setStyleName("c-groupbox-inner");
            component.setContent(newContent);

            CubaOrderedActionsLayout currentContent = (CubaOrderedActionsLayout) component.getContent();

            newContent.setMargin(currentContent.getMargin());
            newContent.setSpacing(currentContent.isSpacing());
        }

        if (ownComponents.contains(childComponent)) {
            int existingIndex = getComponentContent().getComponentIndex(WebComponentsHelper.getComposition(childComponent));
            if (index > existingIndex) {
                index--;
            }

            remove(childComponent);
        }

        com.vaadin.ui.Component vComponent = WebComponentsHelper.getComposition(childComponent);
        getComponentContent().addComponent(vComponent, index);
        getComponentContent().setComponentAlignment(vComponent, WebWrapperUtils.toVaadinAlignment(childComponent.getAlignment()));

        if (frame != null) {
            if (childComponent instanceof BelongToFrame
                    && ((BelongToFrame) childComponent).getFrame() == null) {
                ((BelongToFrame) childComponent).setFrame(frame);
            } else {
                ((FrameImplementation) frame).registerComponent(childComponent);
            }
        }

        if (index == ownComponents.size()) {
            ownComponents.add(childComponent);
        } else {
            ownComponents.add(index, childComponent);
        }

        childComponent.setParent(this);
    }

    @Override
    public int indexOf(Component component) {
        return ownComponents.indexOf(component);
    }

    @Nullable
    @Override
    public Component getComponent(int index) {
        com.vaadin.ui.Component vComponent = getComponentContent().getComponent(index);

        for (Component ownComponent : ownComponents) {
            if (ownComponent.unwrapComposition(com.vaadin.ui.Component.class) == vComponent) {
                return ownComponent;
            }
        }

        return null;
    }

    @Override
    public void remove(Component childComponent) {
        getComponentContent().removeComponent(WebComponentsHelper.getComposition(childComponent));
        ownComponents.remove(childComponent);

        childComponent.setParent(null);
    }

    @Override
    public void removeAll() {
        getComponentContent().removeAllComponents();

        Component[] components = ownComponents.toArray(new Component[0]);
        ownComponents.clear();

        for (Component childComponent : components) {
            childComponent.setParent(null);
        }
    }

    @Override
    public void setFrame(Frame frame) {
        super.setFrame(frame);

        if (frame != null) {
            for (Component childComponent : ownComponents) {
                if (childComponent instanceof BelongToFrame
                        && ((BelongToFrame) childComponent).getFrame() == null) {
                    ((BelongToFrame) childComponent).setFrame(frame);
                }
            }
        }
    }

    protected AbstractOrderedLayout getComponentContent() {
        return ((AbstractOrderedLayout) component.getContent());
    }

    @Override
    public Component getOwnComponent(String id) {
        Preconditions.checkNotNullArgument(id);

        return ownComponents.stream()
                .filter(component -> Objects.equals(id, component.getId()))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    @Override
    public Component getComponent(String id) {
        return ComponentsHelper.getComponent(this, id);
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return Collections.unmodifiableCollection(ownComponents);
    }

    @Override
    public Stream<Component> getOwnComponentsStream() {
        return ownComponents.stream();
    }

    @Override
    public Collection<Component> getComponents() {
        return ComponentsHelper.getComponents(this);
    }

    @Override
    public void expand(Component component) {
        expand(component, "", "");
    }

    @Override
    public void resetExpanded() {
        for (com.vaadin.ui.Component child : getComponentContent()) {
            getComponentContent().setExpandRatio(child, 0.0f);
        }
    }

    @Override
    public void expand(Component component, String height, String width) {
        com.vaadin.ui.Component expandedComponent = WebComponentsHelper.getComposition(component);
        WebComponentsHelper.expand(getComponentContent(), expandedComponent, height, width);
    }

    @Override
    public boolean isExpanded(Component component) {
        return ownComponents.contains(component) && WebComponentsHelper.isComponentExpanded(component);
    }

    @Override
    public boolean isExpanded() {
        return component.isExpanded();
    }

    @Override
    public ExpandDirection getExpandDirection() {
        return orientation == Orientation.HORIZONTAL ? ExpandDirection.HORIZONTAL : ExpandDirection.VERTICAL;
    }

    @Override
    public void setExpanded(boolean expanded) {
        component.setExpanded(expanded);
    }

    @Override
    public boolean isCollapsable() {
        return component.isCollapsable();
    }

    @Override
    public void setCollapsable(boolean collapsable) {
        component.setCollapsable(collapsable);
    }

    @Override
    public Subscription addExpandedStateChangeListener(Consumer<ExpandedStateChangeEvent> listener) {
        return getEventHub().subscribe(ExpandedStateChangeEvent.class, listener);
    }

    @Override
    public void removeExpandedStateChangeListener(Consumer<ExpandedStateChangeEvent> listener) {
        unsubscribe(ExpandedStateChangeEvent.class, listener);
    }

    protected void fireExpandStateChange(boolean expanded, boolean invokedByUser) {
        ExpandedStateChangeEvent event = new ExpandedStateChangeEvent(this, expanded, invokedByUser);
        publish(ExpandedStateChangeEvent.class, event);

        if (invokedByUser) {
            settingsChanged = true;
        }
    }

    @Override
    public void applySettings(Element element) {
        if (isSettingsEnabled()) {
            Element groupBoxElement = element.element("groupBox");
            if (groupBoxElement != null) {
                String expanded = groupBoxElement.attributeValue("expanded");
                if (expanded != null) {
                    setExpanded(Boolean.parseBoolean(expanded));
                }
            }
        }
    }

    @Override
    public boolean saveSettings(Element element) {
        if (!isSettingsEnabled()) {
            return false;
        }

        if (!settingsChanged) {
            return false;
        }

        Element groupBoxElement = element.element("groupBox");
        if (groupBoxElement != null) {
            element.remove(groupBoxElement);
        }
        groupBoxElement = element.addElement("groupBox");
        groupBoxElement.addAttribute("expanded", BooleanUtils.toStringTrueFalse(isExpanded()));
        return true;
    }

    @Override
    public boolean isSettingsEnabled() {
        return settingsEnabled;
    }

    @Override
    public void setSettingsEnabled(boolean settingsEnabled) {
        this.settingsEnabled = settingsEnabled;
    }

    @Override
    public boolean isBorderVisible() {
        return true;
    }

    @Override
    public void setBorderVisible(boolean borderVisible) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSpacing(boolean enabled) {
        getComponentContent().setSpacing(enabled);
    }

    @Override
    public boolean getSpacing() {
        return getComponentContent().isSpacing();
    }

    @Override
    public Orientation getOrientation() {
        return orientation;
    }

    @Override
    public void setOrientation(Orientation orientation) {
        if (this.orientation != orientation) {
            if (!ownComponents.isEmpty()) {
                throw new IllegalStateException("Unable to change groupBox orientation after adding components to it");
            }

            this.orientation = orientation;
        }
    }

    @Override
    public void setShowAsPanel(boolean showAsPanel) {
        component.setShowAsPanel(showAsPanel);
    }

    @Override
    public boolean isShowAsPanel() {
        return component.isShowAsPanel();
    }

    @Override
    public void setStyleName(String name) {
        super.setStyleName(name);

        component.addStyleName(GROUPBOX_PANEL_STYLENAME);
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName().replace(GROUPBOX_PANEL_STYLENAME, ""));
    }

    @Override
    public void addShortcutAction(ShortcutAction action) {
        KeyCombination keyCombination = action.getShortcutCombination();
        com.vaadin.event.ShortcutListener shortcut =
                new ContainerShortcutActionWrapper(action, this, keyCombination);
        component.addShortcutListener(shortcut);

        if (shortcuts == null) {
            shortcuts = new HashMap<>(4);
        }
        shortcuts.put(action, shortcut);
    }

    @Override
    public void removeShortcutAction(ShortcutAction action) {
        if (shortcuts != null) {
            component.removeShortcutListener(shortcuts.remove(action));

            if (shortcuts.isEmpty()) {
                shortcuts = null;
            }
        }
    }

    @Override
    public void setOuterMargin(io.jmix.ui.components.MarginInfo marginInfo) {
        MarginInfo vMargin = new MarginInfo(marginInfo.hasTop(), marginInfo.hasRight(), marginInfo.hasBottom(),
                marginInfo.hasLeft());
        component.setOuterMargin(vMargin);
    }

    @Override
    public io.jmix.ui.components.MarginInfo getOuterMargin() {
        MarginInfo vMargin = component.getOuterMargin();
        return new io.jmix.ui.components.MarginInfo(vMargin.hasTop(), vMargin.hasRight(), vMargin.hasBottom(),
                vMargin.hasLeft());
    }

    @Override
    public void setExpandRatio(Component component, float ratio) {
        AbstractOrderedLayout layout = getComponentContent();
        layout.setExpandRatio(WebComponentsHelper.getComposition(component), ratio);
    }

    @Override
    public float getExpandRatio(Component component) {
        AbstractOrderedLayout layout = getComponentContent();
        return layout.getExpandRatio(component.unwrap(com.vaadin.ui.Component.class));
    }

    @Override
    public boolean isRequiredIndicatorVisible() {
        return component.isRequiredIndicatorVisible();
    }

    @Override
    public void setRequiredIndicatorVisible(boolean visible) {
        component.setRequiredIndicatorVisible(visible);
    }

    @Override
    public void attached() {
        super.attached();

        for (Component component : ownComponents) {
            ((AttachNotifier) component).attached();
        }
    }

    @Override
    public void detached() {
        super.detached();

        for (Component component : ownComponents) {
            ((AttachNotifier) component).detached();
        }
    }
}
