package net.teamfruit.scoreboardadaptor;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ObjectiveArgument;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("scoreboardadaptor")
public class ScoreboardAdaptor {

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public ScoreboardAdaptor() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();
        dispatcher.register(
                Commands.literal("adaptscore")
                        .requires(s -> s.hasPermissionLevel(2))
                        .then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("expression", StringArgumentType.string()).then(Commands.argument("command", StringArgumentType.string())
                                .executes(c -> {
                                    ScoreObjective objc = ObjectiveArgument.getObjective(c, "objective");
                                    String expr = StringArgumentType.getString(c, "expression");
                                    String comm = StringArgumentType.getString(c, "command");

                                    Scoreboard sb = c.getSource().getWorld().getScoreboard();
                                    for (String playerName : c.getSource().getPlayerNames()) {
                                        if (sb.entityHasObjective(playerName, objc)) {
                                            Score score = sb.getOrCreateScore(playerName, objc);
                                            Argument scArg = new Argument("x", score.getScorePoints());
                                            double value = new Expression(expr, scArg).calculate();
                                            dispatcher.execute(
                                                    comm
                                                            .replace("{player}", playerName)
                                                            .replace("{x}", String.valueOf(value)),
                                                    c.getSource()
                                            );
                                        }
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )))
        );
    }
}
