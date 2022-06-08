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
                'class': 'net.minecraft.client.network.login.ClientLoginNetHandler',
                'methodName': 'func_209521_a',
                'methodDesc': '(Lnet/minecraft/network/login/server/SCustomPayloadLoginPacket;)V'
            },
            'transformer': function (method) {
                var insns = new InsnList();
                var jumpAfterLabel = new LabelNode();
                insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/github/oam/OpenAuthMod", "getInstance", "()Lcom/github/oam/OpenAuthMod;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/network/login/ClientLoginNetHandler", ASMAPI.mapField("field_147393_d"), "Lnet/minecraft/network/NetworkManager;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/login/server/SCustomPayloadLoginPacket", ASMAPI.mapField("field_209920_b"), "Lnet/minecraft/util/ResourceLocation;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/login/server/SCustomPayloadLoginPacket", ASMAPI.mapField("field_209919_a"), "I"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/login/server/SCustomPayloadLoginPacket", ASMAPI.mapField("field_209921_c"), "Lnet/minecraft/network/PacketBuffer;"));
                insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/github/oam/OpenAuthMod", "handleLoginCustomPayload", "(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/util/ResourceLocation;ILnet/minecraft/network/PacketBuffer;)Z"));
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
                'class': 'net.minecraft.client.network.play.ClientPlayNetHandler',
                'methodName': 'func_147240_a',
                'methodDesc': '(Lnet/minecraft/network/play/server/SCustomPayloadPlayPacket;)V'
            },
            'transformer': function (method) {
                var insns = new InsnList();
                var jumpAfterLabel = new LabelNode();
                insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/github/oam/OpenAuthMod", "getInstance", "()Lcom/github/oam/OpenAuthMod;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/network/play/ClientPlayNetHandler", ASMAPI.mapField("field_147302_e"), "Lnet/minecraft/network/NetworkManager;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/play/server/SCustomPayloadPlayPacket", ASMAPI.mapField("field_149172_a"), "Lnet/minecraft/util/ResourceLocation;"));
                insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insns.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/network/play/server/SCustomPayloadPlayPacket", ASMAPI.mapField("field_149171_b"), "Lnet/minecraft/network/PacketBuffer;"));
                insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/github/oam/OpenAuthMod", "handlePlayCustomPayload", "(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/network/PacketBuffer;)Z"));
                insns.add(new JumpInsnNode(Opcodes.IFEQ, jumpAfterLabel));
                insns.add(new InsnNode(Opcodes.RETURN));
                insns.add(jumpAfterLabel);

                ASMAPI.insertInsnList(method, ASMAPI.MethodType.STATIC, "net/minecraft/network/PacketThreadUtil", ASMAPI.mapMethod("func_218797_a"), "(Lnet/minecraft/network/IPacket;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/concurrent/ThreadTaskExecutor;)V", insns, ASMAPI.InsertMode.INSERT_AFTER);
                return method;
            }
        }
    }
}
