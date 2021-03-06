/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.mocksafe.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.enterprise.event.Event;

import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.security.client.local.context.ActiveUserCache;
import org.jboss.errai.security.client.local.context.SecurityContext;
import org.jboss.errai.security.client.local.context.impl.BasicUserCacheImpl;
import org.jboss.errai.security.client.local.context.impl.SecurityContextImpl;
import org.jboss.errai.security.client.local.identity.LocalStorageHandler;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.event.LoggedInEvent;
import org.jboss.errai.security.shared.event.LoggedOutEvent;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jboss.errai.security.shared.service.NonCachingUserService;
import org.jboss.errai.security.util.GwtMockitoRunnerExtension;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

@RunWith(GwtMockitoRunnerExtension.class)
public class UserCacheTest {

  @Mock
  private LocalStorageHandler storageHandler;
  @Mock
  private Caller<AuthenticationService> caller;
  @Mock
  private NonCachingUserService authService;
  @Mock
  private Logger logger;
  @Mock
  private Event<LoggedInEvent> loginEvent;
  @Mock
  private Event<LoggedOutEvent> logoutEvent;
  @Mock
  private Navigation nav;
  @InjectMocks
  private SecurityContext securityContext = new SecurityContextImpl();
  @InjectMocks
  private ActiveUserCache userCache = new BasicUserCacheImpl();

  private Method loadMethod;
  private Method rpcMethod;

  class AuthServiceAnswer implements Answer<NonCachingUserService> {
    private final User response;

    public AuthServiceAnswer(final User user) {
      response = user;
    }

    @Override
    public NonCachingUserService answer(final InvocationOnMock invocation) throws Throwable {
      when(authService.getUser()).then(new Answer<User>() {
        @Override
        public User answer(final InvocationOnMock subInvocation) throws Throwable {
          @SuppressWarnings("unchecked")
          final RemoteCallback<User> callback = (RemoteCallback<User>) invocation.getArguments()[0];
          callback.callback(response);
          return null;
        }
      });

      return authService;
    }
  }

  @Before
  public void setup() throws NoSuchMethodException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    loadMethod = BasicUserCacheImpl.class.getDeclaredMethod("maybeLoadStoredCache");
    rpcMethod = SecurityContextImpl.class.getDeclaredMethod("updateCacheFromServer");
    loadMethod.setAccessible(true);
    rpcMethod.setAccessible(true);
    
    final Field userCacheField = SecurityContextImpl.class.getDeclaredField("userCache");
    userCacheField.setAccessible(true);
    userCacheField.set(securityContext, userCache);
  }

  @Test
  public void testRpcOverridesStoredUser() throws Exception {
    final User expected = new User("eve");

    when(storageHandler.getUser()).thenReturn(new User("adam"));
    when(caller.call(any(RemoteCallback.class))).then(new AuthServiceAnswer(expected));

    loadMethod.invoke(userCache);
    // Precondition
    assertEquals("adam", userCache.getUser().getLoginName());

    // Actual test
    rpcMethod.invoke(securityContext);

    assertEquals(expected, userCache.getUser());
    verify(storageHandler).setUser(expected);
  }

  @Test
  public void testRpcHappensWithNoStoredUser() throws Exception {
    final User expected = new User("adam");

    when(storageHandler.getUser()).thenReturn(null);
    when(caller.call(any(RemoteCallback.class))).then(new AuthServiceAnswer(expected));

    loadMethod.invoke(userCache);
    rpcMethod.invoke(securityContext);

    verify(caller).call(any(RemoteCallback.class));
    verify(storageHandler).setUser(expected);
    assertEquals(expected, userCache.getUser());
  }

  @Test
  public void testStorageDoesNotOverrideActiveUser() throws Exception {
    final User expected = new User("adam");

    when(storageHandler.getUser()).thenReturn(new User("eve"));

    userCache.setUser(expected);
    assertTrue(userCache.isValid());
    verify(storageHandler).setUser(expected);

    loadMethod.invoke(userCache);

    assertEquals(expected, userCache.getUser());
    verify(storageHandler, new Times(0)).getUser();
    verify(storageHandler).setUser(expected);
  }

  @Test
  public void testStorageWhenActiveUserSet() throws Exception {
    final User expected = new User("adam");
    userCache.setUser(expected);

    verify(storageHandler).setUser(expected);
  }

  @Test
  public void testStorageWhenNullUserSet() throws Exception {
    userCache.setUser(null);

    verify(storageHandler).setUser(null);
  }

  @Test
  public void testStorageRemovedWhenCacheInvalidated() throws Exception {
    userCache.invalidateCache();

    verify(storageHandler).setUser(null);
  }

}
