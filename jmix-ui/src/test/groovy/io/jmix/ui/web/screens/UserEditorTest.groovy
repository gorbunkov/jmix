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

package io.jmix.ui.web.screens

import io.jmix.core.app.DataService
import io.jmix.core.LoadContext
import io.jmix.ui.WindowParams
import io.jmix.ui.app.security.role.edit.PermissionValue
import io.jmix.ui.app.security.user.edit.UserEditor
import io.jmix.ui.components.PickerField
import io.jmix.ui.components.TextField
import io.jmix.ui.screen.EditorScreen
import io.jmix.ui.screen.MapScreenOptions
import io.jmix.ui.screen.OpenMode
import com.haulmont.cuba.security.entity.EntityOp
import com.haulmont.cuba.security.entity.Group
import com.haulmont.cuba.security.entity.PermissionType
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.web.testsupport.proxy.TestServiceProxy
import spec.cuba.web.UiScreenSpec

import java.util.function.Consumer

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck", "GroovyPointlessBoolean"])
class UserEditorTest extends UiScreenSpec {

    def setup() {
        def group = new Group(name: 'Company')
        def users = [
                new User(login: 'admin', loginLowerCase: 'admin', group: group, active: true, name: 'Administrator'),
                new User(login: 'anonymous', loginLowerCase: 'anonymous', group: group, active: true, name: 'Anonymous')
        ]

        TestServiceProxy.mock(DataService, Mock(DataService) {
            loadList(_) >> { LoadContext lc ->
                if (lc.entityMetaClass == 'sec$User') {
                    return users
                }

                return []
            }
        })
    }

    void cleanup() {
        sessionSource.session.removePermissions(PermissionType.ENTITY_OP)
    }

    def "open UserEditor"() {
        def screens = vaadinUi.screens

        def beforeShowListener = Mock(Consumer)
        def afterShowListener = Mock(Consumer)

        when:
        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        then:
        vaadinUi.topLevelWindow == mainWindow.window

        when:
        def user = new User()
        def params = [(WindowParams.ITEM.name()): user]
        def userEditor = screens.create('sec$User.edit', OpenMode.NEW_TAB, new MapScreenOptions(params))
        ((EditorScreen)userEditor).setEntityToEdit(user)

        userEditor.addBeforeShowListener(beforeShowListener)
        userEditor.addAfterShowListener(afterShowListener)
        userEditor.show()

        then:
        1 * beforeShowListener.accept(_)
        1 * afterShowListener.accept(_)

        userEditor instanceof UserEditor
        screens.getOpenedScreens().currentBreadcrumbs[0] == userEditor
        userEditor.window != null
    }

    def "open UserEditor with denied CREATE and UPDATE"() {
        def screens = vaadinUi.screens
        def session = sessionSource.session

        def userMetaClass = metadata.getClass(User).name
        session.addPermission(PermissionType.ENTITY_OP, userMetaClass + ":" + EntityOp.CREATE.id, "", PermissionValue.DENY.value)
        session.addPermission(PermissionType.ENTITY_OP, userMetaClass + ":" + EntityOp.UPDATE.id, "", PermissionValue.DENY.value)

        when:
        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        then:
        vaadinUi.topLevelWindow == mainWindow.window

        when:
        def user = new User()
        def params = [(WindowParams.ITEM.name()): user]

        UserEditor userEditor = (UserEditor)screens.create('sec$User.edit', OpenMode.NEW_TAB, new MapScreenOptions(params))
        userEditor.setEntityToEdit(user)
        userEditor.show()

        def loginField = (TextField) userEditor.fieldGroupLeft.getComponentNN('login')
        def groupField = (PickerField) userEditor.fieldGroupRight.getComponentNN('group')

        then:
        screens.getOpenedScreens().currentBreadcrumbs[0] == userEditor
        userEditor.window != null
        loginField.isEditable() == false
        groupField.isEditable() == false
    }

    def "open UserEditor with denied READ"() {
        def screens = vaadinUi.screens
        def session = sessionSource.session

        def userMetaClass = metadata.getClass(User).name
        session.addPermission(PermissionType.ENTITY_OP, userMetaClass + ":" + EntityOp.READ.id, "", PermissionValue.DENY.value)

        when:
        def mainWindow = screens.create("mainWindow", OpenMode.ROOT)
        screens.show(mainWindow)

        then:
        vaadinUi.topLevelWindow == mainWindow.window

        when:
        def user = new User()
        def params = [(WindowParams.ITEM.name()): user]

        def userEditor = (UserEditor) screens.create('sec$User.edit', OpenMode.NEW_TAB, new MapScreenOptions(params))
        userEditor.setEntityToEdit(user)
        userEditor.show()

        def loginField = (TextField) userEditor.fieldGroupLeft.getComponentNN('login')
        def groupField = (PickerField) userEditor.fieldGroupRight.getComponentNN('group')

        then:
        screens.getOpenedScreens().currentBreadcrumbs[0] == userEditor
        userEditor.window != null
        loginField.isVisible() == false
        groupField.isVisible() == false
    }
}