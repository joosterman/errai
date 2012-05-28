/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.databinding.client;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * This class provides access to the generated proxies for {@link Bindable} types.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class BindableProxyFactory {
  private static Map<Class<?>, BindableProxyProvider> bindableProxyProviders =
      new HashMap<Class<?>, BindableProxyProvider>();

  /**
   * Returns a proxy for the provided model instance. Changes to the proxy's state will result in updates on the widget
   * given the corresponding property was bound (see
   * {@link BindableProxy#bind(String, com.google.gwt.user.client.ui.HasValue)}).
   * 
   * @param <T>
   *          bindable type
   * @param model
   *          the model instance to proxy
   * @return proxy that can be used in place of the model instance.
   */
  public static <T> T getBindableProxy(T model) {
    BindableProxyProvider proxyProvider = getBindableProxyProvider(model.getClass());

    BindableProxy proxy = proxyProvider.getBindableProxy(model);
    if (proxy == null) {
      throw new RuntimeException("No proxy instance provided for bindable type: " + model.getClass().getName());
    }

    return (T) proxy;
  }

  /**
   * Returns a proxy for a newly created model instance of the provided type. Changes to the proxy's state will result
   * in updates on the widget given the corresponding property was bound (see
   * {@link BindableProxy#bind(String, com.google.gwt.user.client.ui.HasValue)}).
   * 
   * @param bindableType
   *          the bindable type
   * @return proxy that can be used in place of the model instance.
   */
  public static <T> T getBindableProxy(Class<?> bindableType) {
    BindableProxyProvider proxyProvider = getBindableProxyProvider(bindableType);

    BindableProxy proxy = proxyProvider.getBindableProxy();
    if (proxy == null) {
      throw new RuntimeException("No proxy instance provided for bindable type: " + bindableType.getName());
    }

    return (T) proxy;
  }

  private static BindableProxyProvider getBindableProxyProvider(Class<?> bindableType) {
    if (bindableProxyProviders.isEmpty()) {
      throw new RuntimeException("There are no proxy providers for bindable types registered yet.");
    }

    BindableProxyProvider proxyProvider = bindableProxyProviders.get(bindableType);
    if (proxyProvider == null) {
      throw new RuntimeException("No proxy provider found for bindable type:" + bindableType.getName());
    }

    return proxyProvider;
  }

  /**
   * Registers a generated bindable proxy. This method is called by the generated BindableProxyLoader.
   * 
   * @param proxyType
   *          bindable type, must not be null
   * @param proxyProvider
   *          the proxy provided for the generated bindable proxy, must not be null.
   */
  public static void addBindableProxy(Class<?> proxyType, BindableProxyProvider proxyProvider) {
    Assert.notNull(proxyType);
    Assert.notNull(proxyProvider);

    bindableProxyProviders.put(proxyType, proxyProvider);
  }
}