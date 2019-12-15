/*
 * Copyright (C) 2009 Benny Bottema (benny@bennybottema.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.simplejavamail.internal.batchsupport;

import org.bbottema.genericobjectpool.PoolableObject;
import org.simplejavamail.api.internal.batchsupport.LifecycleDelegatingTransport;

import org.jetbrains.annotations.NotNull;
import javax.mail.Transport;

/**
 * Wraps {@link PoolableObject} to implement {@link LifecycleDelegatingTransport}, so transport resources
 * can be used outside the batchmodule and released to be reused in connection pool.
 */
class LifecycleDelegatingTransportImpl implements LifecycleDelegatingTransport {
	private final PoolableObject<Transport> pooledTransport;

	LifecycleDelegatingTransportImpl(final PoolableObject<Transport> pooledTransport) {
		this.pooledTransport = pooledTransport;
	}

	@NotNull
	@Override
	public Transport getTransport() {
		return pooledTransport.getAllocatedObject();
	}

	@Override
	public void signalTransportUsed() {
		pooledTransport.release();
	}

	@Override
	public void signalTransportFailed() {
		pooledTransport.invalidate();
	}
}