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

package io.jmix.samples.rest;

import io.jmix.core.DataManager;
import io.jmix.core.JmixCoreConfiguration;
import io.jmix.core.security.Authenticator;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.rest.JmixRestConfiguration;
import io.jmix.security.JmixSecurityConfiguration;
import io.jmix.security.entity.User;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;

import javax.sql.DataSource;
import java.util.List;

@SpringBootApplication
@Import({JmixCoreConfiguration.class,
        JmixSecurityConfiguration.class,
        JmixDataConfiguration.class,
        JmixRestConfiguration.class})
public class SampleRestApplication {

    @Autowired
    protected DataManager dataManager;

    @Autowired
    Authenticator authenticator;

    public static void main(String[] args) {
        SpringApplication.run(SampleRestApplication.class, args);
    }

    @EventListener(ApplicationStartedEvent.class)
    private void onStartup() {
        authenticator.withSystem(() -> {
//            Group group = new Group();
//            group.setName("Root");
//            User user = new User();
//            user.setLogin("u1");
//            user.setName("User 1");
//            user.setGroup(group);
//            dataManager.save(group, user);
//
//            List<User> users = dataManager.load(User.class).list();
//            System.out.println(">>> users: " + users);

            Greeting greeting = new Greeting();
            greeting.setText("Hello");
            dataManager.save(greeting);

            return null;
        });
    }

    @Bean
    protected DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:hsqldb:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
