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

package io.jmix.ui.components.mainwindow.impl;

import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.BaseUser;
import io.jmix.ui.components.impl.WebAbstractComponent;
import io.jmix.ui.components.mainwindow.UserIndicator;
import io.jmix.ui.widgets.CubaComboBox;

import javax.inject.Inject;
import java.util.function.Function;

import static com.vaadin.server.Sizeable.Unit;

public class WebUserIndicator extends WebAbstractComponent<CssLayout> implements UserIndicator {

    protected static final String USER_INDICATOR_STYLENAME = "c-userindicator";

//    protected final Function<? super BaseUser, String> DEFAULT_USER_NAME_FORMATTER = this::getDefaultUserCaption;

    protected Label userNameLabel;
    protected CubaComboBox<BaseUser> userComboBox;

//    protected Function<? super BaseUser, String> userNameFormatter = DEFAULT_USER_NAME_FORMATTER;

    protected MetadataTools metadataTools;

    public WebUserIndicator() {
        component = new com.vaadin.ui.CssLayout();
        component.setPrimaryStyleName(USER_INDICATOR_STYLENAME);
    }

    @Inject
    public void setMetadataTools(MetadataTools metadataTools) {
        this.metadataTools = metadataTools;
    }

    // todo user substitution

//    @Override
//    public void refreshUserSubstitutions() {
//        component.removeAllComponents();
//
//        UserSessionSource uss = beanLocator.get(UserSessionSource.NAME);
//        List<UserSubstitution> substitutions = getUserSubstitutions();
//
//        AppUI ui = AppUI.getCurrent();
//
//        User currentOrSubstitutedUser = uss.getUserSession().getCurrentOrSubstitutedUser();
//        if (substitutions.isEmpty()) {
//            String substitutedUserCaption = getSubstitutedUserCaption(currentOrSubstitutedUser);
//
//            userComboBox = null;
//
//            userNameLabel = new Label(substitutedUserCaption);
//            userNameLabel.setStyleName("c-user-select-label");
//            userNameLabel.setSizeUndefined();
//
//            if (ui != null && ui.isTestMode()) {
//                userNameLabel.setCubaId("currentUserLabel");
//            }
//
//            component.addComponent(userNameLabel);
//            component.setDescription(substitutedUserCaption);
//        } else {
//            userNameLabel = null;
//
//            userComboBox = new CubaComboBox<>();
//            userComboBox.setEmptySelectionAllowed(false);
//            userComboBox.setItemCaptionGenerator(this::getSubstitutedUserCaption);
//            userComboBox.setStyleName("c-user-select-combobox");
//
//            if (ui != null) {
//                if (ui.isTestMode()) {
//                    userComboBox.setCubaId("substitutedUserSelect");
//                }
//                if (ui.isPerformanceTestMode()) {
//                    userComboBox.setId(ui.getTestIdManager().getTestId("substitutedUserSelect"));
//                }
//            }
//            List<User> options = new ArrayList<>();
//            User sessionUser = uss.getUserSession().getUser();
//            options.add(sessionUser);
//
//            for (UserSubstitution substitution : substitutions) {
//                User substitutedUser = substitution.getSubstitutedUser();
//                options.add(substitutedUser);
//            }
//
//            userComboBox.setItems(options);
//
//            userComboBox.setValue(currentOrSubstitutedUser);
//            userComboBox.addValueChangeListener(this::substitutedUserChanged);
//
//            component.addComponent(userComboBox);
//            component.setDescription(null);
//        }
//
//        adjustWidth();
//        adjustHeight();
//    }
//
//    protected void substitutedUserChanged(ValueChangeEvent<User> event) {
//        UserSessionSource uss = beanLocator.get(UserSessionSource.NAME);
//
//        User newUser = event.getValue();
//        UserSession userSession = uss.getUserSession();
//        if (userSession == null) {
//            throw new RuntimeException("No user session found");
//        }
//
//        User oldUser = userSession.getSubstitutedUser() == null ? userSession.getUser() : userSession.getSubstitutedUser();
//
//        if (!oldUser.equals(newUser)) {
//            String newUserName = StringUtils.isBlank(newUser.getName()) ? newUser.getLogin() : newUser.getName();
//
//            Messages messages = beanLocator.get(Messages.NAME);
//
//            Dialogs dialogs = getScreenContext(this).getDialogs();
//
//            dialogs.createOptionDialog()
//                    .withCaption(messages.getMainMessage("substUserSelectDialog.title"))
//                    .withMessage(messages.formatMainMessage("substUserSelectDialog.msg", newUserName))
//                    .withType(Dialogs.MessageType.WARNING)
//                    .withActions(
//                            new ChangeSubstUserAction(userComboBox.getValue()) {
//                                @Override
//                                public void doRevert() {
//                                    super.doRevert();
//
//                                    revertToCurrentUser();
//                                }
//                            }, new DoNotChangeSubstUserAction() {
//                                @Override
//                                public void actionPerform(io.jmix.ui.components.Component component) {
//                                    super.actionPerform(component);
//
//                                    revertToCurrentUser();
//                                }
//                            })
//                    .show();
//        }
//    }
//
//    protected String getSubstitutedUserCaption(User user) {
//        return userNameFormatter.apply(user);
//    }
//
//    protected String getDefaultUserCaption(User user) {
//        return isNotEmpty(user.getName())
//                ? user.getName()
//                : metadataTools.getInstanceName(user);
//    }
//
//    protected List<UserSubstitution> getUserSubstitutions() {
//        UserManagementService userManagementService = beanLocator.get(UserManagementService.NAME);
//        UserSessionSource uss = beanLocator.get(UserSessionSource.NAME);
//
//        return userManagementService.getSubstitutedUsers(uss.getUserSession().getUser().getId());
//    }
//
//    protected void revertToCurrentUser() {
//        UserSessionSource uss = beanLocator.get(UserSessionSource.NAME);
//        UserSession us = uss.getUserSession();
//
//        userComboBox.setValue(us.getCurrentOrSubstitutedUser());
//    }

    @Override
    public void setWidth(String width) {
        super.setWidth(width);

        adjustWidth();
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);

        adjustHeight();
    }

    protected void adjustWidth() {
        if (getWidth() < 0) {
            if (userNameLabel != null) {
                userNameLabel.setWidth(-1, Unit.PIXELS);
            } else if (userComboBox != null) {
                userComboBox.setWidthUndefined();
            }
        } else {
            if (userNameLabel != null) {
                userNameLabel.setWidth(100, Unit.PERCENTAGE);
            } else if (userComboBox != null) {
                userComboBox.setWidth(100, Unit.PERCENTAGE);
            }
        }
    }

    protected void adjustHeight() {
        if (getHeight() < 0) {
            if (userNameLabel != null) {
                userNameLabel.setHeight(-1, Unit.PIXELS);
            } else if (userComboBox != null) {
                userComboBox.setHeight(-1, Unit.PIXELS);
            }
        } else {
            if (userNameLabel != null) {
                userNameLabel.setHeight(100, Unit.PERCENTAGE);
            } else if (userComboBox != null) {
                userComboBox.setHeight(100, Unit.PERCENTAGE);
            }
        }
    }

    @Override
    public void setUserNameFormatter(Function<? super BaseUser, String> userNameFormatter) {
//        this.userNameFormatter = userNameFormatter;
//        refreshUserSubstitutions();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Function<BaseUser, String> getUserNameFormatter() {
//        return (Function<BaseUser, String>) userNameFormatter;
        return null;
    }
}
