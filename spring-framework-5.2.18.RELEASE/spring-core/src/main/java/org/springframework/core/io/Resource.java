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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.springframework.lang.Nullable;

/**
 * Resource 接口可以根据资源的不同类型，或者资源所处的不同场合，给出相应的具体实现。
 * Spring 在此基础上提供了实现类：
 * {@link ByteArrayResource}：
 * 将字节数组提供的数据作为一种资源进行封装，使用 InputStream 形式访问该类型的资源，
 * 该实现会根据字节数组的数据，构造相应的 ByteArrayInputStream 并返回。
 *
 * {@link ClassPathResource}：
 * 从 ClassPath 中加载具体资源并进行封装。使用 ClassLoader 或者给定的类加载。
 *
 * {@link FileSystemResource}
 * 对 java.io.File 类型的封装，我们可以以文件或者 URL 的形式对该类型资源进行访问。
 *
 * {@link UrlResource}
 * 通过 java.net.URL 进行的具体资源查找定位的实现类，内部委派 URL 进行具体的资源操作。
 *
 * {@link InputStreamResource}
 * 将给定的 InputStream 视为一种资源的 Resource 实现类。
 *
 * Interface for a resource descriptor that abstracts from the actual
 * type of underlying resource, such as a file or class path resource.
 *
 * 从底层资源的实际类型（例如文件或类路径资源）中抽象出来的资源描述符的接口。
 *
 * <p>An InputStream can be opened for every resource if it exists in
 * physical form, but a URL or File handle can just be returned for
 * certain resources. The actual behavior is implementation-specific.
 *
 * 如果 InputStream 以物理形式存在，则可以为每个资源打开它，但只能为某些资源返回 URL 或文件句柄。
 * Resource 的具体操作是基于不同的实现的。
 *
 * @author Juergen Hoeller
 * @since 28.12.2003
 * @see #getInputStream()
 * @see #getURL()
 * @see #getURI()
 * @see #getFile()
 * @see WritableResource
 * @see ContextResource
 * @see UrlResource
 * @see FileUrlResource
 * @see FileSystemResource
 * @see ClassPathResource
 * @see ByteArrayResource
 * @see InputStreamResource
 */
public interface Resource extends InputStreamSource {

	/**
	 * Determine whether this resource actually exists in physical form.
	 * <p>This method performs a definitive existence check, whereas the
	 * existence of a {@code Resource} handle only guarantees a valid
	 * descriptor handle.
	 *
	 * 确定该资源是否以物理形式实际存在。
	 * 此方法执行明确的存在检查，而 Resource 句柄的存在仅保证有效的描述符句柄。
	 */
	boolean exists();

	/**
	 * Indicate whether non-empty contents of this resource can be read via
	 * {@link #getInputStream()}.
	 * <p>Will be {@code true} for typical resource descriptors that exist
	 * since it strictly implies {@link #exists()} semantics as of 5.1.
	 * Note that actual content reading may still fail when attempted.
	 * However, a value of {@code false} is a definitive indication
	 * that the resource content cannot be read.
	 *
	 * 指示是否可以通过 {@link #getInputStream()} 读取此资源的非空内容。
	 * 对于存在的典型资源描述符，将是 true，因为它严格暗示了 5.1 的 {@link #exists()} 语义。
	 * 请注意，尝试实际阅读内容时可能仍会失败（返回为 true 不代表读取一定能成功）。
	 * 但 false 值明确表示无法读取资源内容。
	 *
	 * @see #getInputStream()
	 * @see #exists()
	 */
	default boolean isReadable() {
		return exists();
	}

	/**
	 * Indicate whether this resource represents a handle with an open stream.
	 * If {@code true}, the InputStream cannot be read multiple times,
	 * and must be read and closed to avoid resource leaks.
	 * <p>Will be {@code false} for typical resource descriptors.
	 *
	 * 指示此资源是否表示具有打开流的句柄。
	 * 如果 true，则 InputStream 不能被多次读取，必须读取并关闭以避免资源泄漏。
	 * 对于典型的资源描述符将是 false。
	 */
	default boolean isOpen() {
		return false;
	}

	/**
	 * Determine whether this resource represents a file in a file system.
	 * A value of {@code true} strongly suggests (but does not guarantee)
	 * that a {@link #getFile()} call will succeed.
	 * <p>This is conservatively {@code false} by default.
	 *
	 * 确定此资源是否代表文件系统中的文件。
	 * true 值强烈暗示（但不保证）{@link #getFile()} 调用会成功。
	 * 默认情况下为 false。
	 *
	 * @since 5.0
	 * @see #getFile()
	 */
	default boolean isFile() {
		return false;
	}

	/**
	 * Return a URL handle for this resource.
	 *
	 * 返回此资源的 URL 句柄。
	 *
	 * @throws IOException if the resource cannot be resolved as URL,
	 * i.e. if the resource is not available as descriptor
	 */
	URL getURL() throws IOException;

	/**
	 * Return a URI handle for this resource.
	 *
	 * 返回此资源的 URI 句柄。
	 *
	 * @throws IOException if the resource cannot be resolved as URI,
	 * i.e. if the resource is not available as descriptor
	 * @since 2.5
	 */
	URI getURI() throws IOException;

	/**
	 * Return a File handle for this resource.
	 *
	 * 返回此资源的文件句柄。
	 *
	 * @throws java.io.FileNotFoundException if the resource cannot be resolved as
	 * absolute file path, i.e. if the resource is not available in a file system
	 * @throws IOException in case of general resolution/reading failures
	 * @see #getInputStream()
	 */
	File getFile() throws IOException;

	/**
	 * Return a {@link ReadableByteChannel}.
	 * <p>It is expected that each call creates a <i>fresh</i> channel.
	 * <p>The default implementation returns {@link Channels#newChannel(InputStream)}
	 * with the result of {@link #getInputStream()}.
	 *
	 * 预计每次调用都会创建一个新通道。
	 * 默认实现返回 {@link Channels#newChannel(InputStream)}
	 * 和 {@link #getInputStream()} 的结果。
	 *
	 * @return the byte channel for the underlying resource (must not be {@code null})
	 * @throws java.io.FileNotFoundException if the underlying resource doesn't exist
	 * @throws IOException if the content channel could not be opened
	 * @since 5.0
	 * @see #getInputStream()
	 */
	default ReadableByteChannel readableChannel() throws IOException {
		return Channels.newChannel(getInputStream());
	}

	/**
	 * Determine the content length for this resource.
	 *
	 * 确认资源的内容长度。
	 *
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 */
	long contentLength() throws IOException;

	/**
	 * Determine the last-modified timestamp for this resource.
	 * @throws IOException if the resource cannot be resolved
	 * (in the file system or as some other known physical resource type)
	 */
	long lastModified() throws IOException;

	/**
	 * Create a resource relative to this resource.
	 * @param relativePath the relative path (relative to this resource)
	 * @return the resource handle for the relative resource
	 * @throws IOException if the relative resource cannot be determined
	 */
	Resource createRelative(String relativePath) throws IOException;

	/**
	 * Determine a filename for this resource, i.e. typically the last
	 * part of the path: for example, "myfile.txt".
	 * <p>Returns {@code null} if this type of resource does not
	 * have a filename.
	 */
	@Nullable
	String getFilename();

	/**
	 * Return a description for this resource,
	 * to be used for error output when working with the resource.
	 * <p>Implementations are also encouraged to return this value
	 * from their {@code toString} method.
	 * @see Object#toString()
	 */
	String getDescription();

}
