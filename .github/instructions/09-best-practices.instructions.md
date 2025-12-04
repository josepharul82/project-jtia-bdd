# ✅ Bonnes Pratiques

## Organisation du Code

### Structure des Fichiers
- **Un fichier Page par page web testée**
- **Un fichier Step par fonctionnalité/flux métier**
- **Regrouper les steps liés dans le même fichier**
- **Nommer les fichiers de façon descriptive**

### Exemple d'Organisation
```
model/
├── LoginPage.java          # Page de connexion
├── DashboardPage.java      # Page tableau de bord
├── ProspectPage.java       # Page prospect
└── UnderwritingPage.java   # Page souscription

step/
├── Hook.java              # Hooks Before/After
├── LoginFlowStep.java     # Steps de connexion
├── ProspectFlowStep.java  # Steps prospect
└── NavigationStep.java    # Steps de navigation commune
```

---

## Tests Robustes

### Synchronisation
- ✅ **Utiliser `page.sync(seconds)` pour attendre explicitement**
- ✅ **Préférer les attentes explicites aux `Thread.sleep()`**
- ❌ **Éviter les attentes fixes (sleep)**

```java
// ✅ BON
myPage.submitButton.click();
myPage.sync(2); // Attendre que la page se charge
screenshot();

// ❌ MAUVAIS
myPage.submitButton.click();
Thread.sleep(2000); // Attente fixe
screenshot();
```

### Assertions
- ✅ **Utiliser les méthodes du framework**
- ✅ **Ajouter des screenshots après chaque assertion**
- ✅ **Logger les vérifications importantes**

```java
// ✅ BON
info("Validating error message");
myPage.errorMessage.assertContentText("ERROR: Field is required");
screenshot();

// ❌ MAUVAIS
String text = myPage.errorMessage.getText();
assert text.equals("ERROR: Field is required");
```

---

## Sélecteurs Web

### Ordre de Préférence
1. **ID** - Le plus stable et rapide
2. **Name** - Stable pour les formulaires
3. **CSS Selector** - Pour les sélecteurs simples
4. **XPath** - En dernier recours

```java
// ✅ EXCELLENT : ID
WebElementDescription element = WebElementDescription.builder()
    .tagName("button")
    .id("submit-btn")
    .build();

// ✅ BON : Name
WebElementDescription input = WebElementDescription.builder()
    .tagName("input")
    .name("userName")
    .build();

// ⚠️ ACCEPTABLE : CSS Selector
WebElementDescription alert = WebElementDescription.builder()
    .cssSelector(".alert-danger")
    .build();

// ❌ À ÉVITER : XPath complexe
WebElementDescription element = WebElementDescription.builder()
    .xPath("//div[@class='container']/form/div[2]/button")
    .build();
```

---

## Logging et Débogage

### Traçabilité
```java
@When("I perform a complex action")
public void complexAction() throws Exception {
    info("Starting complex action");
    
    info("Step 1: Filling form");
    myPage.input.sendKeys("value");
    screenshot();
    
    info("Step 2: Validating input");
    myPage.input.assertContentText("value");
    
    info("Step 3: Submitting form");
    myPage.submitButton.click();
    screenshot();
    
    info("Complex action completed");
}
```

### Screenshots
- Prendre après chaque action importante
- Prendre avant et après les assertions
- Prendre en cas d'erreur (dans le After hook)

---

## Gestion des Erreurs

### Propagation
```java
// ✅ BON : Propager les exceptions
public void myMethod() throws Exception {
    info("Performing action");
    myPage.element.click();
}

// ❌ MAUVAIS : Avaler les exceptions
public void myMethod() {
    try {
        myPage.element.click();
    } catch (Exception e) {
        // Silent fail
    }
}
```

### Messages d'Erreur
```java
// ✅ BON : Message descriptif
if (value == null || value.isEmpty()) {
    throw new IllegalArgumentException(
        "Value cannot be null or empty. Expected a valid string."
    );
}

// ❌ MAUVAIS : Message générique
if (value == null || value.isEmpty()) {
    throw new Exception("Error");
}
```

---

## Performance et Maintenance

### Réutilisabilité
- Créer des méthodes utilitaires pour les actions communes
- Partager les steps entre différentes features
- Utiliser le Background pour les préconditions

### Lisibilité
- Nommer les variables et méthodes de façon descriptive
- Commenter le code complexe
- Garder les méthodes courtes et focalisées

### Évolutivité
- Anticiper les changements de l'UI
- Utiliser des constantes pour les valeurs magiques
- Documenter les dépendances et limitations

---

## Checklist Avant Commit

- [ ] Tous les tests passent localement
- [ ] Pas de code commenté inutile
- [ ] Pas de `System.out.println()` ou `printStackTrace()`
- [ ] Screenshots ajoutés aux étapes importantes
- [ ] Logs ajoutés avec `info()`
- [ ] Sélecteurs stables (ID/Name préférés)
- [ ] Pas de credentials en dur
- [ ] Documentation mise à jour si nécessaire
- [ ] Format du code correct (indentation, etc.)
- [ ] Pas de warnings de compilation

