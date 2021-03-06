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

package io.jmix.rest.api.service;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.jmix.core.*;
import io.jmix.core.metamodel.datatype.Datatypes;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.EntityOp;
import io.jmix.core.security.Security;
import io.jmix.rest.api.common.RestControllerUtils;
import io.jmix.rest.api.common.RestParseUtils;
import io.jmix.rest.api.config.RestQueriesConfiguration;
import io.jmix.rest.api.exception.RestAPIException;
import io.jmix.rest.api.transform.JsonTransformationDirection;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import javax.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.ParseException;
import java.util.*;

@Component("jmix_QueriesControllerManager")
public class QueriesControllerManager {

    @Autowired
    protected RestQueriesConfiguration restQueriesConfiguration;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected EntitySerialization entitySerializationAPI;

    @Autowired
    protected Security security;

    @Autowired
    protected RestControllerUtils restControllerUtils;

//    @Autowired
//    protected PersistenceManagerClient persistenceManagerClient;

    @Autowired
    protected RestParseUtils restParseUtils;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    public String executeQueryGet(String entityName,
                                  String queryName,
                                  @Nullable Integer limit,
                                  @Nullable Integer offset,
                                  @Nullable String viewName,
                                  @Nullable Boolean returnNulls,
                                  @Nullable Boolean dynamicAttributes,
                                  @Nullable String version,
                                  Map<String, String> params) {
        return _executeQuery(entityName, queryName, limit, offset, viewName, returnNulls, dynamicAttributes, version, params);
    }

    public String executeQueryPost(String entityName,
                                   String queryName,
                                   @Nullable Integer limit,
                                   @Nullable Integer offset,
                                   @Nullable String viewName,
                                   @Nullable Boolean returnNulls,
                                   @Nullable Boolean dynamicAttributes,
                                   @Nullable String version,
                                   String paramsJson) {
        Map<String, String> paramsMap = restParseUtils.parseParamsJson(paramsJson);
        return _executeQuery(entityName, queryName, limit, offset, viewName, returnNulls, dynamicAttributes, version, paramsMap);
    }

    protected String _executeQuery(String entityName,
                                   String queryName,
                                   @Nullable Integer limit,
                                   @Nullable Integer offset,
                                   @Nullable String viewName,
                                   @Nullable Boolean returnNulls,
                                   @Nullable Boolean dynamicAttributes,
                                   @Nullable String version,
                                   Map<String, String> params) {
        LoadContext<Entity> ctx;
        entityName = restControllerUtils.transformEntityNameIfRequired(entityName, version, JsonTransformationDirection.FROM_VERSION);
        try {
            ctx = createQueryLoadContext(entityName, queryName, limit, offset, params);
        } catch (ClassNotFoundException | ParseException e) {
            throw new RestAPIException("Error on executing the query", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        ctx.setLoadDynamicAttributes(BooleanUtils.isTrue(dynamicAttributes));

        //override default view defined in queries config
        if (!Strings.isNullOrEmpty(viewName)) {
            MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
            ctx.setFetchPlan(restControllerUtils.getView(metaClass, viewName));
        }
        List<Entity> entities = dataManager.loadList(ctx);
        entities.forEach(entity -> restControllerUtils.applyAttributesSecurity(entity));

        List<EntitySerializationOption> serializationOptions = new ArrayList<>();
        serializationOptions.add(EntitySerializationOption.SERIALIZE_INSTANCE_NAME);
        if (BooleanUtils.isTrue(returnNulls)) serializationOptions.add(EntitySerializationOption.SERIALIZE_NULLS);

        String json = entitySerializationAPI.toJson(entities, ctx.getFetchPlan(), serializationOptions.toArray(new EntitySerializationOption[0]));
        json = restControllerUtils.transformJsonIfRequired(entityName, version, JsonTransformationDirection.TO_VERSION, json);
        return json;
    }

    public String getCountGet(String entityName,
                              String queryName,
                              String version,
                              Map<String, String> params) {
        return _getCount(entityName, queryName, version, params);
    }

    public String getCountPost(String entityName,
                               String queryName,
                               String version,
                               String paramsJson) {
        Map<String, String> paramsMap = restParseUtils.parseParamsJson(paramsJson);
        return _getCount(entityName, queryName, version, paramsMap);
    }

    protected String _getCount(String entityName, String queryName, String version, Map<String, String> params) {
        entityName = restControllerUtils.transformEntityNameIfRequired(entityName, version, JsonTransformationDirection.FROM_VERSION);
        LoadContext<Entity> ctx;
        try {
            ctx = createQueryLoadContext(entityName, queryName, null, null, params);
        } catch (ClassNotFoundException | ParseException e) {
            throw new RestAPIException("Error on executing the query", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
        long count = dataManager.getCount(ctx);
        return String.valueOf(count);
    }

    public List<RestQueriesConfiguration.QueryInfo> loadQueriesList(String entityName) {
        MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
        checkCanReadEntity(metaClass);
        return restQueriesConfiguration.getQueries(entityName);
    }

    protected LoadContext<Entity> createQueryLoadContext(String entityName,
                                                         String queryName,
                                                         @Nullable Integer limit,
                                                         @Nullable Integer offset,
                                                         Map<String, String> params) throws ClassNotFoundException, ParseException {
        MetaClass metaClass = restControllerUtils.getMetaClass(entityName);
        checkCanReadEntity(metaClass);

        RestQueriesConfiguration.QueryInfo queryInfo = restQueriesConfiguration.getQuery(entityName, queryName);
        if (queryInfo == null) {
            throw new RestAPIException("Query not found",
                    String.format("Query with name %s for entity %s not found", queryName, entityName),
                    HttpStatus.NOT_FOUND);
        }

        LoadContext<Entity> ctx = new LoadContext<>(metaClass);
        LoadContext.Query query = new LoadContext.Query(queryInfo.getJpql());

        if (limit != null) {
            query.setMaxResults(limit);
        } else if (queryInfo.getLimit() != null) {
            query.setMaxResults(queryInfo.getLimit());
        } else {
            // todo PersistenceManagerClient
//            query.setMaxResults(persistenceManagerClient.getMaxFetchUI(entityName));
        }

        if (offset != null) {
            query.setFirstResult(offset);
        } else if (queryInfo.getOffset() != null) {
            query.setFirstResult(queryInfo.getOffset());
        }

        for (RestQueriesConfiguration.QueryParamInfo paramInfo : queryInfo.getParams()) {
            String paramName = paramInfo.getName();
            String requestParamValue = params.get(paramName);
            if (requestParamValue == null) {
                throw new RestAPIException("Query parameter not found",
                        String.format("Query parameter %s not found", paramName),
                        HttpStatus.BAD_REQUEST);
            }

            Class<?> clazz = ClassUtils.forName(paramInfo.getType(), getClass().getClassLoader());
            Object objectParamValue = toObject(clazz, requestParamValue);
            query.setParameter(paramName, objectParamValue);
        }

        //todo query parameters
//        if (queryInfo.getJpql().contains(":session$userId")) {
//            query.setParameter("session$userId", currentAuthentication.getUser().getKey());
//        }
//        if (queryInfo.getJpql().contains(":session$userLogin")) {
//            // todo user substitution
//            // query.setParameter("session$userLogin", userSessionSource.getUserSession().getCurrentOrSubstitutedUser().getLoginLowerCase());
//            query.setParameter("session$userLogin", currentAuthentication.getUserSession().getUser().getLoginLowerCase());
//        }

        query.setCacheable(queryInfo.isCacheable());
        ctx.setQuery(query);
        ctx.setFetchPlan(restControllerUtils.getView(metaClass, queryInfo.getViewName()));
        return ctx;
    }

    protected void checkCanReadEntity(MetaClass metaClass) {
        if (!security.isEntityOpPermitted(metaClass, EntityOp.READ)) {
            throw new RestAPIException("Reading forbidden",
                    String.format("Reading of the %s is forbidden", metaClass.getName()),
                    HttpStatus.FORBIDDEN);
        }
    }

    protected Object toObject(Class clazz, String value) throws ParseException {
        if (clazz.isArray()) {
            Class componentType = clazz.getComponentType();
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = jsonParser.parse(value).getAsJsonArray();
            List result = new ArrayList();
            for (JsonElement jsonElement : jsonArray) {
                String stringValue = (jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) ?
                        jsonElement.getAsJsonPrimitive().getAsString() :
                        jsonElement.toString();
                Object arrayElementValue = toObject(componentType, stringValue);
                result.add(arrayElementValue);
            }
            return result;
        }
        if (EnumClass.class.isAssignableFrom(clazz)) {
            //noinspection unchecked
            return Enum.valueOf((Class<Enum>) clazz, value);
        }
        if (String.class == clazz) return value;
        if (Integer.class == clazz || Integer.TYPE == clazz
                || Byte.class == clazz || Byte.TYPE == clazz
                || Short.class == clazz || Short.TYPE == clazz) return Datatypes.getNN(Integer.class).parse(value);
        if (Date.class == clazz) {
            try {
                return Datatypes.getNN(Date.class).parse(value);
            } catch (ParseException e) {
                try {
                    return Datatypes.getNN(java.sql.Date.class).parse(value);
                } catch (ParseException e1) {
                    return Datatypes.getNN(Time.class).parse(value);
                }
            }
        }
        if (BigDecimal.class == clazz) return Datatypes.getNN(BigDecimal.class).parse(value);
        if (Boolean.class == clazz || Boolean.TYPE == clazz) return Datatypes.getNN(Boolean.class).parse(value);
        if (Long.class == clazz || Long.TYPE == clazz) return Datatypes.getNN(Long.class).parse(value);
        if (Double.class == clazz || Double.TYPE == clazz
                || Float.class == clazz || Float.TYPE == clazz) return Datatypes.getNN(Double.class).parse(value);
        if (UUID.class == clazz) return UUID.fromString(value);
        throw new IllegalArgumentException("Parameters of type " + clazz.getName() + " are not supported");
    }

}
