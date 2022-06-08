package com.github.oam.injection;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class NetworkManagerTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.replace(".", "/").equals("net/minecraft/network/NetworkManager")) {
            ClassNode node;
            { //Generate ClassNode from byte source
                final ClassReader cr = new ClassReader(basicClass);
                node = new ClassNode();
                cr.accept(node, ClassReader.EXPAND_FRAMES);
            }
            MethodNode channelRead0 = null;
            { //Get correct channelRead0 method
                for (MethodNode method : node.methods) {
                    if ((method.name.equals("channelRead0") || FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(node.name, method.name, method.desc).equals("channelRead0")) && (method.access & Opcodes.ACC_SYNTHETIC) == 0) {
                        channelRead0 = method;
                        break;
                    }
                }
                if (channelRead0 == null) {
                    throw new IllegalStateException("Unable to find 'channelRead0' method");
                }
            }
            final InsnList insns = new InsnList();
            final LabelNode jumpAfter = new LabelNode();
            insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/github/oam/OpenAuthMod", "getInstance", "()Lcom/github/oam/OpenAuthMod;", false));
            insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
            insns.add(new VarInsnNode(Opcodes.ALOAD, 2));
            insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "com/github/oam/OpenAuthMod", "handlePacket", "(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/network/Packet;)Z", false));
            insns.add(new JumpInsnNode(Opcodes.IFEQ, jumpAfter));
            insns.add(new InsnNode(Opcodes.RETURN));
            insns.add(jumpAfter);
            channelRead0.instructions.insertBefore(channelRead0.instructions.getFirst(), insns);
            { //Get the byte source of the modified class
                final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                node.accept(classWriter);
                basicClass = classWriter.toByteArray();
            }
        }
        return basicClass;
    }

}
