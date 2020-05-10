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
package io.jmix.core.security.impl;

import io.jmix.core.CoreProperties;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.core.cluster.ClusterListener;
import io.jmix.core.cluster.ClusterManager;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.security.NoUserSessionException;
import io.jmix.core.security.UserSession;
import io.jmix.core.security.UserSessionEntity;
import io.jmix.core.security.UserSessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * User sessions distributed cache.
 */
@Component(UserSessions.NAME)
public class UserSessionsImpl implements UserSessions {

    public static class UserSessionInfo implements Serializable {
        private static final long serialVersionUID = -4834267718111570841L;

        public final UserSession session;
        public final long since;
        public volatile long lastUsedTs; // set to 0 when propagating removal to cluster
        public volatile long lastSentTs;

        public UserSessionInfo(UserSession session, long now) {
            this.session = session;
            this.since = now;
            this.lastUsedTs = now;
            this.lastSentTs = now;
        }

        public UserSession getSession() {
            return session;
        }

        public long getSince() {
            return since;
        }

        public long getLastUsedTs() {
            return lastUsedTs;
        }

        public long getLastSentTs() {
            return lastSentTs;
        }

        @Override
        public String toString() {
            return String.format("%s, since: %s, lastUsed: %s",
                    session, new Date(since), new Date(lastSentTs));
        }
    }

    private static final Logger log = LoggerFactory.getLogger(UserSessionsImpl.class);

    protected Map<UUID, UserSessionInfo> cache = new ConcurrentHashMap<>();

    protected ClusterManager clusterManager;

    @Inject
    protected CoreProperties properties;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected Metadata metadata;

    // todo UserSessionLog
//    @Inject
//    protected UserSessionLog userSessionLog;

//    private static class ServerUser extends BaseUuidEntity implements User {
//
//        private String login = "server";
//
//        @Override
//        public String getLogin() {
//            return login;
//        }
//
//        @Override
//        public void setLogin(String server) {
//        }
//
//        @Override
//        public String getLoginLowerCase() {
//            return login;
//        }
//
//        @Override
//        public String getName() {
//            return null;
//        }
//    }

//    public UserSessionsImpl() {
//        User serverUser = new ServerUser();
//        NO_USER_SESSION = new UserSession(serverUser) {
//            @Override
//            public UUID getId() {
//                return AppContext.NO_USER_CONTEXT.getSessionId();
//            }
//        };
//    }

    @Inject
    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
        this.clusterManager.addListener(
                UserSessionInfo.class,
                new ClusterListener<UserSessionInfo>() {

                    @Override
                    public void receive(UserSessionInfo message) {
                        receiveClusterMessage(message);
                    }

                    @Override
                    public byte[] getState() {
                        return sendClusterState();
                    }

                    @Override
                    public void setState(byte[] state) {
                        receiveClusterState(state);
                    }
                }
        );
    }

    protected void receiveClusterMessage(UserSessionInfo message) {
        UUID id = message.session.getId();
        if (message.lastUsedTs == 0) {
            log.debug("Removing session due to cluster message: {}", message);
            removeSessionInfo(id);
        } else {
            UserSessionInfo usi = getSessionInfo(id);
            if (usi == null || usi.lastUsedTs < message.lastUsedTs) {
                putSessionInfo(id, message);
            }
        }
    }

    protected void receiveClusterState(byte[] state) {
        if (state == null || state.length == 0) {
            log.debug("Received empty user sessions cache");
            return;
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(state);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            int size = ois.readInt();
            for (int i = 0; i < size; i++) {
                UserSessionInfo usi = (UserSessionInfo) ois.readObject();
                receiveClusterMessage(usi);
            }
            log.debug("Received user sessions cache: {} sessions, {} bytes. Cache now contains {} sessions", size, state.length, cache.size());
        } catch (IOException | ClassNotFoundException e) {
            log.error("Error receiving state", e);
        }
    }

    protected byte[] sendClusterState() {
        List<UserSessionInfo> infoList = getSessionInfoStream().collect(Collectors.toList());
        if (infoList.isEmpty())
            return new byte[0];

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeInt(infoList.size());
            for (UserSessionInfo usi : infoList) {
                oos.writeObject(usi);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error sending state", e);
        }
        byte[] bytes = bos.toByteArray();
        log.debug("Sending user sessions cache to cluster: {} sessions, {} bytes", infoList.size(), bytes.length);
        return bytes;
    }

    @Override
    public void add(UserSession session) {
        UserSessionInfo usi = new UserSessionInfo(session, timeSource.currentTimeMillis());
        putSessionInfo(session.getId(), usi);

        if (properties.isSyncNewUserSessionReplication())
            clusterManager.sendSync(usi);
        else
            clusterManager.send(usi);
    }

    @Override
    public void remove(UserSession session) {
        UserSessionInfo usi = removeSessionInfo(session.getId());
        if (usi != null) {
            log.debug("Removed session: {}", usi);
            usi.lastUsedTs = 0;
            clusterManager.send(usi);
        }
    }

    @Nullable
    @Override
    public UserSession get(UUID id) {
        return internalGet(id, false, false);
    }

    @Override
    public UserSession getNN(UUID id) {
        UserSession userSession = internalGet(id, false, false);
        if (userSession == null)
            throw new NoUserSessionException(id);
        return userSession;
    }

    @Nullable
    @Override
    public UserSession getAndRefresh(UUID id) {
        return internalGet(id, true, false);
    }

    @Override
    public UserSession getAndRefreshNN(UUID id) {
        UserSession userSession = getAndRefresh(id);
        if (userSession == null)
            throw new NoUserSessionException(id);
        return userSession;
    }

    @Nullable
    @Override
    public UserSession getAndRefresh(UUID id, boolean propagate) {
        return internalGet(id, true, propagate);
    }

    @Override
    public UserSession getAndRefreshNN(UUID id, boolean propagate) {
        UserSession userSession = getAndRefresh(id, propagate);
        if (userSession == null)
            throw new NoUserSessionException(id);
        return userSession;
    }

    @Nullable
    protected UserSession internalGet(UUID id, boolean touch, boolean propagate) {
        UserSessionInfo usi = getSessionInfo(id);
        if (usi != null) {
            if (touch) {
                long now = timeSource.currentTimeMillis();

                if (now > (usi.lastUsedTs + toMillis(properties.getUserSessionTouchTimeoutSec()))) {
                    usi.lastUsedTs = now;
                    putSessionInfo(id, usi);
                }

                if (propagate) {
                    if (now > (usi.lastSentTs + toMillis(properties.getUserSessionSendTimeoutSec()))) {
                        usi.lastSentTs = now;
                        clusterManager.send(usi);
                    }
                }
            }
            return usi.session;
        }
        return null;
    }

    @Override
    public void propagate(UUID id) {
        UserSessionInfo usi = getSessionInfo(id);
        if (usi != null) {
            long now = timeSource.currentTimeMillis();
            usi.lastUsedTs = now;
            usi.lastSentTs = now;
            putSessionInfo(id, usi);
            clusterManager.send(usi);
        }
    }

    @Override
    public Collection<UserSessionEntity> getUserSessionInfo() {
        return getSessionInfoStream()
                .map(info -> createUserSessionEntity(info.session, info.since, info.lastUsedTs))
                .collect(Collectors.toList());
    }

    protected UserSessionEntity createUserSessionEntity(UserSession session, long since, long lastUsedTs) {
        UserSessionEntity use = metadata.create(UserSessionEntity.class);
        use.setId(session.getId());
        use.setLogin(session.getUser().getUsername());
        use.setUserName(session.getUser().getUsername());
        use.setAddress(session.getClientDetails().getAddress());
        use.setClientInfo(session.getClientDetails().getInfo());
        use.setSince(new Date(since));
        use.setLastUsedTs(new Date(lastUsedTs));
        return use;
    }

    @Override
    public Stream<UserSessionEntity> getUserSessionEntitiesStream() {
        return getSessionInfoStream()
                .map(info -> createUserSessionEntity(info.session, info.since, info.lastUsedTs));
    }

    @Override
    public Stream<UserSession> getUserSessionsStream() {
        return getSessionInfoStream().map(info -> info.session);
    }

    @Override
    public void killSession(UUID id) {
        UserSessionInfo usi = removeSessionInfo(id);

        if (usi != null) {
            log.debug("Killed session: {}", usi);

            usi.lastUsedTs = 0;
            clusterManager.send(usi);
        }
    }

    @Override
    public List<UUID> findUserSessionsByAttribute(String attributeName, Object attributeValue) {
        Preconditions.checkNotNullArgument(attributeName);

        //noinspection UnnecessaryLocalVariable
        List<UUID> sessionIds = getSessionInfoStream()
                .filter(usInfo -> Objects.equals(usInfo.session.getAttribute(attributeName), attributeValue))
                .map(userSessionInfo -> userSessionInfo.session.getId())
                .collect(Collectors.toList());

        return sessionIds;
    }

    @Override
    public void processEviction() {
        log.trace("Processing eviction");
        long now = timeSource.currentTimeMillis();

        getSessionInfoStream()
                .filter(info -> now > (info.lastUsedTs + toMillis(properties.getUserSessionExpirationTimeoutSec())))
                .forEach(usi -> {
                    log.debug("Removing session due to timeout: {}", usi);

                    // todo UserSessionLog
//                    userSessionLog.updateSessionLogRecord(usi.getSession(), SessionAction.EXPIRATION);

                    removeSessionInfo(usi.session.getId());

                    usi.lastUsedTs = 0;
                    clusterManager.send(usi);
                });
    }

    protected UserSessionInfo getSessionInfo(UUID id) {
        return cache.get(id);
    }

    protected void putSessionInfo(UUID id, UserSessionInfo info) {
        cache.put(id, info);
    }

    @Nullable
    protected UserSessionInfo removeSessionInfo(UUID id) {
        return cache.remove(id);
    }

    protected Stream<UserSessionInfo> getSessionInfoStream() {
        return cache.values().stream();
    }

    protected long toMillis(int seconds) {
        return seconds * 1000L;
    }
}