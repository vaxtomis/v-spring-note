/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.Nullable;

/**
 * 常用的几个实现
 * {@link org.springframework.context.support.FileSystemXmlApplicationContext}
 * {@link org.springframework.context.support.ClassPathXmlApplicationContext}
 * {@link org.springframework.web.context.support.XmlWebApplicationContext}
 *
 * Central interface to provide configuration for an application.
 * This is read-only while the application is running, but may be
 * reloaded if the implementation supports this.<br><br>
 *
 * 为应用程序提供配置的中央接口。
 * ApplicationContext 在应用程序运行时是只读的，此接口的实现可能会重新加载。<br><br>
 *
 * <p>An ApplicationContext provides:
 * <ul>
 * <li>Bean factory methods for accessing application components.
 * Inherited from {@link org.springframework.beans.factory.ListableBeanFactory}.
 * <li>The ability to load file resources in a generic fashion.
 * Inherited from the {@link org.springframework.core.io.ResourceLoader} interface.
 * <li>The ability to publish events to registered listeners.
 * Inherited from the {@link ApplicationEventPublisher} interface.
 * <li>The ability to resolve messages, supporting internationalization.
 * Inherited from the {@link MessageSource} interface.
 * <li>Inheritance from a parent context. Definitions in a descendant context
 * will always take priority. This means, for example, that a single parent
 * context can be used by an entire web application, while each servlet has
 * its own child context that is independent of that of any other servlet.
 * </ul>
 *
 * <br><br>
 *
 * ApplicationContext 提供了：<br>
 * > 用于访问应用组件的 Bean 工厂方法。
 * 继承自接口 {@link org.springframework.beans.factory.ListableBeanFactory}<br>
 * > 以通用方式加载文件资源的能力。
 * 继承自接口 {@link org.springframework.core.io.ResourceLoader}<br>
 * > 能够将事件发布到注册的侦听器。
 * 继承自接口 {@link ApplicationEventPublisher}<br>
 * > 解析消息的能力，支持国际化（I18n）。
 * 继承自接口 {@link MessageSource}
 * 继承自父类上下文。后代上下文中的定义将始终优先（子中的定义大于父）。
 * 这意味着，单个父上下文可以被整个 Web 应用程序使用，
 * 而每个 servlet 都有自己的子上下文，该子上下文独立于任何其他 servlet 的子上下文。
 *
 *
 * <p>In addition to standard {@link org.springframework.beans.factory.BeanFactory}
 * lifecycle capabilities, ApplicationContext implementations detect and invoke
 * {@link ApplicationContextAware} beans as well as {@link ResourceLoaderAware},
 * {@link ApplicationEventPublisherAware} and {@link MessageSourceAware} beans.
 *
 * 除了标准 BeanFactory 的生命周期功能外，ApplicationContext 实现了检测和调用
 * {@link ApplicationContextAware}、{@link ResourceLoaderAware}
 * {@link ApplicationEventPublisherAware}、{@link MessageSourceAware}
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see ConfigurableApplicationContext
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.core.io.ResourceLoader
 */
public interface ApplicationContext extends EnvironmentCapable, ListableBeanFactory, HierarchicalBeanFactory,
		MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

	/**
	 * Return the unique id of this application context.
	 * @return the unique id of the context, or {@code null} if none
	 */
	@Nullable
	String getId();

	/**
	 * Return a name for the deployed application that this context belongs to.
	 * @return a name for the deployed application, or the empty String by default
	 */
	String getApplicationName();

	/**
	 * Return a friendly name for this context.
	 * @return a display name for this context (never {@code null})
	 */
	String getDisplayName();

	/**
	 * Return the timestamp when this context was first loaded.
	 * @return the timestamp (ms) when this context was first loaded
	 */
	long getStartupDate();

	/**
	 * Return the parent context, or {@code null} if there is no parent
	 * and this is the root of the context hierarchy.
	 * @return the parent context, or {@code null} if there is no parent
	 */
	@Nullable
	ApplicationContext getParent();

	/**
	 * Expose AutowireCapableBeanFactory functionality for this context.
	 * <p>This is not typically used by application code, except for the purpose of
	 * initializing bean instances that live outside of the application context,
	 * applying the Spring bean lifecycle (fully or partly) to them.
	 * <p>Alternatively, the internal BeanFactory exposed by the
	 * {@link ConfigurableApplicationContext} interface offers access to the
	 * {@link AutowireCapableBeanFactory} interface too. The present method mainly
	 * serves as a convenient, specific facility on the ApplicationContext interface.
	 * <p><b>NOTE: As of 4.2, this method will consistently throw IllegalStateException
	 * after the application context has been closed.</b> In current Spring Framework
	 * versions, only refreshable application contexts behave that way; as of 4.2,
	 * all application context implementations will be required to comply.
	 * @return the AutowireCapableBeanFactory for this context
	 * @throws IllegalStateException if the context does not support the
	 * {@link AutowireCapableBeanFactory} interface, or does not hold an
	 * autowire-capable bean factory yet (e.g. if {@code refresh()} has
	 * never been called), or if the context has been closed already
	 * @see ConfigurableApplicationContext#refresh()
	 * @see ConfigurableApplicationContext#getBeanFactory()
	 *
	 * 为当前 context 暴露 AutowireCapableBeanFactory 功能。
	 *
	 * 这通常不被应用程序代码使用。
	 * 除非，目的是初始化那些存在于应用程序上下文之外的 bean 实例，
	 * 将 Spring bean 生命周期（全部或部分）应用于它们。
	 * 或由 {@link ConfigurableApplicationContext} 接口公开的内部 BeanFactory
	 * 也提供对 {@link AutowireCapableBeanFactory} 接口的访问。
	 * 本方法主要作为 ApplicationContext 接口上的一个方便的、特定的工具。
	 *
	 * 注意：从 4.2 开始，此方法将在应用程序上下文关闭后始终抛出 IllegalStateException。
	 * 在当前的 Spring Framework 版本中，只有可刷新的应用程序上下文才会这样做；
	 * 从 4.2 开始，所有应用程序上下文实现都需要遵守。
	 *
	 */
	AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException;

}
