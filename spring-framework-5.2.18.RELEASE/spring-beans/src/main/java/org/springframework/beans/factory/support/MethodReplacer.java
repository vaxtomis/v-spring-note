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

package org.springframework.beans.factory.support;

import java.lang.reflect.Method;

/**
 * 顾名思义，用作方法替换的顶层接口。
 *
 * Interface to be implemented by classes that can reimplement any method
 * on an IoC-managed object: the <b>Method Injection</b> form of
 * Dependency Injection.
 *
 * 实现该接口的类可以 在受 IOC 管理的对象上重新构建任何方法。（依赖注入 -> 方法注入）
 *
 * <p>Such methods may be (but need not be) abstract, in which case the
 * container will create a concrete subclass to instantiate.
 *
 * 这些方法可能（但不一定）是抽象的，在这种情况下，容器将创建一个具体的子类来实例化。
 *
 * 通过此方式引入的执行效率并不高（因为是方法替换 而不是 AOP）。
 *
 * @author Rod Johnson
 * @since 1.1
 */
public interface MethodReplacer {

	/**
	 * Reimplement the given method.
	 * @param obj the instance we're reimplementing the method for
	 * @param method the method to reimplement
	 * @param args arguments to the method
	 * @return return value for the method
	 */
	Object reimplement(Object obj, Method method, Object[] args) throws Throwable;

}
