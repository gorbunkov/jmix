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

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import io.jmix.core.BeanLocator;
import io.jmix.ui.AppUI;
import io.jmix.ui.Screens;
import io.jmix.ui.Screens.OpenedScreens;
import io.jmix.ui.Screens.WindowStack;
import io.jmix.ui.UiComponents;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.*;
import io.jmix.ui.navigation.NavigationState;
import io.jmix.ui.navigation.UrlRouting;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.settings.UserSettingsTools;
import io.jmix.ui.util.OperationResult;
import io.jmix.ui.widget.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;
import static java.util.Collections.singletonList;

public class WebAppWorkArea extends WebAbstractComponent<CssLayout> implements AppWorkArea, HasInnerComponents {

    public static final String WORKAREA_STYLENAME = "c-app-workarea";

    public static final String MODE_TABBED_STYLENAME = "c-app-workarea-tabbed";
    public static final String MODE_SINGLE_STYLENAME = "c-app-workarea-single";

    public static final String STATE_INITIAL_STYLENAME = "c-app-workarea-initial";
    public static final String STATE_WINDOWS_STYLENAME = "c-app-workarea-windows";

    public static final String SINGLE_CONTAINER_STYLENAME = "c-main-singlewindow";
    public static final String TABBED_CONTAINER_STYLENAME = "c-main-tabsheet";

    public static final String INITIAL_LAYOUT_STYLENAME = "c-initial-layout";

    protected Mode mode = Mode.TABBED;
    protected State state = State.INITIAL_LAYOUT;

    protected VBoxLayout initialLayout;

    protected HasTabSheetBehaviour tabbedContainer;

    protected JmixSingleModeContainer singleContainer;

    protected boolean shortcutsInitialized = false;

    protected int urlStateCounter = 0;

    public WebAppWorkArea() {
        component = new CssLayout();
        component.setPrimaryStyleName(WORKAREA_STYLENAME);
        component.addStyleName(MODE_TABBED_STYLENAME);
        component.addStyleName(STATE_INITIAL_STYLENAME);
    }

    @Autowired
    public void setBeanLocator(BeanLocator beanLocator) {
        super.setBeanLocator(beanLocator);

        UiComponents cf = beanLocator.get(UiComponents.NAME);
        setInitialLayout(cf.create(VBoxLayout.NAME));

        this.tabbedContainer = createTabbedModeContainer();

        UserSettingsTools userSettingsTools = beanLocator.get(UserSettingsTools.NAME);
        setMode(userSettingsTools.loadAppWindowMode());
    }

    @Override
    public void setStyleName(String name) {
        super.setStyleName(name);

        if (mode == Mode.TABBED) {
            component.addStyleName(MODE_TABBED_STYLENAME);
        } else {
            component.addStyleName(MODE_SINGLE_STYLENAME);
        }

        if (state == State.INITIAL_LAYOUT) {
            component.addStyleName(STATE_INITIAL_STYLENAME);
        } else {
            component.addStyleName(STATE_WINDOWS_STYLENAME);
        }
    }

    @Override
    public String getStyleName() {
        return StringUtils.normalizeSpace(super.getStyleName()
                .replace(MODE_TABBED_STYLENAME, "")
                .replace(MODE_SINGLE_STYLENAME, "")
                .replace(STATE_INITIAL_STYLENAME, "")
                .replace(STATE_WINDOWS_STYLENAME, ""));
    }

    @Override
    public void setFrame(Frame frame) {
        super.setFrame(frame);

        initialLayout.setFrame(frame);
    }

    @Nonnull
    @Override
    public VBoxLayout getInitialLayout() {
        return initialLayout;
    }

    @Override
    public void setInitialLayout(VBoxLayout initialLayout) {
        checkNotNullArgument(initialLayout);

        if (state == State.WINDOW_CONTAINER) {
            throw new IllegalStateException("Unable to change AppWorkArea initial layout in WINDOW_CONTAINER state");
        }

        if (this.initialLayout != null) {
            component.removeComponent(this.initialLayout.unwrapComposition(Component.class));
        }

        this.initialLayout = initialLayout;

        initialLayout.setParent(this);
        initialLayout.setSizeFull();

        Component vInitialLayout = initialLayout.unwrapComposition(Component.class);
        vInitialLayout.addStyleName(INITIAL_LAYOUT_STYLENAME);
        component.addComponent(vInitialLayout);
    }

    @Override
    public void addStateChangeListener(Consumer<StateChangeEvent> listener) {
        getEventHub().subscribe(StateChangeEvent.class, listener);
    }

    @Override
    public void removeStateChangeListener(Consumer<StateChangeEvent> listener) {
        unsubscribe(StateChangeEvent.class, listener);
    }

    @Override
    public Mode getMode() {
        return mode;
    }

    @Override
    public void setMode(Mode mode) {
        if (state == State.WINDOW_CONTAINER) {
            throw new IllegalStateException("Unable to change AppWorkArea mode in WINDOW_CONTAINER state");
        }

        if (this.mode != mode) {
            if (mode == Mode.SINGLE) {
                tabbedContainer = null;

                singleContainer = createSingleModeContainer();
                component.addStyleName(MODE_SINGLE_STYLENAME);
                component.removeStyleName(MODE_TABBED_STYLENAME);
            } else {
                singleContainer = null;

                tabbedContainer = createTabbedModeContainer();
                component.removeStyleName(MODE_SINGLE_STYLENAME);
                component.addStyleName(MODE_TABBED_STYLENAME);
            }

            this.mode = mode;
        }
    }

    protected HasTabSheetBehaviour createTabbedModeContainer() {
        //if (webConfig.getMainTabSheetMode() == MainTabSheetMode.DEFAULT) {
            JmixMainTabSheet jmixTabSheet = new JmixMainTabSheet();

            tabbedContainer = jmixTabSheet;

            // todo dragdroplayouts
//            jmixTabSheet.setDragMode(LayoutDragMode.CLONE);
//            jmixTabSheet.setDropHandler(new TabSheetReorderingDropHandler());
            Action.Handler actionHandler = createTabSheetActionHandler(jmixTabSheet);
            jmixTabSheet.addActionHandler(actionHandler);

            jmixTabSheet.setCloseOthersHandler(this::closeOtherTabWindows);
            jmixTabSheet.setCloseAllTabsHandler(this::closeAllTabWindows);
            jmixTabSheet.addSelectedTabChangeListener(event -> reflectTabChangeToUrl(event.isUserOriginated()));
        // } else {
            // todo managed tabsheet
            /*JmixManagedTabSheet cubaManagedTabSheet = new JmixManagedTabSheet();

            ManagedMainTabSheetMode tabSheetMode = configuration.getConfig(WebConfig.class)
                    .getManagedMainTabSheetMode();
            cubaManagedTabSheet.setMode(JmixManagedTabSheet.Mode.valueOf(tabSheetMode.name()));

            tabbedContainer = cubaManagedTabSheet;

            cubaManagedTabSheet.setDragMode(LayoutDragMode.CLONE);
            cubaManagedTabSheet.setDropHandler(new TabSheetReorderingDropHandler());
            Action.Handler actionHandler = createTabSheetActionHandler(cubaManagedTabSheet);
            cubaManagedTabSheet.addActionHandler(actionHandler);

            cubaManagedTabSheet.setCloseOthersHandler(this::closeOtherTabWindows);
            cubaManagedTabSheet.setCloseAllTabsHandler(this::closeAllTabWindows);
            cubaManagedTabSheet.addSelectedTabChangeListener(event -> reflectTabChangeToUrl(event.isUserOriginated()));*/
        // }

        tabbedContainer.setHeight(100, Sizeable.Unit.PERCENTAGE);
        tabbedContainer.setStyleName(TABBED_CONTAINER_STYLENAME);
        tabbedContainer.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabbedContainer.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);

        return tabbedContainer;
    }

    protected void reflectTabChangeToUrl(boolean userOriginated) {
        if (!userOriginated) {
            return;
        }

        Component selectedTab = tabbedContainer.getTabSheetBehaviour().getSelectedTab();
        if (selectedTab == null) {
            return;
        }

        Window selectedWindow = ((TabWindowContainer) selectedTab).getBreadCrumbs().getCurrentWindow();
        WebWindow webWindow = (WebWindow) selectedWindow;

        NavigationState resolvedState = webWindow.getResolvedState();
        if (resolvedState != null) {
            int stateMark = generateUrlStateMark();

            NavigationState newState = new NavigationState(
                    resolvedState.getRoot(),
                    String.valueOf(stateMark),
                    resolvedState.getNestedRoute(),
                    resolvedState.getParams());
            webWindow.setResolvedState(newState);

            Screen screen = selectedWindow.getFrameOwner();

            UrlRouting urlRouting = UiControllerUtils.getScreenContext(screen)
                    .getUrlRouting();

            urlRouting.pushState(screen, newState.getParams());
        }
    }

    protected Action.Handler createTabSheetActionHandler(HasTabSheetBehaviour tabSheet) {
        return new MainTabSheetActionHandler(tabSheet);
    }

    protected JmixSingleModeContainer createSingleModeContainer() {
        JmixSingleModeContainer boxLayout = new JmixSingleModeContainer();
        boxLayout.setHeight("100%");
        boxLayout.setStyleName(SINGLE_CONTAINER_STYLENAME);
        return boxLayout;
    }

    public HasTabSheetBehaviour getTabbedWindowContainer() {
        return tabbedContainer;
    }

    public JmixSingleModeContainer getSingleWindowContainer() {
        return singleContainer;
    }

    /**
     * Used by {@link Screens}.
     *
     * @param state new state
     */
    @Override
    public void switchTo(State state) {
        if (this.state != state) {
            component.getUI().focus();
            component.removeAllComponents();

            if (state == State.WINDOW_CONTAINER) {
                if (mode == Mode.SINGLE) {
                    component.addComponent(singleContainer);
                } else {
                    component.addComponent(tabbedContainer);
                }
                component.addStyleName(STATE_WINDOWS_STYLENAME);
                component.removeStyleName(STATE_INITIAL_STYLENAME);
            } else {
                component.addComponent(initialLayout.unwrapComposition(Component.class));
                component.removeStyleName(STATE_WINDOWS_STYLENAME);
                component.addStyleName(STATE_INITIAL_STYLENAME);
            }

            this.state = state;

            // init global tab shortcuts
            if (!this.shortcutsInitialized
                    && getState() == State.WINDOW_CONTAINER) {
                initTabShortcuts();

                this.shortcutsInitialized = true;
            }

            if (hasSubscriptions(StateChangeEvent.class)) {
                publish(StateChangeEvent.class, new StateChangeEvent(this, state));
            }
        }
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public Collection<io.jmix.ui.component.Component> getInnerComponents() {
        if (state == State.INITIAL_LAYOUT) {
            return singletonList(getInitialLayout());
        }
        return Collections.emptyList();
    }

    public int getOpenedTabCount() {
        if (getMode() == Mode.TABBED) {
            TabSheetBehaviour tabSheetBehaviour = getTabbedWindowContainer().getTabSheetBehaviour();

            return tabSheetBehaviour.getComponentCount();
        } else {
            JmixSingleModeContainer singleWindowContainer = getSingleWindowContainer();
            TabWindowContainer windowContainer = (TabWindowContainer) singleWindowContainer.getWindowContainer();
            return windowContainer != null ? 1 : 0;
        }
    }

    public Stream<Screen> getOpenedWorkAreaScreensStream() {
        if (getMode() == Mode.TABBED) {
            TabSheetBehaviour tabSheetBehaviour = getTabbedWindowContainer().getTabSheetBehaviour();

            return tabSheetBehaviour.getTabComponentsStream()
                    .flatMap(c -> {
                        TabWindowContainer windowContainer = (TabWindowContainer) c;

                        Deque<Window> windows = windowContainer.getBreadCrumbs().getWindows();

                        return windows.stream()
                                .map(Window::getFrameOwner);
                    });
        } else {
            JmixSingleModeContainer singleWindowContainer = getSingleWindowContainer();
            TabWindowContainer windowContainer = (TabWindowContainer) singleWindowContainer.getWindowContainer();

            if (windowContainer != null) {
                Deque<Window> windows = windowContainer.getBreadCrumbs().getWindows();

                return windows.stream()
                        .map(Window::getFrameOwner);
            }
        }

        return Stream.empty();
    }

    public Stream<Screen> getActiveWorkAreaScreensStream() {
        if (getMode() == Mode.TABBED) {
            TabSheetBehaviour tabSheetBehaviour = getTabbedWindowContainer().getTabSheetBehaviour();

            return tabSheetBehaviour.getTabComponentsStream()
                    .map(c -> {
                        TabWindowContainer windowContainer = (TabWindowContainer) c;

                        Window currentWindow = windowContainer.getBreadCrumbs().getCurrentWindow();

                        return currentWindow.getFrameOwner();
                    });
        } else {
            JmixSingleModeContainer singleWindowContainer = getSingleWindowContainer();
            TabWindowContainer windowContainer = (TabWindowContainer) singleWindowContainer.getWindowContainer();

            if (windowContainer != null) {
                Window currentWindow = windowContainer.getBreadCrumbs().getCurrentWindow();

                return Stream.of(currentWindow.getFrameOwner());
            }
        }

        return Stream.empty();
    }

    public Collection<Screen> getCurrentBreadcrumbs() {
        TabWindowContainer layout = getCurrentWindowContainer();

        if (layout != null) {
            WindowBreadCrumbs breadCrumbs = layout.getBreadCrumbs();

            List<Screen> screens = new ArrayList<>(breadCrumbs.getWindows().size());
            Iterator<Window> windowIterator = breadCrumbs.getWindows().descendingIterator();
            while (windowIterator.hasNext()) {
                Screen frameOwner = windowIterator.next().getFrameOwner();
                screens.add(frameOwner);
            }

            return screens;
        }

        return Collections.emptyList();
    }

    protected boolean isWindowClosePrevented(Window window, Window.CloseOrigin closeOrigin) {
        Window.BeforeCloseEvent event = new Window.BeforeCloseEvent(window, closeOrigin);
        ((WebWindow) window).fireBeforeClose(event);

        return event.isClosePrevented();
    }

    protected void closeAllTabWindows(ComponentContainer container) {
        AppUI ui = (AppUI) component.getUI();

        OpenedScreens openedScreens = ui.getScreens().getOpenedScreens();
        for (WindowStack windowStack : openedScreens.getWorkAreaStacks()) {
            boolean closed = closeWindowStack(windowStack);

            if (!closed) {
                break;
            }
        }
    }

    protected void closeOtherTabWindows(ComponentContainer container) {
        AppUI ui = (AppUI) component.getUI();

        OpenedScreens openedScreens = ui.getScreens().getOpenedScreens();
        for (WindowStack windowStack : openedScreens.getWorkAreaStacks()) {
            if (!windowStack.isSelected()) {
                boolean closed = closeWindowStack(windowStack);

                if (!closed) {
                    break;
                }
            }
        }
    }

    protected boolean closeWindowStack(WindowStack windowStack) {
        boolean closed = true;

        Collection<Screen> tabScreens = windowStack.getBreadcrumbs();

        for (Screen screen : tabScreens) {
            if (isNotCloseable(screen.getWindow())) {
                continue;
            }

            if (isWindowClosePrevented(screen.getWindow(), CloseOriginType.CLOSE_BUTTON)) {
                closed = false;

                // focus tab
                windowStack.select();

                break;
            }

            OperationResult closeResult = screen.close(FrameOwner.WINDOW_CLOSE_ACTION);
            if (closeResult.getStatus() != OperationResult.Status.SUCCESS) {
                closed = false;

                // focus tab
                windowStack.select();

                break;
            }
        }
        return closed;
    }

    protected void initTabShortcuts() {
        Screen rootScreen = ComponentsHelper.getWindowNN(this).getFrameOwner();

        RootWindow topLevelWindow = (RootWindow) rootScreen.getWindow();
        topLevelWindow.withUnwrapped(JmixOrderedActionsLayout.class, actionsLayout -> {
            if (getMode() == Mode.TABBED) {
                actionsLayout.addShortcutListener(createNextWindowTabShortcut(topLevelWindow));
                actionsLayout.addShortcutListener(createPreviousWindowTabShortcut(topLevelWindow));
            }
            actionsLayout.addShortcutListener(createCloseShortcut(topLevelWindow));
        });
    }

    protected ShortcutListener createCloseShortcut(RootWindow topLevelWindow) {
        UiProperties properties = beanLocator.get(UiProperties.class);
        String closeShortcut = properties.getCloseShortcut();
        KeyCombination combination = KeyCombination.create(closeShortcut);

        return new ShortcutListenerDelegate("onClose", combination.getKey().getCode(),
                KeyCombination.Modifier.codes(combination.getModifiers()))
                .withHandler((sender, target) ->
                        closeWindowByShortcut(topLevelWindow)
                );
    }

    protected ShortcutListener createNextWindowTabShortcut(RootWindow topLevelWindow) {
        String nextTabShortcut = beanLocator.get(UiProperties.class).getNextTabShortcut();
        KeyCombination combination = KeyCombination.create(nextTabShortcut);

        return new ShortcutListenerDelegate(
                "onNextTab", combination.getKey().getCode(),
                KeyCombination.Modifier.codes(combination.getModifiers())
        ).withHandler((sender, target) -> {
            TabSheetBehaviour tabSheet = getTabbedWindowContainer().getTabSheetBehaviour();

            if (tabSheet != null
                    && !hasModalWindow()
                    && tabSheet.getComponentCount() > 1) {
                Component selectedTabComponent = tabSheet.getSelectedTab();
                String tabId = tabSheet.getTab(selectedTabComponent);
                int tabPosition = tabSheet.getTabPosition(tabId);
                int newTabPosition = (tabPosition + 1) % tabSheet.getComponentCount();

                String newTabId = tabSheet.getTab(newTabPosition);
                tabSheet.setSelectedTab(newTabId);

                moveFocus(tabSheet, newTabId);
            }
        });
    }

    protected ShortcutListener createPreviousWindowTabShortcut(RootWindow topLevelWindow) {
        String previousTabShortcut = beanLocator.get(UiProperties.class).getPreviousTabShortcut();
        KeyCombination combination = KeyCombination.create(previousTabShortcut);

        return new ShortcutListenerDelegate("onPreviousTab", combination.getKey().getCode(),
                KeyCombination.Modifier.codes(combination.getModifiers())
        ).withHandler((sender, target) -> {
            TabSheetBehaviour tabSheet = getTabbedWindowContainer().getTabSheetBehaviour();

            if (tabSheet != null
                    && !hasModalWindow()
                    && tabSheet.getComponentCount() > 1) {
                Component selectedTabComponent = tabSheet.getSelectedTab();
                String selectedTabId = tabSheet.getTab(selectedTabComponent);
                int tabPosition = tabSheet.getTabPosition(selectedTabId);
                int newTabPosition = (tabSheet.getComponentCount() + tabPosition - 1) % tabSheet.getComponentCount();

                String newTabId = tabSheet.getTab(newTabPosition);
                tabSheet.setSelectedTab(newTabId);

                moveFocus(tabSheet, newTabId);
            }
        });
    }

    protected void closeWindowByShortcut(RootWindow topLevelWindow) {
        if (getState() != AppWorkArea.State.WINDOW_CONTAINER) {
            return;
        }

        AppUI ui = (AppUI) this.getComponent().getUI();
        if (!ui.isAccessibleForUser(this.getComponent())) {
            LoggerFactory.getLogger(WebAppWorkArea.class)
                    .debug("Ignore close shortcut attempt because workArea is inaccessible for user");
            return;
        }

        if (getMode() == Mode.TABBED) {
            TabSheetBehaviour tabSheet = getTabbedWindowContainer().getTabSheetBehaviour();
            if (tabSheet != null) {
                TabWindowContainer layout = (TabWindowContainer) tabSheet.getSelectedTab();
                if (layout != null) {
                    tabSheet.focus();

                    WindowBreadCrumbs breadCrumbs = layout.getBreadCrumbs();

                    Window currentWindow = breadCrumbs.getCurrentWindow();

                    if (isNotCloseable(currentWindow)) {
                        return;
                    }

                    if (isWindowClosePrevented(currentWindow, CloseOriginType.SHORTCUT)) {
                        return;
                    }

                    if (breadCrumbs.getWindows().isEmpty()) {
                        Component previousTab = tabSheet.getPreviousTab(layout);
                        if (previousTab != null) {
                            currentWindow.getFrameOwner()
                                    .close(FrameOwner.WINDOW_CLOSE_ACTION)
                                    .then(() -> tabSheet.setSelectedTab(previousTab));
                        } else {
                            currentWindow.getFrameOwner()
                                    .close(FrameOwner.WINDOW_CLOSE_ACTION);
                        }
                    } else {
                        currentWindow.getFrameOwner()
                                .close(FrameOwner.WINDOW_CLOSE_ACTION);
                    }
                }
            }
        } else {
            Iterator<WindowBreadCrumbs> it = getWindowStacks().iterator();
            if (it.hasNext()) {
                Window currentWindow = it.next().getCurrentWindow();
                if (!isWindowClosePrevented(currentWindow, CloseOriginType.SHORTCUT)) {
                    ui.focus();

                    currentWindow.getFrameOwner()
                            .close(FrameOwner.WINDOW_CLOSE_ACTION);
                }
            }
        }
    }

    protected List<WindowBreadCrumbs> getWindowStacks() {
        if (getMode() == Mode.TABBED) {
            TabSheetBehaviour tabSheet = getTabbedWindowContainer().getTabSheetBehaviour();

            List<WindowBreadCrumbs> allBreadCrumbs = new ArrayList<>();
            for (int i = 0; i < tabSheet.getComponentCount(); i++) {
                String tabId = tabSheet.getTab(i);

                TabWindowContainer tabComponent = (TabWindowContainer) tabSheet.getTabComponent(tabId);
                allBreadCrumbs.add(tabComponent.getBreadCrumbs());
            }
            return allBreadCrumbs;
        } else {
            TabWindowContainer windowContainer = (TabWindowContainer) getSingleWindowContainer().getWindowContainer();

            if (windowContainer == null) {
                return Collections.emptyList();
            }

            return singletonList(windowContainer.getBreadCrumbs());
        }
    }

    protected void moveFocus(TabSheetBehaviour tabSheet, String tabId) {
        TabWindowContainer windowContainer = (TabWindowContainer) tabSheet.getTabComponent(tabId);
        Window window = windowContainer.getBreadCrumbs().getCurrentWindow();

        if (window != null) {
            boolean focused = false;
            String focusComponentId = window.getFocusComponent();
            if (focusComponentId != null) {
                io.jmix.ui.component.Component focusComponent = window.getComponent(focusComponentId);
                if (focusComponent instanceof io.jmix.ui.component.Component.Focusable
                        && focusComponent.isEnabledRecursive()
                        && focusComponent.isVisibleRecursive()) {
                    ((io.jmix.ui.component.Component.Focusable) focusComponent).focus();
                    focused = true;
                }
            }

            if (!focused) {
                tabSheet.focus();
            }
        }
    }

    public boolean isNotCloseable(Window window) {
        if (!window.isCloseable()) {
            return true;
        }

        if (beanLocator.get(UiProperties.class).isDefaultScreenCanBeClosed()) {
            return false;
        }

        return ((WebWindow) window).isDefaultScreenWindow();
    }

    protected boolean hasModalWindow() {
        UI ui = getComponent().getUI();
        return ui.getWindows().stream()
                .anyMatch(com.vaadin.ui.Window::isModal);
    }

    @Nullable
    protected TabWindowContainer getCurrentWindowContainer() {
        TabWindowContainer layout;
        if (getMode() == Mode.TABBED) {
            TabSheetBehaviour tabSheetBehaviour = getTabbedWindowContainer().getTabSheetBehaviour();

            layout = (TabWindowContainer) tabSheetBehaviour.getSelectedTab();
        } else {
            JmixSingleModeContainer singleWindowContainer = getSingleWindowContainer();

            layout = (TabWindowContainer) singleWindowContainer.getWindowContainer();
        }
        return layout;
    }

    public int generateUrlStateMark() {
        return urlStateCounter++;
    }

    // todo dragdroplayouts
    // Allows Tabs reordering, do not support component / text drop to Tabs panel
    /*public static class TabSheetReorderingDropHandler extends DefaultTabSheetDropHandler {
        @Override
        protected void handleDropFromAbsoluteParentLayout(DragAndDropEvent event) {
            // do nothing
        }

        @Override
        protected void handleDropFromLayout(DragAndDropEvent event) {
            // do nothing
        }

        @Override
        protected void handleHTML5Drop(DragAndDropEvent event) {
            // do nothing
        }
    }*/
}