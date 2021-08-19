# ServerDiscovery

This plugin connects minecraft servers to a waterfall proxy within Kubernetes.

The service account in the namespace which the proxy is running in needs the privilege to read 
endpoints from the namespaces specified in the config.

## Config file
### endpointName
The endpoints name which the minecraft servers are connected to. This name is normally set directly
in the service itself. A simple service could look like the following:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: minecraft # this is the name which has to be set in the config
  namespace: minecraft
spec:
  selector:
    app: minecraft
  ports:
    - protocol: TCP
      port: 25565
      targetPort: 25565
```

### namespaces
The namespaces where the minecraft servers are running in.

## Role and role bindings
Role in the namespace where the minecraft servers are running in:
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: minecraft
  name: minecraft-role
rules:
  - apiGroups: [""]
    resources: ["endpoints"]
    verbs: ["get"]
```

Corresponding role binding to the namespace where the proxy is running in:
```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: minecraft-role-binding
  namespace: minecraft
subjects:
  - kind: ServiceAccount
    name: default
    namespace: default
roleRef:
  kind: Role
  name: minecraft-role
  apiGroup: rbac.authorization.k8s.io
```
