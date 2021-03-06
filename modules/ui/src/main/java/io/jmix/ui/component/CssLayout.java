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

package io.jmix.ui.component;

/**
 * CssLayout is a layout component renders components and their captions into a same DIV element.
 * Component layout can then be adjusted with CSS.
 */
public interface CssLayout extends OrderedContainer, Component.BelongToFrame, Component.HasCaption,
        Component.HasIcon, HasContextHelp, LayoutClickNotifier, ShortcutNotifier, HasHtmlCaption, HasHtmlDescription,
        HasRequiredIndicator, HasHtmlSanitizer {

    String NAME = "cssLayout";
}