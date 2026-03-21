package backend.academy.linktracker.scrapper.grpc;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatIdServerInterceptor implements ServerInterceptor {

    public static final Context.Key<Long> CHAT_ID_KEY = Context.key("chatId");

    private static final Metadata.Key<String> TG_CHAT_ID_HEADER =
            Metadata.Key.of("Tg-chat-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String chatIdStr = headers.get(TG_CHAT_ID_HEADER);
        String methodName = call.getMethodDescriptor().getFullMethodName();

        if (methodName.endsWith("RegisterChat") || methodName.endsWith("DeleteChat")) {
            return Contexts.interceptCall(Context.current(), call, headers, next);
        }

        if (chatIdStr == null || chatIdStr.isBlank()) {
            log.atWarn().addKeyValue("method", methodName).log("tg-chat-id header is missing");

            call.close(Status.INVALID_ARGUMENT.withDescription("tg-chat-id header is required"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        try {
            long chatId = Long.parseLong(chatIdStr);
            Context context = Context.current().withValue(CHAT_ID_KEY, chatId);
            return Contexts.interceptCall(context, call, headers, next);
        } catch (NumberFormatException e) {
            log.atWarn().addKeyValue("invalidValue", chatIdStr).log("Invalid format for tg-chat-id header");

            call.close(Status.INVALID_ARGUMENT.withDescription("Invalid tg-chat-id format"), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }
}
