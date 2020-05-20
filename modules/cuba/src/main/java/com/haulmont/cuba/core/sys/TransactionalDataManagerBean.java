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

package com.haulmont.cuba.core.sys;

import com.haulmont.cuba.core.TransactionalDataManager;
import com.haulmont.cuba.core.Transactions;
import io.jmix.core.Entity;
import io.jmix.core.entity.KeyValueEntity;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

@Component(TransactionalDataManager.NAME)
public class TransactionalDataManagerBean implements TransactionalDataManager {

    @Inject
    protected DataManager dataManager;

    @Inject
    protected Transactions transactions;

    @Inject
    private Metadata metadata;

    @Inject
    private EntityStates entityStates;

    @Override
    public <E extends Entity> FluentLoader<E> load(Class<E> entityClass) {
        return new FluentLoader<>(entityClass, dataManager.getDelegate());
    }

    @Override
    public <E extends Entity> FluentLoader.ById<E> load(Id<E> entityId) {
        return new FluentLoader<>(entityId.getEntityClass(), dataManager.getDelegate()).id(entityId.getValue());
    }

    @Override
    public FluentValuesLoader loadValues(String queryString) {
        return new FluentValuesLoader(queryString, dataManager.getDelegate());
    }

    @Override
    public <T> FluentValueLoader<T> loadValue(String queryString, Class<T> valueClass) {
        return new FluentValueLoader<>(queryString, valueClass, dataManager.getDelegate());
    }

    @Nullable
    @Override
    public <E extends Entity> E load(LoadContext<E> context) {
        context.setJoinTransaction(true);
        return dataManager.load(context);
    }

    @Override
    public <E extends Entity> List<E> loadList(LoadContext<E> context) {
        context.setJoinTransaction(true);
        return dataManager.loadList(context);
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        context.setJoinTransaction(true);
        return dataManager.loadValues(context);
    }

    @Override
    public EntitySet save(Entity... entities) {
        CommitContext cc = new CommitContext(entities);
        cc.setJoinTransaction(true);
        return dataManager.commit(cc);
    }

    @Override
    public <E extends Entity> E save(E entity) {
        CommitContext cc = new CommitContext(entity);
        cc.setJoinTransaction(true);
        return dataManager.commit(cc).get(entity);
    }

    @Override
    public <E extends Entity> E save(E entity, @Nullable FetchPlan view) {
        CommitContext cc = new CommitContext();
        cc.addInstanceToCommit(entity, view);
        cc.setJoinTransaction(true);
        return dataManager.commit(cc).get(entity);
    }

    @Override
    public <E extends Entity> E save(E entity, @Nullable String viewName) {
        CommitContext cc = new CommitContext();
        cc.addInstanceToCommit(entity, viewName);
        cc.setJoinTransaction(true);
        return dataManager.commit(cc).get(entity);
    }

    @Override
    public void remove(Entity entity) {
        CommitContext cc = new CommitContext();
        cc.addInstanceToRemove(entity);
        cc.setJoinTransaction(true);
        dataManager.commit(cc);
    }

    @Override
    public <T extends Entity> T create(Class<T> entityClass) {
        return metadata.create(entityClass);
    }

    @Override
    public <T extends Entity, K> T getReference(Class<T> entityClass, K id) {
        T entity = metadata.create(entityClass);
        EntityValues.setId(entity, id);
        entityStates.makePatch(entity);
        return entity;
    }

    @Override
    public TransactionalDataManager secure() {
        return new Secure(dataManager, transactions);
    }

    @Override
    public Transactions transactions() {
        return transactions;
    }

    private static class Secure extends TransactionalDataManagerBean {

        public Secure(DataManager dataManager, Transactions transactions) {
            this.dataManager = dataManager.secure();
            this.transactions = transactions;
        }
    }
}
