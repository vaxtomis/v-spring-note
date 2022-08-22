/*
 * Copyright 2002-2012 the original author or authors.
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

import org.springframework.beans.BeansException;

/**
 * Spring 框架提供了一个 BeanFactoryAware 接口，
 * 容器在实例化 实现了该接口的 bean 的过程会自动将容器本身注入该 bean，
 * 这样该 bean 就持有了它所处的 BeanFactory 的引用。 ——《Spring 揭秘》 P63
 *
 * <br><br>
 *
 * 持有 BeanFactory 之后就可以通过这个 BeanFactory 的引用去创建不同且独立的 Bean 对象
 *
 * <br><br>
 *
 * Interface to be implemented by beans that wish to be aware of their
 * owning {@link BeanFactory}.
 *
 * <br><br>
 *
 * 实现此接口，能感知到 Bean 所属的 BeanFactory
 *
 * <br><br>
 *
 * <p>For example, beans can look up collaborating beans via the factory
 * (Dependency Lookup). Note that most beans will choose to receive references
 * to collaborating beans via corresponding bean properties or constructor
 * arguments (Dependency Injection).
 *
 * <br><br>
 *
 * 例如，bean 可以通过工厂查找依赖的 bean (依赖查询)，
 * 请注意，大多数 bean 将选择通过相应的 bean 属性或构造函数参数接收依赖 bean (依赖注入)
 *
 * <br><br>
 *
 * <p>For a list of all bean lifecycle methods, see the
 * {@link BeanFactory BeanFactory javadocs}.
 *
 * @author Rod Johnson
 * @author Chris Beams
 * @since 11.03.2003
 * @see BeanNameAware
 * @see BeanClassLoaderAware
 * @see InitializingBean
 * @see org.springframework.context.ApplicationContextAware
 */
public interface BeanFactoryAware extends Aware {

	/**
	 * Callback that supplies the owning factory to a bean instance.
	 * <p>Invoked after the population of normal bean properties
	 * but before an initialization callback such as
	 * {@link InitializingBean#afterPropertiesSet()} or a custom init-method.
	 *
	 * <br><br>
	 *
	 * 将拥有工厂提供给 bean 实例的回调。
	 * 在填充普通 bean 属性之后但在初始化回调，
	 * 例如 {@link InitializingBean#afterPropertiesSet()} 或自定义初始化方法）之前调用。
	 *
	 * @param beanFactory owning BeanFactory (never {@code null}).
	 * The bean can immediately call methods on the factory.
	 * @throws BeansException in case of initialization errors
	 * @see BeanInitializationException
	 */
	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}
