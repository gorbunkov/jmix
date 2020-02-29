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

package com.haulmont.cuba.web.app.main;

import com.haulmont.cuba.core.global.Messages;
import com.vaadin.server.WebBrowser;
import io.jmix.core.ConfigInterfaces;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.ScreenTools;
import io.jmix.ui.Screens;
import io.jmix.ui.WebConfig;
import io.jmix.ui.components.AppWorkArea;
import io.jmix.ui.components.Button;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.CssLayout;
import io.jmix.ui.components.Image;
import io.jmix.ui.components.ThemeResource;
import io.jmix.ui.components.Window;
import io.jmix.ui.components.dev.LayoutAnalyzerContextMenuProvider;
import io.jmix.ui.components.mainwindow.AppMenu;
import io.jmix.ui.components.mainwindow.SideMenu;
import io.jmix.ui.components.mainwindow.UserIndicator;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.OpenMode;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiControllerUtils;
import io.jmix.ui.screen.UiDescriptor;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

import static io.jmix.ui.components.ComponentsHelper.setStyleName;

/**
 * Base class for a controller of application Main screen.
 */
@Route(path = "main", root = true)
@UiDescriptor("main-screen.xml")
@UiController("main")
public class MainScreen extends Screen implements Window.HasWorkArea, Window.HasUserIndicator {

    public static final String SIDEMENU_COLLAPSED_STATE = "sidemenuCollapsed";
    public static final String SIDEMENU_COLLAPSED_STYLENAME = "collapsed";

    protected static final String APP_LOGO_IMAGE = "application.logoImage";

    public MainScreen() {
        addInitListener(this::initComponents);
    }

    protected void initComponents(@SuppressWarnings("unused") InitEvent e) {
        initLogoImage();
        initFtsField();
        initUserIndicator();
        initTitleBar();
        initMenu();
        initLayoutAnalyzerContextMenu();
    }

    protected void initUserIndicator() {
        UserIndicator userIndicator = getUserIndicator();
        if (userIndicator != null) {
            boolean authenticated = AppUI.getCurrent().hasAuthenticatedSession();
            userIndicator.setVisible(authenticated);
        }
    }

    protected void initLogoImage() {
        Image logoImage = getLogoImage();
        String logoImagePath = getBeanLocator().get(Messages.class)
                .getMainMessage(APP_LOGO_IMAGE);

        if (logoImage != null
                && StringUtils.isNotBlank(logoImagePath)
                && !APP_LOGO_IMAGE.equals(logoImagePath)) {
            logoImage.setSource(ThemeResource.class).setPath(logoImagePath);
        }
    }

    protected void initFtsField() {
        // TODO fts
        /*FtsField ftsField = getFtsField();
        if (ftsField != null && !FtsConfigHelper.getEnabled()) {
            ftsField.setVisible(false);
        }*/
    }

    protected void initLayoutAnalyzerContextMenu() {
        Image logoImage = getLogoImage();
        if (logoImage != null) {
            LayoutAnalyzerContextMenuProvider laContextMenuProvider =
                    getBeanLocator().get(LayoutAnalyzerContextMenuProvider.NAME);
            laContextMenuProvider.initContextMenu(this, logoImage);
        }
    }

    protected void initMenu() {
        Component menu = getAppMenu();
        if (menu == null) {
            menu = getSideMenu();
        }

        if (menu != null) {
            ((Component.Focusable) menu).focus();
        }

        initCollapsibleMenu();
    }

    protected void initCollapsibleMenu() {
        Component sideMenuContainer = getWindow().getComponent("sideMenuContainer");
        if (sideMenuContainer instanceof CssLayout) {
            if (isMobileDevice()) {
                setSideMenuCollapsed(true);
            } else {
                String menuCollapsedCookie = App.getInstance()
                        .getCookieValue(SIDEMENU_COLLAPSED_STATE);

                boolean menuCollapsed = Boolean.parseBoolean(menuCollapsedCookie);

                setSideMenuCollapsed(menuCollapsed);
            }

            initCollapseMenuControls();
        }
    }

    protected void initCollapseMenuControls() {
        Button collapseMenuButton = getCollapseMenuButton();
        if (collapseMenuButton != null) {
            collapseMenuButton.addClickListener(event ->
                    setSideMenuCollapsed(!isMenuCollapsed()));
        }

        Button settingsButton = getSettingsButton();
        if (settingsButton != null) {
            settingsButton.addClickListener(event ->
                    openSettingsScreen());
        }

        Button loginButton = getLoginButton();
        if (loginButton != null) {
            loginButton.addClickListener(event ->
                    openLoginScreen());
        }
    }

    protected void initTitleBar() {
        ConfigInterfaces configuration = getBeanLocator().get(ConfigInterfaces.class);
        if (configuration.getConfig(WebConfig.class).getUseInverseHeader()) {
            Component titleBar = getTitleBar();
            if (titleBar != null) {
                titleBar.setStyleName("c-app-menubar c-inverse-header");
            }
        }
    }

    @Subscribe
    protected void onAfterShow(AfterShowEvent event) {
        Screens screens = UiControllerUtils.getScreenContext(this)
                .getScreens();
        getBeanLocator().get(ScreenTools.class)
                .openDefaultScreen(screens);
    }

    @Nullable
    @Override
    public AppWorkArea getWorkArea() {
        return (AppWorkArea) getWindow().getComponent("workArea");
    }

    @Nullable
    @Override
    public UserIndicator getUserIndicator() {
        return (UserIndicator) getWindow().getComponent("userIndicator");
    }

    @Nullable
    protected Button getCollapseMenuButton() {
        return (Button) getWindow().getComponent("collapseMenuButton");
    }

    @Nullable
    protected Button getSettingsButton() {
        return (Button) getWindow().getComponent("settingsButton");
    }

    @Nullable
    protected Button getLoginButton() {
        return (Button) getWindow().getComponent("loginButton");
    }

    @Nullable
    protected Image getLogoImage() {
        return (Image) getWindow().getComponent("logoImage");
    }

    // TODO fts
    /*@Nullable
    protected FtsField getFtsField() {
        return (FtsField) getWindow().getComponent("ftsField");
    }*/

    @Nullable
    protected AppMenu getAppMenu() {
        return (AppMenu) getWindow().getComponent("appMenu");
    }

    @Nullable
    protected SideMenu getSideMenu() {
        return (SideMenu) getWindow().getComponent("sideMenu");
    }

    @Nullable
    protected Component getTitleBar() {
        return getWindow().getComponent("titleBar");
    }

    protected void openLoginScreen() {
        String loginScreenId = getBeanLocator().get(ConfigInterfaces.class)
                .getConfig(WebConfig.class)
                .getLoginScreenId();

        UiControllerUtils.getScreenContext(this)
                .getScreens()
                .create(loginScreenId, OpenMode.ROOT)
                .show();
    }

    protected void openSettingsScreen() {
        UiControllerUtils.getScreenContext(this)
                .getScreens()
                .create("settings", OpenMode.NEW_TAB)
                .show();
    }

    protected void setSideMenuCollapsed(boolean collapsed) {
        Component sideMenuContainer = getWindow().getComponent("sideMenuContainer");
        CssLayout sideMenuPanel = (CssLayout) getWindow().getComponent("sideMenuPanel");
        Button collapseMenuButton = getCollapseMenuButton();

        setStyleName(sideMenuContainer, SIDEMENU_COLLAPSED_STYLENAME, collapsed);
        setStyleName(sideMenuPanel, SIDEMENU_COLLAPSED_STYLENAME, collapsed);

        if (collapseMenuButton != null) {
            Messages messages = getBeanLocator().get(Messages.class);
            if (collapsed) {
                collapseMenuButton.setCaption(messages.getMainMessage("menuExpandGlyph"));
                collapseMenuButton.setDescription(messages.getMainMessage("sideMenuExpand"));
            } else {
                collapseMenuButton.setCaption(messages.getMainMessage("menuCollapseGlyph"));
                collapseMenuButton.setDescription(messages.getMainMessage("sideMenuCollapse"));
            }
        }

        App.getInstance()
                .addCookie(SIDEMENU_COLLAPSED_STATE, String.valueOf(collapsed));
    }

    protected boolean isMenuCollapsed() {
        CssLayout sideMenuPanel = (CssLayout) getWindow().getComponent("sideMenuPanel");
        return sideMenuPanel != null
                && sideMenuPanel.getStyleName() != null
                && sideMenuPanel.getStyleName().contains(SIDEMENU_COLLAPSED_STYLENAME);
    }

    protected boolean isMobileDevice() {
        WebBrowser browser = AppUI.getCurrent()
                .getPage()
                .getWebBrowser();

        return browser.getScreenWidth() < 500
                || browser.getScreenHeight() < 800;
    }
}