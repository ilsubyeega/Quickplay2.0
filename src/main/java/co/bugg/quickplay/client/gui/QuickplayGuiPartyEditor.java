package co.bugg.quickplay.client.gui;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.games.Game;
import co.bugg.quickplay.games.Mode;
import co.bugg.quickplay.games.PartyMode;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI screen for editing the modes in party mode
 */
public class QuickplayGuiPartyEditor extends QuickplayGui {
    /**
     * A list of every single mode the client is aware of
     */
    List<PartyMode> modes = new ArrayList<>();
    /**
     * A list of all the modes toggled on
     * CURRENTLY A REFERENCE to the Quickplay settings, not a value
     */
    List<PartyMode> toggledModes = new ArrayList<>();

    /**
     * The width of buttons
     */
    public final int buttonWidth = 300;
    /**
     * The height of buttons
     */
    public final int buttonHeight = 20;
    /**
     * Vertical spacing between buttons
     */
    public final int buttonYMargins = 5;
    /**
     * The Y value at which point values should start being drawn
     */
    public int topOfButtons;

    @Override
    public void initGui() {
        super.initGui();

        topOfButtons = (int) (height * 0.2);

        // Copy over reference to the toggled mode
        toggledModes = Quickplay.INSTANCE.settings.partyModes;

        // Create a list of all applicable modes
        for(Game game : Quickplay.INSTANCE.gameList) {
            if(game.unlocalizedName.equals("partyMode"))
                continue;
            for(Mode mode : game.modes) {
                modes.add(new PartyMode(game.name + " - " + mode.name, mode.command, game.unlocalizedName.replace("/", "") + "/" + mode.command.replace("/", "")));
            }
        }

        int buttonId = 0;
        // Add all the buttons for each mode
        for(PartyMode mode : modes) {
            // Display string for whether this mode is currently enabled or not
            final String trueOrFalse = checkIfModeToggled(mode) != null ? EnumChatFormatting.GREEN + I18n.format("quickplay.config.gui.true") : EnumChatFormatting.RED + I18n.format("quickplay.config.gui.false");
            componentList.add(new QuickplayGuiButton(mode, buttonId, width / 2 - buttonWidth / 2, topOfButtons + (buttonHeight + buttonYMargins) * buttonId, buttonWidth, buttonHeight, mode.name + ": " + trueOrFalse, true));
            buttonId++;
        }

        setScrollingValues();
    }

    /**
     * Checks if the given mode is currently toggled on or not according to {@link #toggledModes}
     * @param mode Mode to check
     * @return The reference to the mode in {@link #toggledModes}, or null if not in the list.
     */
    public PartyMode checkIfModeToggled(PartyMode mode) {
        return toggledModes.stream().filter(settingMode -> settingMode.namespace.equals(mode.namespace)).findFirst().orElse(null);
    }

    @Override
    public void setScrollingValues() {
        super.setScrollingValues();
        scrollFrameTop = topOfButtons;

        // Increase scroll speed & amount
        scrollMultiplier = 3;
        scrollDelay = 2;
        scrollbarYMargins = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        drawDefaultBackground();

        drawScrollbar(width / 2 + buttonWidth / 2 + 3);

        super.drawScreen(mouseX, mouseY, partialTicks);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        super.componentClicked(component);
        if(component.origin instanceof PartyMode) {
            final PartyMode mode = (PartyMode) component.origin;
            // Display name of this component, split at the colon, to separate the name from the current toggle status
            final String nameWithoutToggleStatus = component.displayString.split(":")[0];

            // Reference to this mode's twin in the toggled modes list (or null if doesn't exist)
            final PartyMode toggledModeReference = checkIfModeToggled(mode);
            // If the mode is toggled on remove it, otherwise add it
            if(toggledModeReference != null) {
                toggledModes.remove(toggledModeReference);
                component.displayString = nameWithoutToggleStatus + ": " + EnumChatFormatting.RED + I18n.format("quickplay.config.gui.false");
            } else {
                toggledModes.add(mode);
                component.displayString = nameWithoutToggleStatus + ": " + EnumChatFormatting.GREEN + I18n.format("quickplay.config.gui.true");
            }
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        try {
            Quickplay.INSTANCE.settings.save();
        } catch (IOException e) {
            e.printStackTrace();
            Quickplay.INSTANCE.messageBuffer.push(new Message(new ChatComponentTranslation("quickplay.config.saveerror")));
        }
    }
}