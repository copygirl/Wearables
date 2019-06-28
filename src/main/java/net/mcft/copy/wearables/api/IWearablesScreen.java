package net.mcft.copy.wearables.api;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.screen.Screen;

/**
 * Implemented by container screens which wish to have control
 * over when the Wearables popup may be shown. For example, for the
 * creative inventory screen, only when the survival tab is selected.
 * <p>
 * The popup may be enabled for any container screen using configuration
 * stored in data packs under {@code config/wearables/container/}, not
 * just ones which implement this interface.
 */
@Environment(EnvType.CLIENT)
public interface IWearablesScreen
{
	/** Returns whether the Wearables popup may be shown. */
	public boolean allowWearablesPopup();
	
	
	/** Returns the specified {@link Screen} casted as an
	 *  {@link IWearablesScreen}, or {@link #DUMMY} if it isn't. */
	public static IWearablesScreen from(Screen screen)
		{ return (screen instanceof IWearablesScreen) ? (IWearablesScreen)screen : DUMMY; }
	
	public static final IWearablesScreen DUMMY = new IWearablesScreen()
		{ @Override public boolean allowWearablesPopup() { return true; } };
}
