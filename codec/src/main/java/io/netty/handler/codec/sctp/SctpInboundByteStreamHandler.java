/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.netty.handler.codec.sctp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.socket.SctpMessage;
import io.netty.handler.codec.CodecException;

/**
 * A ChannelHandler which receives  {@link SctpMessage} belongs to a application protocol form a specific SCTP Stream
 * and decode it as {@link ByteBuf}.
 */
public class SctpInboundByteStreamHandler extends ChannelInboundMessageHandlerAdapter<SctpMessage> {
    private final int protocolIdentifier;
    private final int streamIdentifier;


    /**
     * @param streamIdentifier   accepted stream number, this should be >=0 or <= max stream number of the association.
     * @param protocolIdentifier supported application protocol.
     */
    public SctpInboundByteStreamHandler(int protocolIdentifier, int streamIdentifier) {
        this.protocolIdentifier = protocolIdentifier;
        this.streamIdentifier = streamIdentifier;
    }

    protected boolean isDecodable(SctpMessage msg) {
        return msg.getProtocolIdentifier() == protocolIdentifier && msg.getStreamIdentifier() == streamIdentifier;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, SctpMessage msg) throws Exception {
        if (!isDecodable(msg)) {
            ctx.nextInboundMessageBuffer().add(msg);
            ctx.fireInboundBufferUpdated();
            return;
        }

        if (!msg.isComplete()) {
            throw new CodecException(String.format("Received SctpMessage is not complete, please add %s in the " +
                    "pipeline before this handler", SctpMessageCompletionHandler.class.getSimpleName()));
        }

        ctx.nextInboundByteBuffer().writeBytes(msg.getPayloadBuffer());
        ctx.fireInboundBufferUpdated();
    }
}
