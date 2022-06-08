function initializeCoreMod() {
    var ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnList = Java.type("org.objectweb.asm.tree.InsnList");
    var InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
    var FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
    var MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
    var VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
    var LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");
    var JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");

    return {
        'ClientLoginNetHandler Transformer': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl',
                'methodName': 'm_7254_',
                'methodDesc': '(Lnet/minecraft/network/protocol/login/ClientboundCustomQueryPacket;)V'
            },
            'transformer': function (method) {
                var insns = new InsnList();
                var jumpAfterLabel = new LabelNode();
                insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/github/oam/OpenAuthMod", "getInstance", "()Lcom/github/oam/OpenAuthMod;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/multiplayer/ClientHandshakePacketListenerImpl", ASMAPI.mapField("f_104522_"), "Lnet/minecraft/network/Connection;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/protocol/login/ClientboundCustomQueryPacket", ASMAPI.mapField("f_134746_"), "Lnet/minecraft/resources/ResourceLocation;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/protocol/login/ClientboundCustomQueryPacket", ASMAPI.mapField("f_134745_"), "I"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/protocol/login/ClientboundCustomQueryPacket", ASMAPI.mapField("f_134747_"), "Lnet/minecraft/network/FriendlyByteBuf;"));
                insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/github/oam/OpenAuthMod", "handleLoginCustomPayload", "(Lnet/minecraft/network/Connection;Lnet/minecraft/resources/ResourceLocation;ILnet/minecraft/network/FriendlyByteBuf;)Z"));
                insns.add(new JumpInsnNode(Opcodes.IFEQ, jumpAfterLabel));
                insns.add(new InsnNode(Opcodes.RETURN));
                insns.add(jumpAfterLabel);

                ASMAPI.insertInsnList(method, ASMAPI.MethodType.INTERFACE, "java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V", insns, ASMAPI.InsertMode.INSERT_AFTER);
                return method;
            }
        },
        'ClientPlayNetHandler Transformer': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.multiplayer.ClientPacketListener',
                'methodName': 'm_7413_',
                'methodDesc': '(Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;)V'
            },
            'transformer': function (method) {
                var insns = new InsnList();
                var jumpAfterLabel = new LabelNode();
                insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/github/oam/OpenAuthMod", "getInstance", "()Lcom/github/oam/OpenAuthMod;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/multiplayer/ClientPacketListener", ASMAPI.mapField("f_104885_"), "Lnet/minecraft/network/Connection;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/protocol/game/ClientboundCustomPayloadPacket", ASMAPI.mapField("f_132029_"), "Lnet/minecraft/resources/ResourceLocation;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/protocol/game/ClientboundCustomPayloadPacket", ASMAPI.mapField("f_132030_"), "Lnet/minecraft/network/FriendlyByteBuf;"));
                insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/github/oam/OpenAuthMod", "handlePlayCustomPayload", "(Lnet/minecraft/network/Connection;Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Z"));
                insns.add(new JumpInsnNode(Opcodes.IFEQ, jumpAfterLabel));
                insns.add(new InsnNode(Opcodes.RETURN));
                insns.add(jumpAfterLabel);

                ASMAPI.insertInsnList(method, ASMAPI.MethodType.STATIC, "net/minecraft/network/protocol/PacketUtils", ASMAPI.mapMethod("m_131363_"), "(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", insns, ASMAPI.InsertMode.INSERT_AFTER);
                return method;
            }
        }
    }
}
