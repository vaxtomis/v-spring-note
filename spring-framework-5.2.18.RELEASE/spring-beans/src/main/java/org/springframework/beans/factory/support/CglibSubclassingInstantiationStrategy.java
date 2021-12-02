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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cglib.core.ClassLoaderAwareGeneratorStrategy;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.CallbackFilter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.cglib.proxy.NoOp;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Default object instantiation strategy for use in BeanFactories.
 *
 * 使用 BeanFactory 的默认对象实例化策略。
 *
 * <p>Uses CGLIB to generate subclasses dynamically if methods need to be
 * overridden by the container to implement <em>Method Injection</em>.
 *
 * 为了实现方法注入，方法需要被容器重写。
 * 使用 CGLIB 来生成动态子类（也就是通过创建子类，并重写方法来代理）。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.1
 */
public class CglibSubclassingInstantiationStrategy extends SimpleInstantiationStrategy {

	/**
	 * Index in the CGLIB callback array for passthrough behavior,
	 * in which case the subclass won't override the original class.
	 *
	 * 代理类直接调用被代理的方法不进行拦截。
	 * 表示传递操作（类似直接穿过子类进入原始类，不进行覆盖）。
	 *
	 */
	private static final int PASSTHROUGH = 0;

	/**
	 * Index in the CGLIB callback array for a method that should
	 * be overridden to provide <em>method lookup</em>.
	 *
	 * 方法应该被覆盖以提供方法查找。
	 */
	private static final int LOOKUP_OVERRIDE = 1;

	/**
	 * Index in the CGLIB callback array for a method that should
	 * be overridden using generic <em>method replacer</em> functionality.
	 *
	 * 方法应该被 泛型 Method Replacer 覆盖。
	 */
	private static final int METHOD_REPLACER = 2;

	// 无参 or 默认构造方法
	@Override
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		return instantiateWithMethodInjection(bd, beanName, owner, null);
	}

	// 带参数构造方法
	@Override
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			@Nullable Constructor<?> ctor, Object... args) {

		// Must generate CGLIB subclass...
		// 必须生成 CGLIB 子类
		return new CglibSubclassCreator(bd, owner).instantiate(ctor, args);
	}


	/**
	 * An inner class created for historical reasons to avoid external CGLIB dependency
	 * in Spring versions earlier than 3.2.
	 *
	 * 出于历史原因创建的内部类，以避免在早于 3.2 的 Spring 版本中对 CGLIB 产生外部依赖。
	 */
	private static class CglibSubclassCreator {

		private static final Class<?>[] CALLBACK_TYPES = new Class<?>[]
				{NoOp.class, LookupOverrideMethodInterceptor.class, ReplaceOverrideMethodInterceptor.class};

		private final RootBeanDefinition beanDefinition;

		private final BeanFactory owner;

		CglibSubclassCreator(RootBeanDefinition beanDefinition, BeanFactory owner) {
			this.beanDefinition = beanDefinition;
			this.owner = owner;
		}

		/**
		 * Create a new instance of a dynamically generated subclass implementing the
		 * required lookups.
		 *
		 * 创建动态生成的子类的新实例，实现所需的查找。
		 *
		 * @param ctor constructor to use. If this is {@code null}, use the
		 * no-arg constructor (no parameterization, or Setter Injection)
		 * @param args arguments to use for the constructor.
		 * Ignored if the {@code ctor} parameter is {@code null}.
		 * @return new instance of the dynamically generated subclass
		 */
		public Object instantiate(@Nullable Constructor<?> ctor, Object... args) {
			Class<?> subclass = createEnhancedSubclass(this.beanDefinition);
			Object instance;
			// 构造方法为空，默认无参构造
			if (ctor == null) {
				instance = BeanUtils.instantiateClass(subclass);
			}
			else {
				try {
					// 获取增强后的 Class 的 构造方法，并生成实例
					Constructor<?> enhancedSubclassConstructor = subclass.getConstructor(ctor.getParameterTypes());
					instance = enhancedSubclassConstructor.newInstance(args);
				}
				catch (Exception ex) {
					throw new BeanInstantiationException(this.beanDefinition.getBeanClass(),
							"Failed to invoke constructor for CGLIB enhanced subclass [" + subclass.getName() + "]", ex);
				}
			}
			// SPR-10785: set callbacks directly on the instance instead of in the
			// enhanced class (via the Enhancer) in order to avoid memory leaks.
			// 直接在实例上而不是在增强类中（通过增强器）设置回调以避免内存泄漏。
			/**
			 * CGLIB Factory 接口：
			 * Enhancer 类返回的所有增强实例都实现了这个接口。
			 * 将此接口用于新实例比通过 Enhance 接口或使用反射更快。
			 * 此外，要拦截在对象构造期间调用的方法，您必须使用这些方法而不是反射。
			 */
			Factory factory = (Factory) instance;
			factory.setCallbacks(new Callback[] {NoOp.INSTANCE,
					new LookupOverrideMethodInterceptor(this.beanDefinition, this.owner),
					new ReplaceOverrideMethodInterceptor(this.beanDefinition, this.owner)});
			return instance;
		}

		/**
		 * Create an enhanced subclass of the bean class for the provided bean
		 * definition, using CGLIB.
		 *
		 * 通过 CGLIB 创建 Bean 类的增强子类（Class）来提供 BeanDefinition。
		 */
		private Class<?> createEnhancedSubclass(RootBeanDefinition beanDefinition) {
			// Enhancer (CGLIB) 创建
			Enhancer enhancer = new Enhancer();
			// 设定父类
			enhancer.setSuperclass(beanDefinition.getBeanClass());
			// 设定命名策略
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			// 如果 BeanFactory 是实现 ConfigurableBeanFactory 的实例
			// 获取它的 ClassLoader，然后设定 ClassLoaderAwareGeneratorStrategy
			if (this.owner instanceof ConfigurableBeanFactory) {
				ClassLoader cl = ((ConfigurableBeanFactory) this.owner).getBeanClassLoader();
				enhancer.setStrategy(new ClassLoaderAwareGeneratorStrategy(cl));
			}
			// 设定回调过滤器过滤器（对于 BeanDefinition）
			enhancer.setCallbackFilter(new MethodOverrideCallbackFilter(beanDefinition));
			enhancer.setCallbackTypes(CALLBACK_TYPES);
			return enhancer.createClass();
		}
	}


	/**
	 * Class providing hashCode and equals methods required by CGLIB to
	 * ensure that CGLIB doesn't generate a distinct class per bean.
	 * Identity is based on class and bean definition.
	 *
	 * 类提供 CGLIB 所需的 hashCode 和 equals 方法，以确保 CGLIB 不会为每个 bean 生成不同的类。
	 * 标识基于类和 bean 定义。
	 */
	private static class CglibIdentitySupport {

		private final RootBeanDefinition beanDefinition;

		public CglibIdentitySupport(RootBeanDefinition beanDefinition) {
			this.beanDefinition = beanDefinition;
		}

		public RootBeanDefinition getBeanDefinition() {
			return this.beanDefinition;
		}

		@Override
		public boolean equals(@Nullable Object other) {
			return (other != null && getClass() == other.getClass() &&
					this.beanDefinition.equals(((CglibIdentitySupport) other).beanDefinition));
		}

		@Override
		public int hashCode() {
			return this.beanDefinition.hashCode();
		}
	}


	/**
	 * CGLIB callback for filtering method interception behavior.
	 *
	 * 回调 用于 过滤方法拦截行为。
	 */
	private static class MethodOverrideCallbackFilter extends CglibIdentitySupport implements CallbackFilter {

		private static final Log logger = LogFactory.getLog(MethodOverrideCallbackFilter.class);

		public MethodOverrideCallbackFilter(RootBeanDefinition beanDefinition) {
			super(beanDefinition);
		}

		@Override
		public int accept(Method method) {
			MethodOverride methodOverride = getBeanDefinition().getMethodOverrides().getOverride(method);
			if (logger.isTraceEnabled()) {
				logger.trace("MethodOverride for " + method + ": " + methodOverride);
			}
			// 没有重写方法，直接穿透
			if (methodOverride == null) {
				return PASSTHROUGH;
			}
			else if (methodOverride instanceof LookupOverride) {
				return LOOKUP_OVERRIDE;
			}
			else if (methodOverride instanceof ReplaceOverride) {
				return METHOD_REPLACER;
			}
			throw new UnsupportedOperationException("Unexpected MethodOverride subclass: " +
					methodOverride.getClass().getName());
		}
	}


	/**
	 * CGLIB MethodInterceptor to override methods, replacing them with an
	 * implementation that returns a bean looked up in the container.
	 *
	 * CGLIB MethodInterceptor 覆盖方法，用容器中查找到的 Bean 实现去替换。
	 */
	private static class LookupOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

		private final BeanFactory owner;

		public LookupOverrideMethodInterceptor(RootBeanDefinition beanDefinition, BeanFactory owner) {
			super(beanDefinition);
			this.owner = owner;
		}

		/**
		 * Lookup 的 cglib 方法拦截。
		 * 首先我们想要使用 lookup-method 方法注入，会指定两个信息：
		 * 1. name 注入的方法名
		 * 2. bean 需要注入的对象
		 * 从 BeanDefinition 的 MethodOverrides 匹配方法
		 * 匹配到 LookupOverride，则能获取到 beanName
		 * 从而通过 beanName 在 BeanFactory 中拿到 bean 实例。
		 */
		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
			// Cast is safe, as CallbackFilter filters are used selectively.
			// 传入给定方法，从当前 BeanDefinition 的 MethodOverrides 里去匹配有没有对应覆盖的方法
			LookupOverride lo = (LookupOverride) getBeanDefinition().getMethodOverrides().getOverride(method);
			Assert.state(lo != null, "LookupOverride not found");
			Object[] argsToUse = (args.length > 0 ? args : null);  // if no-arg, don't insist on args at all
			if (StringUtils.hasText(lo.getBeanName())) {
				// 通过 beanName 去获取一个 bean 的实例
				Object bean = (argsToUse != null ? this.owner.getBean(lo.getBeanName(), argsToUse) :
						this.owner.getBean(lo.getBeanName()));
				// Detect package-protected NullBean instance through equals(null) check
				return (bean.equals(null) ? null : bean);
			}
			else {
				return (argsToUse != null ? this.owner.getBean(method.getReturnType(), argsToUse) :
						this.owner.getBean(method.getReturnType()));
			}
		}
	}


	/**
	 * CGLIB MethodInterceptor to override methods, replacing them with a call
	 * to a generic MethodReplacer.
	 *
	 * CGLIB MethodInterceptor 覆盖方法，用泛型 MethodReplacer 的调用去替换。
	 */
	private static class ReplaceOverrideMethodInterceptor extends CglibIdentitySupport implements MethodInterceptor {

		private final BeanFactory owner;

		public ReplaceOverrideMethodInterceptor(RootBeanDefinition beanDefinition, BeanFactory owner) {
			super(beanDefinition);
			this.owner = owner;
		}

		/**
		 * 方法替换 的 cglib 方法拦截内。
		 *
		 * Replace Method 中指定了：
		 * 1. name 替换方法名
		 * 2. replacer MethodReplacer 对象名
		 * 同样从 BeanDefinition 的 MethodOverrides 匹配方法
		 * 匹配到 ReplaceOverride
		 * 然后直接通过 BeanFactory 去取 MethodReplacer
		 * 调用 MethodReplacer 的 reimplement 来创建
		 */
		@Override
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy mp) throws Throwable {
			ReplaceOverride ro = (ReplaceOverride) getBeanDefinition().getMethodOverrides().getOverride(method);
			Assert.state(ro != null, "ReplaceOverride not found");
			// TODO could cache if a singleton for minor performance optimization
			MethodReplacer mr = this.owner.getBean(ro.getMethodReplacerBeanName(), MethodReplacer.class);
			return mr.reimplement(obj, method, args);
		}
	}

}
