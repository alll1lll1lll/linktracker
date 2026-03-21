package backend.academy.linktracker.bot.client;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatIdClientInterceptor implements ClientInterceptor {

    private final long chatId;

    private static final Metadata.Key<String> TG_CHAT_ID_HEADER =
            Metadata.Key.of("Tg-chat-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(TG_CHAT_ID_HEADER, String.valueOf(chatId));
                super.start(responseListener, headers);
            }
        };
    }
}
