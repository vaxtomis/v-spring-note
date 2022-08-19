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

package org.springframework.core.io;

import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

/**
 * 资源查找定位策略的统一抽象。具体的资源查找策略由相应实现类给出。<br><br>
 *
 * Strategy interface for loading resources (e.. class path or file system
 * resources). An {@link org.springframework.context.ApplicationContext}
 * is required to provide this functionality, plus extended
 * {@link org.springframework.core.io.support.ResourcePatternResolver} support.<br><br>
 *
 * 用于加载资源（例如类路径或文件系统资源）的策略接口。<br>
 * 需要 {@link org.springframework.context.ApplicationContext} 来提供此功能，
 * 以及扩展的 {@link org.springframework.core.io.support.ResourcePatternResolver} 支持。<br><br>
 *
 * <p>{@link DefaultResourceLoader} is a standalone implementation that is
 * usable outside an ApplicationContext, also used by {@link ResourceEditor}.<br><br>
 *
 * {@link DefaultResourceLoader} 是一个独立的实现，
 * 可以在 ApplicationContext 之外使用，也被 {@link ResourceEditor} 使用。<br><br>
 *
 * <p>Bean properties of type Resource and Resource array can be populated
 * from Strings when running in an ApplicationContext, using the particular
 * context's resource loading strategy.<br><br>
 *
 * 在 ApplicationContext 中运行时，
 * 可以使用特定上下文的资源加载策略从字符串填充类型为 Resource 和 Resource 数组的 Bean 属性。<br><br>
 *
 * @author Juergen Hoeller
 * @since 10.03.2004
 * @see Resource
 * @see org.springframework.core.io.support.ResourcePatternResolver
 * @see org.springframework.context.ApplicationContext
 * @see org.springframework.context.ResourceLoaderAware
 */
public interface ResourceLoader {

	/** Pseudo URL prefix for loading from the class path: "classpath:". */
	String CLASSPATH_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;


	/**
	 * Return a Resource handle for the specified resource location.
	 * <p>The handle should always be a reusable resource descriptor,
	 * allowing for multiple {@link Resource#getInputStream()} calls.
	 * <p><ul>
	 * <li>Must support fully qualified URLs, e.g. "file:C:/test.dat".
	 * <li>Must support classpath pseudo-URLs, e.g. "classpath:test.dat".
	 * <li>Should support relative file paths, e.g. "WEB-INF/test.dat".
	 * (This will be implementation-specific, typically provided by an
	 * ApplicationContext implementation.)
	 * </ul>
	 * <p>Note that a Resource handle does not imply an existing resource;
	 * you need to invoke {@link Resource#exists} to check for existence.
	 *
	 * 【核心功能】获取根据指定的资源位置，定位到具体的资源实例。
	 *
	 * 返回指定资源位置的资源句柄。句柄应该始终是一个可重用的资源描述符，
	 * 允许多个 {@link Resource#getInputStream()} 调用。
	 *
	 * > 必须支持完全限定的 URL，例如 "file:C:/test.dat"。
	 * > 必须支持类路径伪 URL，例如 "classpath:test.dat"。
	 * > 应该支持相对文件路径，例如 "WEB-INF/test.dat"。
	 *
	 * （这将是特定于实现的，通常由 ApplicationContext 实现提供。）
	 * 请注意，资源句柄并不意味着现有资源；您需要调用 {@link Resource#exists} 来检查是否存在。
	 *
	 * @param location the resource location
	 * @return a corresponding Resource handle (never {@code null})
	 * @see #CLASSPATH_URL_PREFIX
	 * @see Resource#exists()
	 * @see Resource#getInputStream()
	 */
	Resource getResource(String location);

	/**
	 * Expose the ClassLoader used by this ResourceLoader.
	 * <p>Clients which need to access the ClassLoader directly can do so
	 * in a uniform manner with the ResourceLoader, rather than relying
	 * on the thread context ClassLoader.
	 * @return the ClassLoader
	 * (only {@code null} if even the system ClassLoader isn't accessible)
	 *
	 * 暴露 ResourceLoader 使用的 ClassLoader。
	 * 需要直接访问 ClassLoader 的客户端可以通过 ResourceLoader 以统一的方式访问，
	 * 而不是依赖于线程上下文 ClassLoader。
	 *
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 * @see org.springframework.util.ClassUtils#forName(String, ClassLoader)
	 */
	@Nullable
	ClassLoader getClassLoader();

}
