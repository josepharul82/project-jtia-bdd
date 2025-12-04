# 📚 Instructions pour GitHub Copilot

Ce dossier contient les instructions organisées pour guider GitHub Copilot dans l'assistance au développement du projet d'automatisation de tests `project-jtia`.

## 📂 Structure des Instructions

### 🏗️ Architecture & Concepts
1. **[01-architecture.md](01-architecture.md)** - Stack technique et structure du projet
2. **[10-resources.md](10-resources.md)** - Documentation externe et ressources

### 🧪 Développement de Tests
3. **[02-page-object-model.md](02-page-object-model.md)** - Pattern POM et conventions
4. **[03-step-definitions.md](03-step-definitions.md)** - Steps Cucumber et implémentation
5. **[04-gherkin-features.md](04-gherkin-features.md)** - Features et scénarios Gherkin
6. **[05-hooks-lifecycle.md](05-hooks-lifecycle.md)** - Hooks et cycle de vie des tests

### 🎨 Développement UI
7. **[06-html-development.md](06-html-development.md)** - Bonnes pratiques HTML/CSS/JS

### ⚙️ Configuration & Outils
8. **[07-configuration.md](07-configuration.md)** - Configuration YAML et environnement
9. **[08-maven-commands.md](08-maven-commands.md)** - Commandes Maven et build

### ✅ Qualité
10. **[09-best-practices.md](09-best-practices.md)** - Bonnes pratiques et conventions

---

## 🎯 Utilisation

Chaque fichier est autonome et peut être consulté indépendamment selon le contexte :

- **Nouveau test à créer** → Consulter 02, 03, 04
- **Problème de sélecteur** → Consulter 02, 09
- **Nouvelle page HTML** → Consulter 06
- **Configuration** → Consulter 07
- **Build/CI** → Consulter 08

---

## 🔄 Mise à Jour

Cette documentation doit être maintenue à jour avec l'évolution du projet. Pour ajouter ou modifier :

1. Éditer le fichier concerné
2. Mettre à jour ce README si nécessaire
3. Dater les modifications importantes

---

## 📋 Quick Reference

### Créer un Nouveau Test Complet

1. **Feature Gherkin** (`src/main/resources/features/`) → [04-gherkin-features.md](04-gherkin-features.md)
2. **Page Object** (`fr.axa.automation.model/`) → [02-page-object-model.md](02-page-object-model.md)
3. **Step Definition** (`fr.axa.automation.step/`) → [03-step-definitions.md](03-step-definitions.md)
4. **Exécution** → [08-maven-commands.md](08-maven-commands.md)

### Créer une Page HTML de Test

1. **Structure HTML** → [06-html-development.md](06-html-development.md)
2. **Ajouter ID/Name** → [06-html-development.md](06-html-development.md#identifiants-et-sélecteurs)
3. **Valider accessibilité** → [06-html-development.md](06-html-development.md#accessibilité)

---

**Note** : Ce fichier sert de point d'entrée. Consultez les fichiers individuels pour des détails spécifiques.

---

**Dernière mise à jour** : 2025-01-23

