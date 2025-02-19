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
package io.v6d.core.client.ds.ffi;

import io.v6d.core.FFI;
import lombok.val;

public class ObjectMeta {
    private io.v6d.core.client.ds.ObjectMeta metadata;

    static {
        FFI.loadNativeLibrary();
    }

    public ObjectMeta(io.v6d.core.client.ds.ObjectMeta metadata) {
        this.metadata = metadata;
    }

    public long resolve() {
        val buffers = metadata.getBuffers().allBuffers();
        val objects = new long[buffers.size()];
        val pointers = new long[buffers.size()];
        val sizes = new long[buffers.size()];
        int index = 0;
        for (val item : buffers.entrySet()) {
            objects[index] = item.getKey().value();
            pointers[index] = item.getValue().getPointer();
            sizes[index] = item.getValue().getSize();
            index += 1;
        }
        return constructNative(metadata.metadataString(), objects, pointers, sizes);
    }

    private native long constructNative(String meta, long[] objects, long[] pointers, long[] sizes);
}
