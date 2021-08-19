package net.itskev.serverdiscovery.listener;

import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.List;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {

  private final ProxyServer proxyServer;

  @EventHandler
  public void onPlayerLogin(PostLoginEvent event) {
    List<String> serverPriority = event.getPlayer().getPendingConnection().getListener().getServerPriority();
    serverPriority.clear();

    serverPriority.add(proxyServer.getServersCopy().keySet().stream().findFirst().orElseThrow());
  }
}
