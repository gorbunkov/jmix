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

package io.jmix.ui.model.impl;

import io.jmix.core.*;
import io.jmix.core.commons.events.Subscription;
import io.jmix.core.commons.events.sys.VoidSubscription;
import io.jmix.ui.model.DataContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Dummy implementation of {@link DataContext} used for read-only screens like entity browsers.
 */
public class NoopDataContext implements DataContext {

    protected BeanLocator beanLocator;

    public NoopDataContext(BeanLocator beanLocator) {
        this.beanLocator = beanLocator;
    }

    @Nullable
    @Override
    public <T extends Entity> T find(Class<T> entityClass, Object entityId) {
        return null;
    }

    @Override
    public <T extends Entity> T find(T entity) {
        return null;
    }

    @Override
    public boolean contains(Entity entity) {
        return false;
    }

    @Override
    public <T extends Entity> T merge(T entity) {
        return entity;
    }

    @Override
    public EntitySet merge(Collection<? extends Entity> entities) {
        return EntitySet.of(entities);
    }

    @Override
    public void remove(Entity entity) {
    }

    @Override
    public void evict(Entity entity) {
    }

    @Override
    public void evictModified() {
    }

    @Override
    public void clear() {
    }

    @Override
    public <T extends Entity> T create(Class<T> entityClass) {
        return beanLocator.get(Metadata.class).create(entityClass);
    }

    @Override
    public boolean hasChanges() {
        return false;
    }

    @Override
    public boolean isModified(Entity entity) {
        return false;
    }

    @Override
    public void setModified(Entity entity, boolean modified) {
    }

    @Override
    public Set<Entity> getModified() {
        return Collections.emptySet();
    }

    @Override
    public boolean isRemoved(Entity entity) {
        return false;
    }

    @Override
    public Set<Entity> getRemoved() {
        return Collections.emptySet();
    }

    @Override
    public EntitySet commit() {
        return EntitySet.of(Collections.emptySet());
    }

    @Override
    public DataContext getParent() {
        return null;
    }

    @Override
    public void setParent(DataContext parentContext) {
    }

    @Override
    public Subscription addChangeListener(Consumer<ChangeEvent> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Override
    public Subscription addPreCommitListener(Consumer<PreCommitEvent> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Override
    public Subscription addPostCommitListener(Consumer<PostCommitEvent> listener) {
        return VoidSubscription.INSTANCE;
    }

    @Override
    public Function<SaveContext, Set<Entity>> getCommitDelegate() {
        return null;
    }

    @Override
    public void setCommitDelegate(Function<SaveContext, Set<Entity>> delegate) {
    }
}
