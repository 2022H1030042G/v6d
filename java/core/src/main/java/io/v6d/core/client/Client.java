/** Copyright 2020-2022 Alibaba Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.v6d.core.client;

import static com.google.common.base.MoreObjects.toStringHelper;

import io.v6d.core.client.ds.Object;
import io.v6d.core.client.ds.ObjectMeta;
import io.v6d.core.common.util.InstanceID;
import io.v6d.core.common.util.ObjectID;
import io.v6d.core.common.util.VineyardException;
import java.util.Collection;

/** Vineyard IPC client. */
public abstract class Client {
    protected String ipc_socket;
    protected String rpc_endpoint;
    protected InstanceID instanceId;

    public abstract ObjectMeta createMetaData(ObjectMeta metadata) throws VineyardException;

    public ObjectMeta getMetaData(ObjectID id) throws VineyardException {
        return this.getMetaData(id, false);
    }

    public ObjectMeta getMetaData(ObjectID id, boolean sync_remote) throws VineyardException {
        return this.getMetaData(id, sync_remote, false);
    }

    public abstract ObjectMeta getMetaData(ObjectID id, boolean sync_remote, boolean wait)
            throws VineyardException;

    public abstract Collection<ObjectMeta> listMetaData(String pattern) throws VineyardException;

    public abstract Collection<ObjectMeta> listMetaData(String pattern, boolean regex)
            throws VineyardException;

    public abstract Collection<ObjectMeta> listMetaData(String pattern, boolean regex, int limit)
            throws VineyardException;

    public abstract void persist(ObjectID id) throws VineyardException;

    public void persist(ObjectMeta meta) throws VineyardException {
        this.persist(meta.getId());
    }

    public void persist(Object object) throws VineyardException {
        this.persist(object.getId());
    }

    public abstract void delete(Collection<ObjectID> ids, boolean force, boolean deep)
            throws VineyardException;

    public void delete(Collection<ObjectID> ids) throws VineyardException {
        this.delete(ids, false, true);
    }

    public abstract void putName(ObjectID id, String name) throws VineyardException;

    public abstract ObjectID getName(String name, boolean wait) throws VineyardException;

    public ObjectID getName(String name) throws VineyardException {
        return getName(name, false);
    }

    public abstract void dropName(String name) throws VineyardException;

    public abstract void createStream(final ObjectID id) throws VineyardException;

    public abstract void openStream(final ObjectID id, final char mode) throws VineyardException;

    public abstract void pushStreamChunk(final ObjectID id, final ObjectID chunk)
            throws VineyardException;

    public abstract ObjectID pullStreamChunkID(final ObjectID id) throws VineyardException;

    public abstract ObjectMeta pullStreamChunkMeta(final ObjectID id) throws VineyardException;

    public abstract void stopStream(final ObjectID id, boolean failed) throws VineyardException;

    public abstract InstanceStatus getInstanceStatus() throws VineyardException;

    public abstract ClusterStatus getClusterStatus() throws VineyardException;

    public boolean connected() {
        return false;
    }

    public void disconnect() {}

    public String getIPCSocket() {
        return this.ipc_socket;
    }

    public String getRPCEndpoint() {
        return this.rpc_endpoint;
    }

    public InstanceID getInstanceId() {
        return this.instanceId;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("instance_id", instanceId)
                .add("ipc_socket", ipc_socket)
                .add("rpc_endpoint", rpc_endpoint)
                .toString();
    }
}
