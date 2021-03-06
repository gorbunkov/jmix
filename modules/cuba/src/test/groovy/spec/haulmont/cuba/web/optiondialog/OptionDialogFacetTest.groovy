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

package spec.haulmont.cuba.web.optiondialog

import com.haulmont.cuba.web.app.main.MainScreen
import io.jmix.ui.Dialogs
import io.jmix.ui.GuiDevelopmentException
import io.jmix.ui.component.ContentMode
import io.jmix.ui.component.impl.WebButton
import io.jmix.ui.component.impl.WebOptionDialogFacet
import io.jmix.ui.screen.OpenMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.optiondialog.screens.OptionDialogScreen

@SuppressWarnings('GroovyAccessibility')
class OptionDialogFacetTest extends UiScreenSpec {

    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.optiondialog.screens'])
    }

    def 'OptionDialog attributes are correctly loaded'() {
        def screens = vaadinUi.screens

        def mainScreen = screens.create(MainScreen, OpenMode.ROOT)
        mainScreen.show()

        when: 'OptionDialog is configured in XML'

        def screenWithDialog = screens.create(OptionDialogScreen)
        def optionDialog = screenWithDialog.optionDialog

        then: 'Attribute values are propagated to OptionDialog facet'

        optionDialog.id == 'optionDialog'
        optionDialog.caption == 'OptionDialog Facet'
        optionDialog.message == 'OptionDialog Test'
        optionDialog.contentMode == ContentMode.HTML
        optionDialog.height == 200
        optionDialog.width == 350
        optionDialog.styleName == 'opt-dialog-style'
        optionDialog.maximized

        when: 'OptionDialog is shown'

        optionDialog.show()

        then: 'UI has this dialog window'

        vaadinUi.windows.any { window ->
            window.caption == 'OptionDialog Facet'
        }
    }

    def 'Declarative OptionDialog subscription on Action'() {
        def screens = vaadinUi.screens

        def mainScreen = screens.create(MainScreen, OpenMode.ROOT)
        mainScreen.show()

        def screen = screens.create(OptionDialogScreen)
        screen.show()

        when: 'Dialog target action performed'

        screen.dialogAction.actionPerform(screen.dialogButton)

        then: 'Dialog is shown'

        vaadinUi.windows.find { w -> w.caption == 'Dialog Action Subscription' }
    }

    def 'Declarative OptionDialog subscription on Button'() {
        def screens = vaadinUi.screens

        def mainScreen = screens.create(MainScreen, OpenMode.ROOT)
        mainScreen.show()

        def screen = screens.create(OptionDialogScreen)

        when: 'Dialog target button is clicked'

        ((WebButton) screen.dialogButton).buttonClicked(null)

        then: 'Dialog is shown'

        vaadinUi.windows.find { w -> w.caption == 'Dialog Button Subscription' }
    }

    def 'OptionDialog should be bound to frame'() {
        def dialog = new WebOptionDialogFacet()

        when: 'Trying to show Dialog not bound to frame'

        dialog.show()

        then: 'Exception is thrown'

        thrown IllegalStateException

        when: 'Trying to setup declarative subscription without frame'

        dialog.subscribe()

        then: 'Exception is thrown'

        thrown IllegalStateException
    }

    def 'OptionDialog should have single subscription'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create(MainScreen, OpenMode.ROOT)
        screens.show(mainWindow)

        def screen = screens.create(OptionDialogScreen)

        def dialog = new WebOptionDialogFacet()
        dialog.setOwner(screen.getWindow())
        dialog.setActionTarget('actionId')
        dialog.setButtonTarget('buttonId')

        when: 'Both action and button are set as Dialog target'

        dialog.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException
    }

    def 'OptionDialog target should not be missing'() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create(MainScreen, OpenMode.ROOT)
        screens.show(mainWindow)

        def screen = screens.create(OptionDialogScreen)

        def dialog = new WebOptionDialogFacet()
        dialog.setOwner(screen.getWindow())

        when: 'Missing action is set as target'

        dialog.setActionTarget('missingAction')
        dialog.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException

        when: 'Missing button is set as target'

        dialog.setActionTarget(null)
        dialog.setButtonTarget('missingButton')
        dialog.subscribe()

        then: 'Exception is thrown'

        thrown GuiDevelopmentException
    }
}
