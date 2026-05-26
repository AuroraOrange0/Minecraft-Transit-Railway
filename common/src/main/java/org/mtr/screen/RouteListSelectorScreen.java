package org.mtr.screen;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import org.mtr.core.data.NameColorDataBase;
import org.mtr.core.data.Route;
import org.mtr.data.IGui;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import org.mtr.tool.GuiHelper;
import org.mtr.widget.ListComponent;
import org.mtr.widget.ListItem;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class RouteListSelectorScreen extends ListSelectorScreen<Route, NameColorDataBase> {

	private final boolean canSelectDuplicateAndCanManuallySortSelectedList;
	private final boolean flattenRoutesByColor;

	public RouteListSelectorScreen(Consumer<ObjectArrayList<Route>> onClose, boolean canSelectDuplicate, boolean canManuallySortSelectedList, boolean flattenRoutesByColor, WindowBase previousScreen) {
		super(true, canSelectDuplicate, canManuallySortSelectedList, onClose, previousScreen);
		this.flattenRoutesByColor = flattenRoutesByColor;
		canSelectDuplicateAndCanManuallySortSelectedList = canSelectDuplicate && canManuallySortSelectedList;
	}

	@Override
	protected void setData(ListComponent<Route> listComponent, ObjectCollection<Route> dataList, boolean isSelectedList, ObjectArrayList<ObjectObjectImmutablePair<Identifier, ListItem.ActionConsumer<Route>>> actions) {
		if (canSelectDuplicateAndCanManuallySortSelectedList && isSelectedList) {
			listComponent.setData(dataList.stream().map(route -> ListItem.createChild(
				(drawing, x, y) -> drawing.setVerticesWH(x + GuiHelper.DEFAULT_PADDING, y + GuiHelper.DEFAULT_PADDING, GuiHelper.MINECRAFT_FONT_SIZE, GuiHelper.MINECRAFT_FONT_SIZE).setColor(ColorHelper.fullAlpha(route.getColor())).draw(),
				null,
				GuiHelper.DEFAULT_PADDING + GuiHelper.MINECRAFT_FONT_SIZE,
				route,
				String.join("|", IGui.formatStationName(route.getName())),
				actions
			)).collect(Collectors.toCollection(ObjectArrayList::new)));
		} else {
			ListComponent.setRoutes(listComponent, dataList, null, flattenRoutesByColor, actions);
		}
	}
}
