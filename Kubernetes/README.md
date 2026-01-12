# Kubernetes + Spring Boot + Gradle + Docker Operations Manual

Complete lifecycle: write → build → container → run → deploy → config → secret → verify.

---

## **Machine Prerequisites**

Install the following tools:

1. **Java 21+**
2. **IntelliJ IDEA** (Community or Ultimate)
3. **Git**
4. **Docker Desktop**
   * Enable *Kubernetes* in Docker Desktop → Settings → Kubernetes → Enable Kubernetes → Apply
5. **kubectl CLI**
6. *(Optional alternative cluster)*: `kind` or `minikube`

Check Kubernetes is alive:

```bash
kubectl cluster-info
kubectl get nodes
```

If nodes show `Ready`, cluster OK.

---

## **Create Spring Boot Project (IntelliJ)**

### Step 1: New project

* IntelliJ → *New Project* → *Spring Initializr*
* Settings:
  * Language: Java
  * Build: Gradle
  * Packaging: Jar
  * Java: 21
* Project properties:
  * Group: `com.example`
  * Artifact: `kubernetes`
* Dependencies:
  * **Spring Web**
  * **Spring Boot Actuator** (for health probes)
  * **Spring Boot Test**

Project structure created.

---

## **Write REST Endpoints**

Create `src/main/java/com/example/kubernetes/Controller.java`:

```java
package com.example.kubernetes;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Controller {
    
    @Value("${app.greeting}")
    private String greeting;
    
    @Value("${app.password}")
    private String password;
    
    @GetMapping("/ping")
    public String ping() { 
        return "pong"; 
    }
    
    @GetMapping("/config")
    public String config() {
        return "Greeting: " + greeting + ", Password configured: " + (password != null && !password.isEmpty());
    }
}
```

---

## **Build JAR Artifact**

### Configure Java Version

Update `build.gradle`:

```gradle
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
```

### Build with Gradle

Terminal:

```bash
./gradlew clean test bootJar
```

Artifact lands in:

```
build/libs/Kubernetes-0.0.1-SNAPSHOT.jar
```

Local smoke test:

```bash
java -jar build/libs/*.jar
```

Verify:

```bash
curl http://localhost:8080/ping
```

---

## **Containerize the Application**

Create `Dockerfile` at project root:

```dockerfile
FROM eclipse-temurin:21-jre
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

Build image:

```bash
docker build -t kubernetes:latest .
```

Verify image exists:

```bash
docker images | grep kubernetes
```

Test image locally:

```bash
docker run -p 8080:8080 kubernetes:latest
```

Verify:

```bash
curl http://localhost:8080/ping
```

---

## **Load Image into Kubernetes**

If Docker Desktop Kubernetes: image already local → no push required.

If Minikube:

```bash
minikube image load kubernetes:latest
```

If using registry:

```bash
docker tag kubernetes:latest <registry>/<user>/kubernetes:latest
docker push <registry>/<user>/kubernetes:latest
```

---

## **Write Kubernetes Manifests**

Create directory:

```bash
mkdir k8s
```

### **1. Deployment**

`k8s/deployment.yaml`

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kubernetes
spec:
  replicas: 2
  selector:
    matchLabels:
      app: kubernetes
  template:
    metadata:
      labels:
        app: kubernetes
    spec:
      containers:
        - name: kubernetes
          image: kubernetes:latest
          imagePullPolicy: Never
          ports:
            - containerPort: 8080
          env:
            - name: GREETING
              valueFrom:
                configMapKeyRef:
                  name: kubernetes-config
                  key: GREETING
            - name: PASSWORD
              valueFrom:
                secretKeyRef:
                  name: kubernetes-secret
                  key: PASSWORD
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
```

### **2. Service**

`k8s/service.yaml`

```yaml
apiVersion: v1
kind: Service
metadata:
  name: kubernetes-service
spec:
  type: ClusterIP
  selector:
    app: kubernetes
  ports:
    - port: 80
      targetPort: 8080
```

---

## **Access Application Through Cluster**

Apply manifests first:

```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

Check objects:

```bash
kubectl get pods
kubectl get svc
kubectl get deployment
```

Start port-forward:

```bash
kubectl port-forward svc/kubernetes-service 8080:80
```

Verify:

```bash
curl http://localhost:8080/ping
```

---

## **Kubernetes ConfigMaps**

Create `k8s/configmap.yaml`:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: kubernetes-config
data:
  GREETING: "HelloConfig"
```

Apply ConfigMap:

```bash
kubectl apply -f k8s/configmap.yaml
```

The deployment already references this ConfigMap in the `env` section (see Phase 6).

Spring Boot reads via properties in `application.properties`:

```properties
app.greeting=${GREETING:Hello}
```

And in Controller:

```java
@Value("${app.greeting}")
private String greeting;
```

---

## **Kubernetes Secrets**

Create secret encoded:

```bash
echo -n "secret123" | base64
# Output: c2VjcmV0MTIz
```

Create `k8s/secret.yaml`:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: kubernetes-secret
type: Opaque
data:
  PASSWORD: c2VjcmV0MTIz
```

Apply Secret:

```bash
kubectl apply -f k8s/secret.yaml
```

The deployment already references this Secret in the `env` section (see Phase 6).

Spring Boot configuration in `application.properties`:

```properties
app.password=${PASSWORD:default-password}
```

And in Controller:

```java
@Value("${app.password}")
private String password;
```

Verify config and secret:

```bash
curl http://localhost:8080/config
# Output: Greeting: HelloConfig, Password configured: true
```

---

## **Probes for Kubernetes Health**

Health probes are already configured in the deployment (see Phase 6).

Enable in Spring Boot `application.properties`:

```properties
spring.application.name=Kubernetes

management.endpoint.health.probes.enabled=true
management.endpoints.web.exposure.include=health

app.password=${PASSWORD:default-password}
app.greeting=${GREETING:Hello}
```

The probes are:

* **Liveness**: `/actuator/health/liveness`
* **Readiness**: `/actuator/health/readiness`

Test probes:

```bash
curl http://localhost:8080/actuator/health/liveness
# Output: {"status":"UP"}

curl http://localhost:8080/actuator/health/readiness
# Output: {"status":"UP"}
```

---

## **Verify Deployment**

Check pods in detail:

```bash
kubectl get pods -o wide
```

View pod logs:

```bash
kubectl logs <pod-name>
```

Or get logs from first pod:

```bash
kubectl logs $(kubectl get pods -o name | head -1 | cut -d'/' -f2)
```

Describe pod to see full configuration:

```bash
kubectl describe pod <pod-name>
```

Check rollout status:

```bash
kubectl rollout status deployment/kubernetes
```

View all resources:

```bash
kubectl get all
```

View ConfigMap:

```bash
kubectl get configmap kubernetes-config -o yaml
```

View Secret:

```bash
kubectl get secret kubernetes-secret -o yaml
```

---

## **Clean Up**

Delete all Kubernetes resources:

```bash
kubectl delete -f k8s/
```

Or delete individually:

```bash
kubectl delete deployment kubernetes
kubectl delete service kubernetes-service
kubectl delete configmap kubernetes-config
kubectl delete secret kubernetes-secret
```

---

## **Commands**

### Build & Deploy

```bash
# Build application
./gradlew clean bootJar

# Build Docker image
docker build -t kubernetes:latest .

# Apply all manifests
kubectl apply -f k8s/

# Check status
kubectl get all
kubectl get pods
kubectl get svc
```

### Testing

```bash
# Port forward
kubectl port-forward svc/kubernetes-service 8080:80

# Test endpoints
curl http://localhost:8080/ping
curl http://localhost:8080/config
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

### Debugging

```bash
# View logs
kubectl logs <pod-name>

# Describe resources
kubectl describe pod <pod-name>
kubectl describe deployment kubernetes
kubectl describe service kubernetes-service

# Get into pod
kubectl exec -it <pod-name> -- /bin/sh
```
