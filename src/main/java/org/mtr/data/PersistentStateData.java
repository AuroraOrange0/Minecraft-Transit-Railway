package org.mtr.data;

import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.util.datafix.DataFixTypes;
import org.mtr.MTR;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import java.util.stream.LongStream;

/**
 * This class is for storing extra world data that is not stored in Transport Simulation Core.
 * For example, "Disable Next Station Announcements" is a Minecraft-only setting which isn't tracked by Transport Simulation Core.
 */
public final class PersistentStateData extends SavedData {

	@Getter
	private final String uniqueWorldId;
	private final LongAVLTreeSet routeIdsWithDisabledAnnouncements = new LongAVLTreeSet();

	private static final String KEY_UNIQUE_WORLD_ID = "unique_world_id";
	private static final String KEY_ROUTE_IDS_WITH_DISABLED_ANNOUNCEMENTS = "route_ids_with_disabled_announcements";
	public static final Codec<PersistentStateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.STRING.optionalFieldOf(KEY_UNIQUE_WORLD_ID, "").forGetter(PersistentStateData::getUniqueWorldId),
		Codec.LONG_STREAM.optionalFieldOf(KEY_ROUTE_IDS_WITH_DISABLED_ANNOUNCEMENTS, LongStream.empty()).forGetter(data -> data.routeIdsWithDisabledAnnouncements.longStream())
	).apply(instance, PersistentStateData::new));
	public static final SavedDataType<PersistentStateData> TYPE = new SavedDataType<>(MTR.MOD_ID, PersistentStateData::new, CODEC, DataFixTypes.LEVEL);

	public PersistentStateData() {
		super();
		uniqueWorldId = MTR.randomString();
	}

	private PersistentStateData(String uniqueWorldId, LongStream routeIdsWithDisabledAnnouncements) {
		super();
		if (uniqueWorldId.isEmpty()) {
			this.uniqueWorldId = MTR.randomString();
			setDirty();
		} else {
			this.uniqueWorldId = uniqueWorldId;
		}
		routeIdsWithDisabledAnnouncements.forEach(this.routeIdsWithDisabledAnnouncements::add);
	}

	public boolean getRouteIdHasDisabledAnnouncements(long routeId) {
		return routeIdsWithDisabledAnnouncements.contains(routeId);
	}

	public void setRouteIdHasDisabledAnnouncements(long routeId, boolean isDisabled) {
		if (isDisabled) {
			routeIdsWithDisabledAnnouncements.add(routeId);
		} else {
			routeIdsWithDisabledAnnouncements.remove(routeId);
		}
		setDirty();
	}
}
