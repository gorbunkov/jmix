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
package com.haulmont.cuba.web;

import com.google.common.base.Strings;
import com.vaadin.server.*;
import com.vaadin.spring.annotation.VaadinSessionScope;
import com.vaadin.ui.UI;
import io.jmix.core.Messages;
import io.jmix.core.security.AnonymousUserCredentials;
import io.jmix.core.security.LoginException;
import io.jmix.core.security.UserSession;
import com.haulmont.cuba.core.global.UserSessionSource;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.Connection;
import io.jmix.ui.Notifications;
import io.jmix.ui.events.AppLoggedInEvent;
import io.jmix.ui.events.AppLoggedOutEvent;
import io.jmix.ui.events.AppStartedEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Locale;
import java.util.Objects;

//todo remove class
/**
 * Default {@link App} implementation that shows {@link AppLoginWindow} on start. Single instance of App is bound to
 * single HTTP session.
 */
//@Component(App.NAME)
@VaadinSessionScope
public class DefaultApp extends App {

    private static final Logger log = LoggerFactory.getLogger(DefaultApp.class);

    @Inject
    protected UserSessionSource userSessionSource;

    public DefaultApp() {
    }

    @Override
    protected Connection createConnection() {
        Connection connection = super.createConnection();
        connection.addStateChangeListener(this::connectionStateChanged);
        return connection;
    }

    protected void connectionStateChanged(Connection.StateChangeEvent event) {
        Connection connection = event.getSource();

        log.debug("connectionStateChanged connected: {}, authenticated: {}",
                connection.isConnected(), connection.isAuthenticated());

        cleanupBackgroundTasks();
        removeAllWindows();
        clearSettingsCache();

        if (connection.isConnected()) {
            UserSession userSession = connection.getSessionNN();
            setLocale(userSession.getLocale());

            // substitution listeners are cleared by connection on logout
            connection.addUserSubstitutionListener(this::userSubstituted);

            preventSessionFixation(connection, userSession);

            initExceptionHandlers(true);

            AppUI currentUi = AppUI.getCurrent();
            if (currentUi != null) {
                UserSession oldUserSession = currentUi.getUserSession();

                currentUi.setUserSession(connection.getSession());

                getAppUIs()
                        .stream()
                        .filter(ui ->
                                ui.hasAuthenticatedSession()
                                        && (Objects.equals(ui.getUserSession(), oldUserSession)
                                                || uiProperties.isForceRefreshAuthenticatedTabs()))
                        .forEach(ui -> ui.setUserSession(userSession));
            }

            if (connection.isAuthenticated()) {
                notifyMismatchedUIs(userSession);
            }

            initializeUi();

            // todo navigation
            /*if (linkHandler != null && linkHandler.canHandleLink()) {
                linkHandler.handle();
                linkHandler = null;
            }

            RedirectHandler redirectHandler = currentUi != null && currentUi.getUrlChangeHandler() != null
                    ? currentUi.getUrlChangeHandler().getRedirectHandler()
                    : null;

            if (redirectHandler != null && redirectHandler.scheduled()) {
                redirectHandler.redirect();
            }*/

            publishAppLoggedInEvent();
        } else {
            initExceptionHandlers(false);

            VaadinRequest currentRequest = VaadinService.getCurrentRequest();
            if (currentRequest != null) {
                Locale requestLocale = currentRequest.getLocale();
                setLocale(resolveLocale(requestLocale));
            }

            try {
                connection.login(new AnonymousUserCredentials(getLocale()));
            } catch (LoginException e) {
                throw new RuntimeException("Unable to login as anonymous!", e);
            }

            publishAppLoggedOutEvent(event.getPreviousSession());
        }
    }

    protected void notifyMismatchedUIs(UserSession userSession) {
        getAppUIs()
                .stream()
                .filter(ui -> ui.hasAuthenticatedSession()
                        && !Objects.equals(ui.getUserSession(), userSession))
                .forEach(this::notifyMismatchedSessionUi);
    }

    protected void notifyMismatchedSessionUi(AppUI ui) {
        Messages messages = beanLocator.get(Messages.class);

        String sessionChangedCaption = messages.getMessage("sessionChangedCaption");
        String sessionChanged = messages.getMessage("sessionChanged");

        ui.getNotifications()
                .create(Notifications.NotificationType.SYSTEM)
                .withCaption(sessionChangedCaption)
                .withDescription(sessionChanged)
                .withCloseListener((e) -> recreateUi(ui))
                .show();
    }

    protected void userSubstituted(Connection.UserSubstitutedEvent event) {
        cleanupBackgroundTasks();
        clearSettingsCache();
        removeAllWindows();

        updateUiUserSessions();

        initializeUi();
    }

    protected void updateUiUserSessions() {
        AppUI currentUi = AppUI.getCurrent();
        if (currentUi == null) {
            return;
        }

        UserSession oldUserSession = currentUi.getUserSession();
        UserSession appUserSession = getConnection().getSession();

        currentUi.setUserSession(appUserSession);

        for (AppUI ui : getAppUIs()) {
            if (Objects.equals(oldUserSession, ui.getUserSession())) {
                ui.setUserSession(appUserSession);
            }
        }
    }

    protected void publishAppLoggedOutEvent(UserSession previousSession) {
        AppLoggedOutEvent event = new AppLoggedOutEvent(this, previousSession);
        events.publish(event);

        String loggedOutUrl = event.getRedirectUrl();
        if (loggedOutUrl != null) {
            redirectAfterLogout(loggedOutUrl);
        }
    }

    protected void redirectAfterLogout(String loggedOutUrl) {
        if (!Strings.isNullOrEmpty(loggedOutUrl)) {
            AppUI currentUi = AppUI.getCurrent();
            // it can be null if we handle request in a custom RequestHandler
            if (currentUi != null) {
                currentUi.setContent(null);
                currentUi.getPage().setLocation(loggedOutUrl);
            } else {
                VaadinResponse response = VaadinService.getCurrentResponse();
                try {
                    ((VaadinServletResponse) response).getHttpServletResponse().
                            sendRedirect(loggedOutUrl);
                } catch (IOException e) {
                    log.error("Error on send redirect to client", e);
                }
            }

            VaadinSession vaadinSession = VaadinSession.getCurrent();
            for (UI ui : vaadinSession.getUIs()) {
                if (ui != currentUi) {
                    ui.access(() -> {
                        ui.setContent(null);
                        ui.getPage().setLocation(loggedOutUrl);
                    });
                }
            }
        }
    }

    protected void publishAppLoggedInEvent() {
        AppLoggedInEvent event = new AppLoggedInEvent(this);
        events.publish(event);
    }

    protected void initializeUi() {
        AppUI currentUi = AppUI.getCurrent();
        if (currentUi != null) {
            createTopLevelWindow(currentUi);
        }

        UserSession appUserSession = userSessionSource.getUserSession();

        for (AppUI ui : getAppUIs()) {
            if (currentUi != ui
                    && Objects.equals(appUserSession, ui.getUserSession())
                    || (ui.hasAuthenticatedSession()
                            && uiProperties.isForceRefreshAuthenticatedTabs())) {
                ui.accessSynchronously(() ->
                        createTopLevelWindow(ui));
            }
        }
    }

    protected void preventSessionFixation(Connection connection, UserSession userSession) {
        if (connection.isAuthenticated()
                // && !isLoggedInWithExternalAuth(userSession) todo external authentication
                && VaadinService.getCurrentRequest() != null) {

            VaadinService.reinitializeSession(VaadinService.getCurrentRequest());

            WrappedSession session = VaadinSession.getCurrent().getSession();
            int timeout = uiProperties.getHttpSessionExpirationTimeoutSec();
            session.setMaxInactiveInterval(timeout);

            HttpSession httpSession = session instanceof WrappedHttpSession ?
                    ((WrappedHttpSession) session).getHttpSession() : null;
            log.debug("Session reinitialized: HttpSession={}, timeout={}sec, UserSession={}",
                    httpSession, timeout, connection.getSession());
        }
    }

    @Override
    protected String routeTopLevelWindowId() {
        if (connection.isAuthenticated()) {
            return uiProperties.getMainScreenId();
        } else {
            String loginScreenId = uiProperties.getLoginScreenId();
            String initialScreenId = uiProperties.getInitialScreenId();

            if (StringUtils.isEmpty(initialScreenId)) {
                return loginScreenId;
            }

            if (!windowConfig.hasWindow(initialScreenId)) {
                log.info("No initial screen with id '{}' found", initialScreenId);
                return loginScreenId;
            }

            UserSession userSession = userSessionSource.getUserSession();
            // todo screen permissions
            /*if (!userSession.isScreenPermitted(initialScreenId)) {
                log.info("Initial screen '{}' is not permitted", initialScreenId);
                return loginScreenId;
            }*/

            return initialScreenId;
        }
    }

    @Override
    public void loginOnStart() throws LoginException {
        publishAppStartedEvent();

        if (!connection.isConnected()) {
            try {
                connection.login(new AnonymousUserCredentials(getLocale()));
            } catch (LoginException e) {
                throw new RuntimeException("Unable to login as anonymous!", e);
            }
        }
    }

    protected void publishAppStartedEvent() throws LoginException {
        try {
            events.publish(new AppStartedEvent(this));
        } catch (UndeclaredThrowableException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof LoginException) {
                throw (LoginException) cause;
            } else {
                throw ex;
            }
        }
    }
}