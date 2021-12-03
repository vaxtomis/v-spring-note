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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Simple object instantiation strategy for use in a BeanFactory.
 *
 * 在 BeanFactory 中使用的简单对象实例化策略。
 *
 * <p>Does not support Method Injection, although it provides hooks for subclasses
 * to override to add Method Injection support, for example by overriding methods.
 *
 * 不支持方法注入，尽管它为子类提供钩子，使用重写来添加方法注入支持。例如可以重写方法。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 1.1
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {

	private static final ThreadLocal<Method> currentlyInvokedFactoryMethod = new ThreadLocal<>();


	/**
	 * Return the factory method currently being invoked or {@code null} if none.
	 * <p>Allows factory method implementations to determine whether the current
	 * caller is the container itself as opposed to user code.
	 *
	 * 返回当前正在调用的工厂方法或 {@code null}（如果没有）。
	 * 允许工厂方法实现确定当前调用者是容器本身而不是用户代码。
	 */
	@Nullable
	public static Method getCurrentlyInvokedFactoryMethod() {
		return currentlyInvokedFactoryMethod.get();
	}


	/**
	 * 实例化
	 * RootBeanDefinition -> extends AbstractBeanDefinition
	 * 这里是通过 Class 获取声明的无参构造方法
	 *
	 * @param bd the bean definition
	 * @param beanName the name of the bean when it is created in this context.
	 * @param owner the owning BeanFactory
	 * @return Object
	 */
	@Override
	public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		// Don't override the class with CGLIB if no overrides.
		// 如果 BeanDefinition 的 methodOverrides 为空（没有方法被覆盖）
		// -> methodOverrides 是一个 Set<MethodOverride>
		//     -> MethodOverride 定义了方法名，是否被覆盖，覆盖方法路径
		if (!bd.hasMethodOverrides()) {
			Constructor<?> constructorToUse;
			// 之所以要加锁，是因为后面的 doPrivileged 方法要求在单独线程内
			synchronized (bd.constructorArgumentLock) {
				// 拿到 BeanDefinition 可执行工厂方法（构造方法）的引用
				constructorToUse = (Constructor<?>) bd.resolvedConstructorOrFactoryMethod;
				if (constructorToUse == null) {
					// 获取 Class
					final Class<?> clazz = bd.getBeanClass();
					// 过滤掉接口（接口提供不了充分的信息）
					if (clazz.isInterface()) {
						throw new BeanInstantiationException(clazz, "Specified class is an interface");
					}
					try {
						if (System.getSecurityManager() != null) {
							/**
							 * AccessController.doPrivileged是一个在 AccessController 类中的静态方法，
							 * 允许在一个类实例中的代码通知这个 AccessController:它的代码主体是享受 "privileged(特权的)"，
							 * 访问控制器因此中断栈检查，后续的栈帧对操作的资源不论是否有权限都无关。
							 *
							 * 应用无法直接访问某些系统资源，但应用又必须得到这些资源才能完成功能。
							 * doPrivileged让程序突破当前域权限限制，临时扩大访问权限。
							 */
							constructorToUse = AccessController.doPrivileged(
									// 获取 Class 的 getDeclaredConstructor
									(PrivilegedExceptionAction<Constructor<?>>) clazz::getDeclaredConstructor);
						}
						else {
							constructorToUse = clazz.getDeclaredConstructor();
						}
						bd.resolvedConstructorOrFactoryMethod = constructorToUse;
					}
					catch (Throwable ex) {
						throw new BeanInstantiationException(clazz, "No default constructor found", ex);
					}
				}
			}
			return BeanUtils.instantiateClass(constructorToUse);
		}
		else {
			// Must generate CGLIB subclass.
			return instantiateWithMethodInjection(bd, beanName, owner);
		}
	}

	/**
	 * Subclasses can override this method, which is implemented to throw
	 * UnsupportedOperationException, if they can instantiate an object with
	 * the Method Injection specified in the given RootBeanDefinition.
	 * Instantiation should use a no-arg constructor.
	 *
	 * 如果子类可以通过给定的 RootBeanDefinition 指定的注入方法实例化对象，
	 * 子类就可以重写这个方法，并抛出 UnsupportedOperationException。
	 * 实例化应该使用无参数构造函数。
	 */
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner) {
		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}

	/**
	 * 实例化，额外添加了 构造方法 和 参数
	 * 基本同上，只是自动获取无参构造方法被替换成了 传入的构造方法
	 *
	 * @param bd the bean definition
	 * @param beanName the name of the bean when it is created in this context.
	 * @param owner the owning BeanFactory
	 * @param ctor the constructor to use
	 * @param args the constructor arguments to apply
	 * @return Object
	 */
	@Override
	public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			final Constructor<?> ctor, Object... args) {

		if (!bd.hasMethodOverrides()) {
			if (System.getSecurityManager() != null) {
				// use own privileged to change accessibility (when security is on)
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
					ReflectionUtils.makeAccessible(ctor);
					return null;
				});
			}
			return BeanUtils.instantiateClass(ctor, args);
		}
		else {
			return instantiateWithMethodInjection(bd, beanName, owner, ctor, args);
		}
	}

	/**
	 * Subclasses can override this method, which is implemented to throw
	 * UnsupportedOperationException, if they can instantiate an object with
	 * the Method Injection specified in the given RootBeanDefinition.
	 * Instantiation should use the given constructor and parameters.
	 *
	 * 如果子类可以使用给定 RootBeanDefinition 中指定的方法注入实例化对象，
	 * 则子类可以覆盖此方法，并抛出 UnsupportedOperationException。
	 * 实例化应该使用给定的构造函数和参数。
	 */
	protected Object instantiateWithMethodInjection(RootBeanDefinition bd, @Nullable String beanName,
			BeanFactory owner, @Nullable Constructor<?> ctor, Object... args) {

		throw new UnsupportedOperationException("Method Injection not supported in SimpleInstantiationStrategy");
	}

	/**
	 * 实例化
	 * 传入 工厂 Bean，工厂方法 和 参数
	 *
	 *
	 * @param bd the bean definition
	 * @param beanName the name of the bean when it is created in this context.
	 * @param owner the owning BeanFactory
	 * @param factoryBean the factory bean instance to call the factory method on,
	 * or {@code null} in case of a static factory method
	 * @param factoryMethod the factory method to use
	 * @param args the factory method arguments to apply
	 * @return Object
	 */
	@Override
	public Object instantiate(RootBeanDefinition bd, @Nullable String beanName, BeanFactory owner,
			@Nullable Object factoryBean, final Method factoryMethod, Object... args) {

		try {
			if (System.getSecurityManager() != null) {
				AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
					ReflectionUtils.makeAccessible(factoryMethod);
					return null;
				});
			}
			else {
				ReflectionUtils.makeAccessible(factoryMethod);
			}

			// 保存上一个工厂方法
			Method priorInvokedFactoryMethod = currentlyInvokedFactoryMethod.get();
			try {
				// 传入当前提供的工厂方法
				currentlyInvokedFactoryMethod.set(factoryMethod);
				// 通过 factoryMethod.invoke 来创建对象
				Object result = factoryMethod.invoke(factoryBean, args);
				if (result == null) {
					result = new NullBean();
				}
				return result;
			}
			finally {
				if (priorInvokedFactoryMethod != null) {
					// 还原
					currentlyInvokedFactoryMethod.set(priorInvokedFactoryMethod);
				}
				else {
					currentlyInvokedFactoryMethod.remove();
				}
			}
		}
		catch (IllegalArgumentException ex) {
			throw new BeanInstantiationException(factoryMethod,
					"Illegal arguments to factory method '" + factoryMethod.getName() + "'; " +
					"args: " + StringUtils.arrayToCommaDelimitedString(args), ex);
		}
		catch (IllegalAccessException ex) {
			throw new BeanInstantiationException(factoryMethod,
					"Cannot access factory method '" + factoryMethod.getName() + "'; is it public?", ex);
		}
		catch (InvocationTargetException ex) {
			String msg = "Factory method '" + factoryMethod.getName() + "' threw exception";
			if (bd.getFactoryBeanName() != null && owner instanceof ConfigurableBeanFactory &&
					((ConfigurableBeanFactory) owner).isCurrentlyInCreation(bd.getFactoryBeanName())) {
				msg = "Circular reference involving containing bean '" + bd.getFactoryBeanName() + "' - consider " +
						"declaring the factory method as static for independence from its containing instance. " + msg;
			}
			throw new BeanInstantiationException(factoryMethod, msg, ex.getTargetException());
		}
	}

}
