package org.iotp.gateway.extensions.opc.scan;

import java.util.Optional;

import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.iotp.infomgt.data.kv.KvEntry;

/**
 */
public class DataValueConverter {

    public static Optional<KvEntry> toKvEntry(DataValue dataValue){
        return Optional.empty();
    }
}
