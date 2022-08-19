/*
 * Copyright 2002-2019 the original author or authors.
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

package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.lang.Nullable;

/**
 * BeanPostProcessor 的概念容易和 BeanFactoryPostProcessor 的概念混淆。<br>
 * 区别在于：<br>
 * BeanPostProcessor 是存在于对象实例化阶段，<br>
 * BeanFactoryPostProcessor 存在于容器启动阶段。<br>
 * {@link BeanFactoryPostProcessor}
 * 调用 ConfigurableBeanFactory 的 addBeanPostProcessor() 方法
 * 注册自定义 BeanPostProcessor。
 * 对于 ApplicationContext，则将实现类通过通常 XML 配置即可（自动识别并加载注册）
 * 在下方 Registration 有提到。<br><br>
 *
 * Factory hook that allows for custom modification of new bean instances &mdash;
 * for example, checking for marker interfaces or wrapping beans with proxies.<br><br>
 *
 * 允许自定义修改新创建 bean 实例的工厂钩子 —— 例如，检查标记接口或用代理包装 bean。<br><br>
 *
 * <p>Typically, post-processors that populate beans via marker interfaces
 * or the like will implement {@link #postProcessBeforeInitialization},
 * while post-processors that wrap beans with proxies will normally
 * implement {@link #postProcessAfterInitialization}.<br><br>
 *
 * 通常，通过标记接口等填充 bean 的后处理器将实现 {@link #postProcessBeforeInitialization}，
 * 而使用代理包装 bean 的后处理器通常将实现 {@link #postProcessAfterInitialization}。<br><br>
 *
 * <h3>Registration</h3>
 * <p>An {@code ApplicationContext} can autodetect {@code BeanPostProcessor} beans
 * in its bean definitions and apply those post-processors to any beans subsequently
 * created. A plain {@code BeanFactory} allows for programmatic registration of
 * post-processors, applying them to all beans created through the bean factory.
 * <br><br>
 *
 * 注册：<br>
 * ApplicationContext 可以在其 bean 定义中自动检测 BeanPostProcessor bean，
 * 并将这些后处理器应用于随后新建的任何 bean。
 * 一个普通的 BeanFactory 允许以编程方式注册后处理器，
 * 将它们应用于通过 BeanFactory 创建的所有 bean。<br><br>
 *
 * <h3>Ordering</h3>
 * <p>{@code BeanPostProcessor} beans that are autodetected in an
 * {@code ApplicationContext} will be ordered according to
 * {@link org.springframework.core.PriorityOrdered} and
 * {@link org.springframework.core.Ordered} semantics. In contrast,
 * {@code BeanPostProcessor} beans that are registered programmatically with a
 * {@code BeanFactory} will be applied in the order of registration; any ordering
 * semantics expressed through implementing the
 * {@code PriorityOrdered} or {@code Ordered} interface will be ignored for
 * programmatically registered post-processors. Furthermore, the
 * {@link org.springframework.core.annotation.Order @Order} annotation is not
 * taken into account for {@code BeanPostProcessor} beans.
 * <br><br>
 * 顺序：<br>
 * 在 ApplicationContext 中自动检测的 BeanPostProcessor bean，
 * 会根据 PriorityOrdered 和 Ordered 语义进行排序。
 * 相比之下，以编程方式注册到 BeanFactory 的 BeanPostProcessor bean 将按注册顺序应用；
 * 对于以编程方式注册的后处理器，通过实现 PriorityOrdered 或 Ordered 接口表达的任何排序语义都将被忽略。
 * 此外，@Order 注释不会被 BeanPostProcessor bean 考虑在内。<br><br>
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 10.10.2003
 * @see InstantiationAwareBeanPostProcessor
 * @see DestructionAwareBeanPostProcessor
 * @see ConfigurableBeanFactory#addBeanPostProcessor
 * @see BeanFactoryPostProcessor
 */
public interface BeanPostProcessor {

	/**
	 * ApplicationContext 对应的 Aware 接口就是通过 BeanPostProcessor 的方式处理的。
	 * ApplicationContext 中每个对象的实例化走到 BeanPostProcessor 前置处理这一步时，
	 * 会检测到之前注册到容器的 ApplicationContextAwareProcessor 这个 BeanPostProcessor 实现类
	 * 然后调用其 postProcessBeforeInitialization() 方法
	 * 检测并设置 Aware 相关依赖。
	 *
	 * postProcessBeforeInitialization() 方法可以对当前对象实例做更多的处理。
	 * 比如替换当前对象实例或者字节码增强当前对象实例。Spring 的 AOP 更多地使用
	 * BeanPostProcessor 来为对象生成相应的代理对象。
	 *
	 * Apply this {@code BeanPostProcessor} to the given new bean instance <i>before</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 *
	 * 在 bean 初始化回调（如 InitializingBean 的 afterPropertiesSet 或自定义初始化方法）之前，
	 * 将此 BeanPostProcessor 应用于创建的新 bean 实例。
	 * bean 将被填充属性值。返回的 bean 实例可能是原始实例的 Wrapper。
	 * 默认实现按原样返回给定的 bean。
	 *
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 */
	@Nullable
	default Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	/**
	 * Apply this {@code BeanPostProcessor} to the given new bean instance <i>after</i> any bean
	 * initialization callbacks (like InitializingBean's {@code afterPropertiesSet}
	 * or a custom init-method). The bean will already be populated with property values.
	 * The returned bean instance may be a wrapper around the original.
	 * <p>In case of a FactoryBean, this callback will be invoked for both the FactoryBean
	 * instance and the objects created by the FactoryBean (as of Spring 2.0). The
	 * post-processor can decide whether to apply to either the FactoryBean or created
	 * objects or both through corresponding {@code bean instanceof FactoryBean} checks.
	 * <p>This callback will also be invoked after a short-circuiting triggered by a
	 * {@link InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation} method,
	 * in contrast to all other {@code BeanPostProcessor} callbacks.
	 * <p>The default implementation returns the given {@code bean} as-is.
	 *
	 * 同上，不过改为在 bean 初始化回调之后。
	 *
	 * @param bean the new bean instance
	 * @param beanName the name of the bean
	 * @return the bean instance to use, either the original or a wrapped one;
	 * if {@code null}, no subsequent BeanPostProcessors will be invoked
	 * @throws org.springframework.beans.BeansException in case of errors
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet
	 * @see org.springframework.beans.factory.FactoryBean
	 */
	@Nullable
	default Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

}
