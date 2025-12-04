# 🛠️ Commandes Maven

## Commandes de Base

### Compiler le Projet
```bash
# Compilation complète
mvn clean compile

# Compilation sans tests
mvn clean compile -DskipTests
```

### Exécuter les Tests
```bash
# Exécuter tous les tests
mvn clean test

# Exécuter avec un tag spécifique
mvn test -Dcucumber.filter.tags="@flow"

# Exécuter plusieurs tags
mvn test -Dcucumber.filter.tags="@flow and @smoke"

# Exclure un tag
mvn test -Dcucumber.filter.tags="not @wip"
```



### Logs Détaillés
```bash
# Logs détaillés
mvn test -X
```

### Offline Mode
```bash
# Mode hors ligne (ne télécharge pas de dépendances)
mvn test -o
```


## Configuration Maven (pom.xml)

### Propriétés Importantes
```xml
<properties>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
    <cucumber.version>7.x.x</cucumber.version>
    <selenium.version>4.x.x</selenium.version>
</properties>
```

### Plugin Surefire
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0-M5</version>
    <configuration>
        <testFailureIgnore>false</testFailureIgnore>
        <includes>
            <include>**/*Test.java</include>
        </includes>
    </configuration>
</plugin>
```

---

## Résolution de Problèmes

### Erreurs Communes

#### Dépendances Manquantes
```bash
# Forcer le téléchargement des dépendances
mvn clean install -U
```


