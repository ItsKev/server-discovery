package net.itskev.serverdiscovery;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import net.itskev.serverdiscovery.listener.PlayerJoinListener;
import net.itskev.serverdiscovery.tasks.ServerDiscoveryTask;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Timer;
import java.util.logging.Level;

public class ServerDiscoveryPlugin extends Plugin {

  private final Timer timer = new Timer(true);

  @Override
  public void onEnable() {
    Configuration configuration = loadConfiguration().orElseThrow();
    DefaultKubernetesClient kubernetesClient = new DefaultKubernetesClient();

    timer.scheduleAtFixedRate(new ServerDiscoveryTask(getProxy(), kubernetesClient, configuration), 1000, 1000);

    getProxy().getPluginManager().registerListener(this, new PlayerJoinListener(getProxy()));
  }

  private Optional<Configuration> loadConfiguration() {
    File folder = getDataFolder();
    if (!folder.exists()) {
      folder.mkdir();
    }

    File file = new File(folder, "config.yml");
    if (!file.exists()) {
      try (InputStream inputStream = getResourceAsStream("config.yml")) {
        Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException exception) {
        getLogger().log(Level.SEVERE, "Failed to write config file", exception);
      }
    }

    try {
      return Optional.ofNullable(ConfigurationProvider.getProvider(YamlConfiguration.class).load(file));
    } catch (IOException exception) {
      getLogger().log(Level.SEVERE, "Failed to load config file", exception);
      return Optional.empty();
    }
  }
}
