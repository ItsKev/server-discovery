package net.itskev.serverdiscovery;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import net.itskev.serverdiscovery.listener.PlayerJoinListener;
import net.itskev.serverdiscovery.tasks.ServerDiscoveryTask;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Timer;

public class ServerDiscoveryPlugin extends Plugin {

  private final Timer timer = new Timer(true);

  @Override
  public void onEnable() {
    DefaultKubernetesClient kubernetesClient = new DefaultKubernetesClient();
    timer.scheduleAtFixedRate(new ServerDiscoveryTask(getProxy(), kubernetesClient), 1000, 1000);

    getProxy().getPluginManager().registerListener(this, new PlayerJoinListener(getProxy()));
  }
}
