/*
 * Copyright 2002-2020 the original author or authors.
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

package org.springframework.beans.factory;

import org.springframework.lang.Nullable;

/**
 * Interface to be implemented by objects used within a {@link BeanFactory} which
 * are themselves factories for individual objects. If a bean implements this
 * interface, it is used as a factory for an object to expose, not directly as a
 * bean instance that will be exposed itself.
 *
 * 由 {@link BeanFactory} 中使用的对象实现的接口，这些对象本身是单个对象的工厂。
 * 如果一个 bean 实现了这个接口，它就被当成工厂用于传出对象，而不是作为 bean 实例传出自己。
 *
 *
 * <p><b>NB: A bean that implements this interface cannot be used as a normal bean.</b>
 * A FactoryBean is defined in a bean style, but the object exposed for bean
 * references ({@link #getObject()}) is always the object that it creates.
 *
 * 实现此接口的 bean 不能用作普通 bean。
 * FactoryBean 以 bean 样式定义，但通过 bean 依赖传出的对象始终是它创建的对象。
 *
 * <p>FactoryBeans can support singletons and prototypes, and can either create
 * objects lazily on demand or eagerly on startup. The {@link SmartFactoryBean}
 * interface allows for exposing more fine-grained behavioral metadata.
 *
 * FactoryBean 可以支持单例和原型，并且可以根据需要懒加载创建对象。
 * {@link SmartFactoryBean} 接口允许公开更细粒度的行为元数据。
 *
 * <p>This interface is heavily used within the framework itself, for example for
 * the AOP {@link org.springframework.aop.framework.ProxyFactoryBean} or the
 * {@link org.springframework.jndi.JndiObjectFactoryBean}. It can be used for
 * custom components as well; however, this is only common for infrastructure code.
 *
 * 该接口在框架本身中大量使用，例如用于 AOP
 * {@link org.springframework.aop.framework.ProxyFactoryBean}
 * 或 {@link org.springframework.jndi.JndiObjectFactoryBean}。
 * 它也可以用于自定义组件；然而，这仅适用于基础设施代码。
 *
 * 当某些对象的实例化过程过于繁琐，通过 XML 配置过于复杂，
 * 使我们宁愿使用 Java 代码来完成这个实例化过程的时候，
 * 或者，某些第三方库不能直接注册到 Spring 容器的时候，就可以实现这个接口，
 * 给出自己的对象实例化逻辑代码。
 *
 * <p><b>{@code FactoryBean} is a programmatic contract. Implementations are not
 * supposed to rely on annotation-driven injection or other reflective facilities.</b>
 * {@link #getObjectType()} {@link #getObject()} invocations may arrive early in the
 * bootstrap process, even ahead of any post-processor setup. If you need access to
 * other beans, implement {@link BeanFactoryAware} and obtain them programmatically.
 *
 * {@code FactoryBean} 是一个程序化合约，实现不应该依赖于注释驱动的注入或其他反射设施。
 * {@link #getObjectType()} {@link #getObject()} 调用可能会在引导过程的早期到达，
 * 甚至在任何后处理器设置之前。
 * 如果您需要访问其他 bean，请实现 {@link BeanFactoryAware} 并以编程方式获取它们。
 *
 * <p><b>The container is only responsible for managing the lifecycle of the FactoryBean
 * instance, not the lifecycle of the objects created by the FactoryBean.</b> Therefore,
 * a destroy method on an exposed bean object (such as {@link java.io.Closeable#close()}
 * will <i>not</i> be called automatically. Instead, a FactoryBean should implement
 * {@link DisposableBean} and delegate any such close call to the underlying object.
 *
 * 容器只负责管理 FactoryBean 实例的生命周期，而不是 FactoryBean 创建的对象的生命周期。
 * 因此，传出的 bean 对象上有一个 destroy 方法，
 * 例如 {@link java.io.Closeable#close() } 不会被自动调用。
 * 对应的，失去 close() 方法后，FactoryBean 应该实现 {@link DisposableBean}
 * 并将关闭调用委托给底层对象（去实现）。
 *
 * <p>Finally, FactoryBean objects participate in the containing BeanFactory's
 * synchronization of bean creation. There is usually no need for internal
 * synchronization other than for purposes of lazy initialization within the
 * FactoryBean itself (or the like).
 *
 * FactoryBean 对象参与包含 BeanFactory 的 bean 创建同步。
 * 除了 FactoryBean 本身（或类似的）内部的延迟初始化之外，通常不需要内部同步。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 08.03.2003
 * @param <T> the bean type
 * @see org.springframework.beans.factory.BeanFactory
 * @see org.springframework.aop.framework.ProxyFactoryBean
 * @see org.springframework.jndi.JndiObjectFactoryBean
 */
public interface FactoryBean<T> {

	/**
	 * The name of an attribute that can be
	 * {@link org.springframework.core.AttributeAccessor#setAttribute set} on a
	 * {@link org.springframework.beans.factory.config.BeanDefinition} so that
	 * factory beans can signal their object type when it can't be deduced from
	 * the factory bean class.
	 *
	 * 属性名称可以通过 {@link org.springframework.beans.factory.config.BeanDefinition}
	 * 上的 {@link org.springframework.core.AttributeAccessor#setAttribute set} 方法设置，
	 * 当无法通过 FactoryBean class 推导时，FactoryBean 可以标记（高亮）他们的对象类型。
	 *
	 * @since 5.2
	 */
	String OBJECT_TYPE_ATTRIBUTE = "factoryBeanObjectType";


	/**
	 *  返回该 FactoryBean 生产的对象实例。
	 *
	 * Return an instance (possibly shared or independent) of the object
	 * managed by this factory.
	 * <p>As with a {@link BeanFactory}, this allows support for both the
	 * Singleton and Prototype design pattern.
	 * <p>If this FactoryBean is not fully initialized yet at the time of
	 * the call (for example because it is involved in a circular reference),
	 * throw a corresponding {@link FactoryBeanNotInitializedException}.
	 * <p>As of Spring 2.0, FactoryBeans are allowed to return {@code null}
	 * objects. The factory will consider this as normal value to be used; it
	 * will not throw a FactoryBeanNotInitializedException in this case anymore.
	 * FactoryBean implementations are encouraged to throw
	 * FactoryBeanNotInitializedException themselves now, as appropriate.
	 *
	 * 返回此工厂管理的对象的实例（可能是共享的或独立的）。
	 * 与 {@link BeanFactory} 一样，同时支持单例和原型设计模式。
	 * 如果此 FactoryBean 在执行时尚未完全初始化调用（例如因为它涉及循环引用），
	 * 抛出相应的异常 {@link FactoryBeanNotInitializedException}。
	 *
	 * 从 Spring 2.0 开始，FactoryBeans 被允许返回 {@code null} 对象。
	 * 工厂会将此视为使用的正常值；在这种情况下它不会再抛出 FactoryBeanNotInitializedException 。
	 * 鼓励 FactoryBean 实现在适当的时候自己抛出 FactoryBeanNotInitializedException 。
	 *
	 * @return an instance of the bean (can be {@code null})
	 * @throws Exception in case of creation errors
	 * @see FactoryBeanNotInitializedException
	 */
	@Nullable
	T getObject() throws Exception;

	/**
	 * 返回 getOject() 方法返回对象的类型。
	 *
	 * Return the type of object that this FactoryBean creates,
	 * or {@code null} if not known in advance.
	 * <p>This allows one to check for specific types of beans without
	 * instantiating objects, for example on autowiring.
	 * <p>In the case of implementations that are creating a singleton object,
	 * this method should try to avoid singleton creation as far as possible;
	 * it should rather estimate the type in advance.
	 * For prototypes, returning a meaningful type here is advisable too.
	 * <p>This method can be called <i>before</i> this FactoryBean has
	 * been fully initialized. It must not rely on state created during
	 * initialization; of course, it can still use such state if available.
	 * <p><b>NOTE:</b> Autowiring will simply ignore FactoryBeans that return
	 * {@code null} here. Therefore it is highly recommended to implement
	 * this method properly, using the current state of the FactoryBean.
	 *
	 * 返回此 FactoryBean 创建的对象类型，如果无法确定，则返回 {@code null}。
	 * 这允许人们在不实例化对象的情况下检查 bean 的具体类型，例如在自动装配时。
	 *
	 * 此方法应尽量避免创建单例的情况。对于原型，也建议在此处返回有意义的类型。
	 *
	 * 可以在 FactoryBean 完全初始化之前调用此方法。
	 * 它不依赖于初始化期间创建的状态；当然，它仍然可以使用这种状态。
	 *
	 * 注意：自动装配将忽略返回 {@code null} 的 FactoryBean。
	 * 因此，强烈建议使用 FactoryBean 的当前状态正确实现此方法。
	 *
	 * @return the type of object that this FactoryBean creates,
	 * or {@code null} if not known at the time of the call
	 * @see ListableBeanFactory#getBeansOfType
	 */
	@Nullable
	Class<?> getObjectType();

	/**
	 * 表明工厂方法 getObject() 所生产的对象是否要以 singleton 形式存在于容器中。
	 *
	 * Is the object managed by this factory a singleton? That is,
	 * will {@link #getObject()} always return the same object
	 * (a reference that can be cached)?
	 * <p><b>NOTE:</b> If a FactoryBean indicates to hold a singleton object,
	 * the object returned from {@code getObject()} might get cached
	 * by the owning BeanFactory. Hence, do not return {@code true}
	 * unless the FactoryBean always exposes the same reference.
	 * <p>The singleton status of the FactoryBean itself will generally
	 * be provided by the owning BeanFactory; usually, it has to be
	 * defined as singleton there.
	 * <p><b>NOTE:</b> This method returning {@code false} does not
	 * necessarily indicate that returned objects are independent instances.
	 * An implementation of the extended {@link SmartFactoryBean} interface
	 * may explicitly indicate independent instances through its
	 * {@link SmartFactoryBean#isPrototype()} method. Plain {@link FactoryBean}
	 * implementations which do not implement this extended interface are
	 * simply assumed to always return independent instances if the
	 * {@code isSingleton()} implementation returns {@code false}.
	 * <p>The default implementation returns {@code true}, since a
	 * {@code FactoryBean} typically manages a singleton instance.
	 * @return whether the exposed object is a singleton
	 * @see #getObject()
	 * @see SmartFactoryBean#isPrototype()
	 */
	default boolean isSingleton() {
		return true;
	}

}
