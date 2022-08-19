/*
 * Copyright 2002-2018 the original author or authors.
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

/**
 * 一切准备就绪后，容器会检查 singleton 类型的 bean 实例。查看是否实现了 DisposableBean 接口，
 * 或者其对应的 bean 定义是否通过 <bean> 的 destroy-method 属性指定了自定义的销毁方法。<br>
 * 如果是，会为该实例注册一个用于对象销毁的回调（Callback），以便在这些 singleton 类型的
 * 对象实例销毁之前，执行销毁逻辑。<br><br>
 *
 * 自定义的对象销毁逻辑不会马上执行。只有该对象实例不再被使用时才会执行相关自定义销毁逻辑。<br><br>
 * 【对于 BeanFactory】<br>
 * 我们需要告知容器，在哪个时间点来执行对象的销毁方法。<br>
 * 如果不能在正确的时机调用 destroySinletons()，所有实现了 DisposableBean 接口的
 * 对象实例或者声明了 destroy-method 的 bean 的对象实例，不会执行销毁逻辑。<br><br>
 *
 * 【对于 ApplicationContext】<br>
 * AbstractApplicationContext 提供了 registerShutdownHook() 方法，
 * 底层使用标准的 Runtime 类的 addShutdownHook() 调用相应 bean 对象的销毁逻辑，
 * 从而保证在 JVM 退出之前，singleton 类型的 bean 对象实例的自定义销毁逻辑会执行。<br><br>
 *
 * Interface to be implemented by beans that want to release resources on destruction.
 * A {@link BeanFactory} will invoke the destroy method on individual destruction of a
 * scoped bean. An {@link org.springframework.context.ApplicationContext} is supposed
 * to dispose all of its singletons on shutdown, driven by the application lifecycle.<br><br>
 *
 * 想要在销毁时释放资源的 bean 实现的接口。<br>
 * {@link BeanFactory} 将在单独销毁作用域 bean 时调用本接口 destroy() 方法。<br>
 * {@link org.springframework.context.ApplicationContext} 应该在关闭时处理其所有单例，
 * 由应用程序生命周期驱动。<br><br>
 *
 * <p>A Spring-managed bean may also implement Java's {@link AutoCloseable} interface
 * for the same purpose. An alternative to implementing an interface is specifying a
 * custom destroy method, for example in an XML bean definition. For a list of all
 * bean lifecycle methods, see the {@link BeanFactory BeanFactory javadocs}.<br><br>
 *
 * 出于同样的目的，Spring 管理的 bean 也可以实现 Java 的 {@link AutoCloseable} 接口。<br>
 * 作为一种可以指定自定义销毁方法的替代，例如在 XML BeanDefinition 中。<br>
 * 有关所有 bean 生命周期方法的列表，请参阅 {@link BeanFactory BeanFactory javadocs}。<br><br>
 *
 * @author Juergen Hoeller
 * @since 12.08.2003
 * @see InitializingBean
 * @see org.springframework.beans.factory.support.RootBeanDefinition#getDestroyMethodName()
 * @see org.springframework.beans.factory.config.ConfigurableBeanFactory#destroySingletons()
 * @see org.springframework.context.ConfigurableApplicationContext#close()
 */
public interface DisposableBean {

	/**
	 * Invoked by the containing {@code BeanFactory} on destruction of a bean.
	 *
	 * 在销毁 bean 时由 BeanFactory 调用。
	 *
	 * @throws Exception in case of shutdown errors. Exceptions will get logged
	 * but not rethrown to allow other beans to release their resources as well.
	 */
	void destroy() throws Exception;

}
