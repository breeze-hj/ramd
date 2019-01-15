package eastwind.ramd.codec;

import java.net.InetSocketAddress;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eastwind.ramd.model.RamdMessage;
import eastwind.ramd.model.Shake;
import eastwind.ramd.model.TcpObject;
import eastwind.ramd.model.Vote;
import eastwind.ramd.support.Result;

/**
 * Created by jan.huang on 2018/3/13.
 */
public class KryoFactory {

    private static InetSocketAddressSerializer inetSocketAddressSerializer = new InetSocketAddressSerializer();

    public static Kryo newKryo() {
        Kryo kryo = new Kryo();
        kryo.addDefaultSerializer(InetSocketAddress.class, inetSocketAddressSerializer);
        kryo.register(TcpObject.class);
        kryo.register(Shake.class);
        kryo.register(Vote.class);
        kryo.register(Result.class);
        kryo.register(RamdMessage.class);
        return kryo;
    }

    static class InetSocketAddressSerializer extends Serializer<InetSocketAddress> {

        public InetSocketAddressSerializer() {
            super(true, true);
        }

        @Override
        public void write(Kryo kryo, Output output, InetSocketAddress o) {
            output.writeString(o.getHostString());
            output.writeInt(o.getPort());
        }

        @Override
        public InetSocketAddress read(Kryo kryo, Input input, Class<InetSocketAddress> aClass) {
            String host = input.readString();
            int port = input.readInt();
            return new InetSocketAddress(host, port);
        }
    }
}
