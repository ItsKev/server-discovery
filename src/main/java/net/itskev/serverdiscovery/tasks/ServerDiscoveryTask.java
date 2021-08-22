package net.itskev.serverdiscovery.tasks;

import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ServerDiscoveryTask extends TimerTask {

  private static final String LABEL_NAME = "server-discovery";

  private final ProxyServer proxyServer;
  private final DefaultKubernetesClient kubernetesClient;

  @Override
  public void run() {
    NamespaceList list = kubernetesClient.namespaces()
        .withLabel(LABEL_NAME, "true")
        .list();

    List<ServerInfo> serverInfo = new ArrayList<>();
    for (Namespace namespace : list.getItems()) {
      try {
        PodList podList = kubernetesClient.pods()
            .inNamespace(namespace.getMetadata().getName())
            .withLabel(LABEL_NAME, "true")
            .list();

        serverInfo.addAll(getServerInfo(podList.getItems()));
      } catch (Exception exception) {
        proxyServer.getLogger().log(Level.SEVERE, "Failed to get servers", exception);
      }
    }

    ProxyConfig config = proxyServer.getConfig();
    config.removeServersNamed(config.getServersCopy().keySet());
    config.addServers(serverInfo);
  }

  private List<ServerInfo> getServerInfo(List<Pod> pods) {
    return pods.stream()
        .map(pod -> proxyServer.constructServerInfo(
            pod.getMetadata().getName(),
            new InetSocketAddress(pod.getStatus().getPodIP(), 25565),
            "",
            false))
        .collect(Collectors.toList());
  }
}
