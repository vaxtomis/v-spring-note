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
 * 容器内部广泛使用的一个对象声明周期标识接口。
 * 作用在于，在对象实例化过程调用 "BeanPostProcessor的前置处理" 后，
 * 会接着检测当前对象是否实现了 InitializingBean 接口。
 * 如果是，则会调用其 afterPropertiesSet() 方法进一步调整对象实例的状态。
 * 比如，有些情况下，某个业务对象实例化完成后还不能处于可使用状态，
 * 此时可以让该业务对象实现该接口，在方法 afterPropertiesSet() 中完成后续处理。<br><br>
 *
 * 注意，此接口虽然在 Spring 内部广泛应用，但是业务代码中使用侵入性较强，<br>
 * 推荐使用 init-method 属性。<br><br>
 *
 * Interface to be implemented by beans that need to react once all their properties
 * have been set by a {@link BeanFactory}: e.g. to perform custom initialization,
 * or merely to check that all mandatory properties have been set.<br><br>
 *
 * 本接口需要被那些，需要在它们所有属性被 BeanFactory 设置后响应的 bean 实现：
 * 例如执行自定义初始化，或仅检查是否已设置所有必需属性。<br><br>
 *
 * <p>An alternative to implementing {@code InitializingBean} is specifying a custom
 * init method, for example in an XML bean definition. For a list of all bean
 * lifecycle methods, see the {@link BeanFactory BeanFactory javadocs}.<br><br>
 *
 * 一种实现 InitializingBean 的替代方法是指定自定义 init 方法，如在 XML bean 定义中。
 * 有关所有 bean 生命周期方法的列表，请参阅 {@link BeanFactory BeanFactory javadocs}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see DisposableBean
 * @see org.springframework.beans.factory.config.BeanDefinition#getPropertyValues()
 * @see org.springframework.beans.factory.support.AbstractBeanDefinition#getInitMethodName()
 */
public interface InitializingBean {

	/**
	 * Invoked by the containing {@code BeanFactory} after it has set all bean properties
	 * and satisfied {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
	 * <p>This method allows the bean instance to perform validation of its overall
	 * configuration and final initialization when all bean properties have been set.
	 * @throws Exception in the event of misconfiguration (such as failure to set an
	 * essential property) or if initialization fails for any other reason
	 *
	 * 由包含 BeanFactory 在设置所有 bean 属性并满足 {@link BeanFactoryAware}、
	 * ApplicationContextAware 等之后调用。
	 * 此方法允许 bean 实例在设置所有 bean 属性后执行其整体配置和最终初始化的验证。
	 */
	void afterPropertiesSet() throws Exception;
}
