package org.openrs2.protocol.login.downstream

import org.openrs2.protocol.EmptyPacketCodec
import javax.inject.Singleton

@Singleton
public class ServiceUnavailableCodec : EmptyPacketCodec<LoginResponse.ServiceUnavailable>(
    packet = LoginResponse.ServiceUnavailable,
    opcode = 27
)