package cc.co.evenprime.bukkit.nocheat.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockPlaceEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.checks.BlockPlaceCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockplace.DirectionCheck;
import cc.co.evenprime.bukkit.nocheat.checks.blockplace.ReachCheck;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockPlace;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;
import cc.co.evenprime.bukkit.nocheat.debug.PerformanceManager.Type;

/**
 * Central location to listen to Block-related events and dispatching them to
 * checks
 * 
 */
public class BlockPlaceEventManager extends EventManager {

    private final List<BlockPlaceCheck> checks;

    public BlockPlaceEventManager(NoCheat plugin) {

        super(plugin);

        this.checks = new ArrayList<BlockPlaceCheck>(2);
        this.checks.add(new DirectionCheck(plugin));
        this.checks.add(new ReachCheck(plugin));

        registerListener(Event.Type.BLOCK_PLACE, Priority.Lowest, true, plugin.getPerformance(Type.BLOCKPLACE));
    }

    @Override
    protected void handleBlockPlaceEvent(BlockPlaceEvent event, Priority priority) {

        if(event.getBlock() == null)
            return;

        boolean cancelled = false;

        NoCheatPlayer player = plugin.getPlayer(event.getPlayer().getName());

        if(!player.getConfiguration().blockplace.check || player.hasPermission(Permissions.BLOCKPLACE)) {
            return;
        }

        CCBlockPlace cc = player.getConfiguration().blockplace;
        BlockPlaceData data = player.getData().blockplace;

        data.blockPlaced.set(event.getBlock());
        data.blockPlacedAgainst.set(event.getBlockAgainst());
        data.placedType = event.getBlock().getType();

        for(BlockPlaceCheck check : checks) {
            // If it should be executed, do it
            if(!cancelled && check.isEnabled(cc) && !player.hasPermission(check.getPermission())) {
                check.check(player, data, cc);
            }
        }

        if(cancelled) {
            event.setCancelled(cancelled);
        }
    }

    public List<String> getActiveChecks(ConfigurationCache cc) {
        LinkedList<String> s = new LinkedList<String>();

        if(cc.blockplace.check && cc.blockplace.reachCheck)
            s.add("blockplace.reach");

        return s;
    }
}
