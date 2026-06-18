package com.nekogan.pipez.net;

import de.maxhenkel.corelib.net.Message;
import com.nekogan.pipez.blocks.tileentity.types.PipeType;
import com.nekogan.pipez.gui.ExtractContainer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class CycleFilterModeMessage implements Message<CycleFilterModeMessage> {

    private int index;

    public CycleFilterModeMessage() {

    }

    public CycleFilterModeMessage(int index) {
        this.index = index;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        AbstractContainerMenu container = context.getSender().containerMenu;
        if (container instanceof ExtractContainer) {
            ExtractContainer extractContainer = (ExtractContainer) container;
            PipeType<?> pipeType = extractContainer.getPipe().getPipeTypes()[index];
            extractContainer.getPipe().setFilterMode(extractContainer.getSide(), pipeType, extractContainer.getPipe().getFilterMode(extractContainer.getSide(), pipeType).cycle());
        }
    }

    @Override
    public CycleFilterModeMessage fromBytes(FriendlyByteBuf packetBuffer) {
        this.index = packetBuffer.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeInt(index);
    }
}
