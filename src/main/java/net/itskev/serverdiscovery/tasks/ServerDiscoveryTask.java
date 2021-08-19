package net.itskev.serverdiscovery.tasks;

import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ServerDiscoveryTask extends TimerTask {

  private final ProxyServer proxyServer;
  private final DefaultKubernetesClient kubernetesClient;
  private final Configuration configuration;

  @Override
  public void run() {
    List<ServerInfo> serverInfo = new ArrayList<>();
    for (String namespace : getNamespaces()) {
      try {
        Endpoints endpoints = kubernetesClient.endpoints().inNamespace(namespace).withName(getEndpointName()).get();
        if (endpoints == null) {
          return;
        }

        serverInfo.addAll(getServerInfo(endpoints));
      } catch (Exception exception) {
        proxyServer.getLogger().log(Level.SEVERE, "Failed to get servers", exception);
      }
    }

    ProxyConfig config = proxyServer.getConfig();
    config.removeServersNamed(config.getServersCopy().keySet());
    config.addServers(serverInfo);
  }

  private String getEndpointName() {
    return configuration.getString("endpointName");
  }

  private List<String> getNamespaces() {
    return configuration.getStringList("namespaces");
  }

  private List<ServerInfo> getServerInfo(Endpoints endpoints) {
    return endpoints.getSubsets().stream()
        .filter(endpointSubset -> !endpointSubset.getAddresses().isEmpty())
        .flatMap(endpointSubset -> endpointSubset.getAddresses().stream())
        .map(endpointAddress -> proxyServer.constructServerInfo(
            endpointAddress.getTargetRef().getName(),
            new InetSocketAddress(endpointAddress.getIp(), 25565),
            "",
            false))
        .collect(Collectors.toList());
  }
}
