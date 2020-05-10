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
package io.jmix.ui.presentations;

import io.jmix.core.*;
import io.jmix.core.commons.xmlparsing.Dom4jTools;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.Presentation;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.ui.components.Component;
import io.jmix.ui.components.ComponentsHelper;
import io.jmix.ui.sys.PersistenceHelper;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.*;

public class PresentationsImpl implements Presentations {

    private String name;
    private Map<Object, Presentation> presentations;
    private Presentation current;
    private Presentation def;

    private Set<Presentation> needToUpdate = new HashSet<>();
    private Set<Presentation> needToRemove = new HashSet<>();

    private List<PresentationsChangeListener> listeners;
    private FetchPlanRepository fetchPlanRepository;

    public PresentationsImpl(Component c) {
        name = ComponentsHelper.getComponentPath(c);
    }

    @Override
    public void add(Presentation p) {
        checkLoad();
        presentations.put(EntityValues.<UUID>getId(p), p);
        if (PersistenceHelper.isNew(p)) {
            needToUpdate.add(p);

            if (BooleanUtils.isTrue(p.getDefault())) {
                def = p;
            }
        }
        firePresentationsSetChanged();
    }

    @Override
    public Presentation getCurrent() {
        checkLoad();
        return current;
    }

    @Override
    public void setCurrent(Presentation p) {
        checkLoad();
        if (p == null) {
            Object old = current;
            current = null;
            fireCurrentPresentationChanged(old);
        } else if (presentations.containsKey(EntityValues.<UUID>getId(p))) {
            Object old = current;
            current = p;
            fireCurrentPresentationChanged(old);
        } else {
            throw new IllegalStateException(String.format("Invalid presentation: %s", EntityValues.<UUID>getId(p)));
        }
    }

    @Override
    public Element getSettings(Presentation p) {
        p = getPresentation(EntityValues.<UUID>getId(p));
        if (p != null) {
            Document doc;
            if (!StringUtils.isEmpty(p.getXml())) {
                doc = AppBeans.get(Dom4jTools.class).readDocument(p.getXml());
            } else {
                doc = DocumentHelper.createDocument();
                doc.setRootElement(doc.addElement("presentation"));
            }
            return doc.getRootElement();
        } else {
            return null;
        }
    }

    @Override
    public void setSettings(Presentation p, Element e) {
        p = getPresentation(EntityValues.<UUID>getId(p));
        if (p != null) {
            p.setXml(AppBeans.get(Dom4jTools.class).writeDocument(e.getDocument(), false));
            modify(p);
        }
    }

    @Override
    public Presentation getPresentation(Object id) {
        checkLoad();
        return presentations.get(id);
    }

    @Override
    public String getCaption(Object id) {
        Presentation p = getPresentation(id);
        if (p != null) {
            return p.getName();
        }
        return null;
    }

    @Override
    public Collection<Object> getPresentationIds() {
        checkLoad();
        return Collections.unmodifiableCollection(presentations.keySet());
    }

    @Override
    public void setDefault(Presentation p) {
        checkLoad();
        if (p == null) {
            Object old = def;
            def = null;
            fireDefaultPresentationChanged(old);
        } else if (presentations.containsKey(EntityValues.<UUID>getId(p))) {
            Object old = def;
            if (def != null) {
                def.setDefault(false);
            }
            p.setDefault(true);
            def = p;
            fireDefaultPresentationChanged(old);
        } else {
            throw new IllegalStateException(String.format("Invalid presentation: %s", EntityValues.<UUID>getId(p)));
        }
    }

    @Override
    public Presentation getDefault() {
        return def;
    }

    @Override
    public void remove(Presentation p) {
        checkLoad();
        if (presentations.remove(EntityValues.<UUID>getId(p)) != null) {
            if (PersistenceHelper.isNew(p)) {
                needToUpdate.remove(p);
            } else {
                needToUpdate.remove(p);
                needToRemove.add(p);
            }

            if (p.equals(def)) {
                def = null;
            }

            if (p.equals(current)) {
                current = null;
            }

            firePresentationsSetChanged();
        }
    }

    @Override
    public void modify(Presentation p) {
        checkLoad();
        if (presentations.containsKey(EntityValues.<UUID>getId(p))) {
            needToUpdate.add(p);
            if (BooleanUtils.isTrue(p.getDefault())) {
                setDefault(p);
            } else if (def != null && EntityValues.<UUID>getId(def).equals(EntityValues.<UUID>getId(p))) {
                setDefault(null);
            }
        } else {
            throw new IllegalStateException(String.format("Invalid presentation: %s", EntityValues.<UUID>getId(p)));
        }
    }

    @Override
    public boolean isAutoSave(Presentation p) {
        p = getPresentation(EntityValues.<UUID>getId(p));
        return p != null && BooleanUtils.isTrue(p.getAutoSave());
    }

    @Override
    public boolean isGlobal(Presentation p) {
        p = getPresentation(EntityValues.<UUID>getId(p));
        return p != null && !PersistenceHelper.isNew(p) && p.getUser() == null;
    }

    @Override
    public void commit() {
        if (!needToUpdate.isEmpty() || !needToRemove.isEmpty()) {
            DataManager ds = AppBeans.get(DataManager.NAME);

            SaveContext ctx = new SaveContext().saving(needToUpdate).removing(needToRemove);
            Set<Entity> commitResult = ds.save(ctx);
            commited(commitResult);

            clearCommitList();

            firePresentationsSetChanged();
        }
    }

    public void commited(Set<Entity> entities) {
        for (Entity entity : entities) {
            if (entity.equals(def))
                setDefault((Presentation) entity);
            else if (entity.equals(current))
                current = (Presentation) entity;

            if (presentations.containsKey(EntityValues.getId(entity))) {
                presentations.put(EntityValues.getId(entity), (Presentation) entity);
            }
        }
    }

    @Override
    public void addListener(PresentationsChangeListener listener) {
        if (listeners == null) {
            listeners = new LinkedList<>();
        }
        listeners.add(listener);
    }

    @Override
    public void removeListener(PresentationsChangeListener listener) {
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listeners = null;
            }
        }
    }

    @Override
    public Presentation getPresentationByName(String name) {
        for (Presentation p : presentations.values()) {
            if (name.equalsIgnoreCase(p.getName())) {
                return p;
            }
        }
        return null;
    }

    protected void fireCurrentPresentationChanged(Object oldPresentationId) {
        if (listeners != null) {
            for (final PresentationsChangeListener listener : listeners) {
                listener.currentPresentationChanged(this, oldPresentationId);
            }
        }
    }

    protected void firePresentationsSetChanged() {
        if (listeners != null) {
            for (final PresentationsChangeListener listener : listeners) {
                listener.presentationsSetChanged(this);
            }
        }
    }

    protected void fireDefaultPresentationChanged(Object oldPresentationId) {
        if (listeners != null) {
            for (final PresentationsChangeListener listener : listeners) {
                listener.defaultPresentationChanged(this, oldPresentationId);
            }
        }
    }

    private void checkLoad() {
        if (presentations == null) {
            DataManager ds = AppBeans.get(DataManager.NAME);
            LoadContext<Presentation> ctx = new LoadContext<>(Presentation.class);
            ctx.setFetchPlan(AppBeans.get(FetchPlanRepository.class).getFetchPlan(Presentation.class, "app"));

            CurrentAuthentication currentAuthentication = AppBeans.get(CurrentAuthentication.NAME);
            // todo user substitution
            BaseUser user = currentAuthentication.getUser();

            ctx.setQueryString("select p from sec$Presentation p " +
                    "where p.componentId = :component and (p.user is null or p.user.id = :userId)")
                    .setParameter("component", name)
                    .setParameter("userId", EntityValues.<UUID>getId(user));

            final List<Presentation> list = ds.loadList(ctx);

            presentations = new LinkedHashMap<>(list.size());
            for (final Presentation p : list) {
                presentations.put(EntityValues.<UUID>getId(p), p);
            }
        }
    }

    private void clearCommitList() {
        needToUpdate.clear();
        needToRemove.clear();
    }
}
