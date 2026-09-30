# OBVX Backend

Backend REST API de la plateforme **OBVX**, développé avec **Spring Boot**.

Le backend fournit actuellement les fonctionnalités nécessaires à la gestion de l'authentification, des utilisateurs, des rôles, des catégories, des produits, des images produits et du panier utilisateur.

Les fonctionnalités e-commerce avancées telles que les commandes, les paiements, l'historique des commandes et la gestion avancée du stock restent à développer.

---

## 🚀 Technologies utilisées

- **Java 17** — version configurée dans Maven
- **Spring Boot 4.1.1**
- **Spring Security**
- **JWT**
- **Spring Data JPA**
- **Hibernate**
- **PostgreSQL**
- **Supabase PostgreSQL**
- **Supabase Storage**
- **Jakarta Validation**
- **Lombok**
- **Maven**
- **Docker**
- **Postman**
- **JUnit 5**
- **Mockito**
- **AssertJ**
- **Spring MockMvc**

> Le `Dockerfile` utilise actuellement une image **Eclipse Temurin 25 JDK**, tandis que le `pom.xml` configure Java 17 pour la compilation. Cette différence devra être harmonisée ultérieurement.

---

# 📁 Architecture du projet

```text
src/
├── main/
│   ├── java/
│   │   └── obvx/com/backend/
│   │       │
│   │       ├── BackendApplication.java
│   │       │
│   │       ├── config/
│   │       │   ├── JwtAuthenticationFilter.java
│   │       │   ├── SecurityConfig.java
│   │       │   └── SupabaseConfig.java
│   │       │
│   │       ├── controller/
│   │       │   ├── AdminController.java
│   │       │   ├── AuthController.java
│   │       │   ├── CartController.java
│   │       │   ├── CategoryController.java
│   │       │   ├── ProductsController.java
│   │       │   └── UserController.java
│   │       │
│   │       ├── dto/
│   │       │   ├── AddToCartRequest.java
│   │       │   ├── AdminRequest.java
│   │       │   ├── AuthResponse.java
│   │       │   ├── CartItemResponse.java
│   │       │   ├── CartResponse.java
│   │       │   ├── CategoryRequest.java
│   │       │   ├── LoginRequest.java
│   │       │   ├── ProductRequest.java
│   │       │   ├── ProductResponse.java
│   │       │   ├── RegisterRequest.java
│   │       │   ├── UpdateCartItemRequest.java
│   │       │   ├── UpdateUserRequest.java
│   │       │   └── UserResponse.java
│   │       │
│   │       ├── entity/
│   │       │   ├── Cart.java
│   │       │   ├── CartItem.java
│   │       │   ├── Category.java
│   │       │   ├── Products.java
│   │       │   ├── Role.java
│   │       │   └── User.java
│   │       │
│   │       ├── exception/
│   │       │   ├── ErrorResponse.java
│   │       │   ├── GlobalExceptionHandler.java
│   │       │   ├── RessourceAlreadyExistsException.java
│   │       │   └── RessourceNotFoundException.java
│   │       │
│   │       ├── repository/
│   │       │   ├── CartItemRepository.java
│   │       │   ├── CartRepository.java
│   │       │   ├── CategoryRepository.java
│   │       │   ├── ProductRepository.java
│   │       │   └── UserRepository.java
│   │       │
│   │       └── service/
│   │           ├── AdminService.java
│   │           ├── AuthService.java
│   │           ├── CartService.java
│   │           ├── CategoryService.java
│   │           ├── JwtService.java
│   │           ├── ProductsService.java
│   │           ├── SupabaseStorageService.java
│   │           └── UserService.java
│   │
│   └── resources/
│       └── application.properties
│
└── test/
    └── java/
        └── obvx/com/backend/
            ├── service/
            ├── controller/
            ├── config/
            ├── exception/
            └── BackendApplicationTests.java
```

---

# 🔐 Authentification

L'API utilise **Spring Security** et des **JWT**.

Le système actuel permet :

- l'inscription ;
- la connexion ;
- le hashage des mots de passe ;
- la génération d'un JWT ;
- la validation du JWT ;
- l'identification de l'utilisateur connecté ;
- la récupération du rôle depuis l'utilisateur ;
- la protection des endpoints.

Le filtre `JwtAuthenticationFilter` intercepte les requêtes contenant :

```http
Authorization: Bearer <token>
```

Il récupère l'email du token, recherche l'utilisateur correspondant et place son authentification dans le `SecurityContext`.

---

# 📝 Inscription

```http
POST /api/auth/register
```

### Requête

```json
{
  "name": "Christ Amien",
  "email": "amien@example.com",
  "password": "password123"
}
```

Contraintes :

- `name` obligatoire ;
- `email` obligatoire et valide ;
- mot de passe d'au moins **8 caractères**.

Un nouvel utilisateur reçoit automatiquement le rôle :

```text
CUSTOMER
```

### Réponse

```json
{
  "id": 1,
  "name": "Christ Amien",
  "email": "amien@example.com",
  "role": "CUSTOMER"
}
```

Le mot de passe est hashé avant son enregistrement en base de données.

---

# 🔑 Connexion

```http
POST /api/auth/login
```

### Requête

```json
{
  "email": "amien@example.com",
  "password": "password123"
}
```

### Réponse

```json
{
  "id": 1,
  "name": "Christ Amien",
  "email": "amien@example.com",
  "role": "CUSTOMER",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

Le token doit ensuite être envoyé avec :

```http
Authorization: Bearer <token>
```

---

# 🎫 JWT

Le JWT est généré par `JwtService`.

Le token contient notamment :

```text
subject → email
id
name
role
issuedAt
expiration
```

La durée de validité actuelle est de **24 heures**.

Le flux d'authentification est :

```text
Client
   ↓
Authorization: Bearer <JWT>
   ↓
JwtAuthenticationFilter
   ↓
JwtService
   ↓
UserRepository
   ↓
User
   ↓
SecurityContext
```

---

# 🛡️ Sécurité

La configuration principale se trouve dans :

```text
SecurityConfig.java
```

Le projet active également la sécurité au niveau des méthodes avec :

```java
@EnableMethodSecurity
```

Les routes d'authentification sont accessibles sans JWT :

```text
/auth/register
/auth/login
```

Toutes les autres requêtes nécessitent une authentification.

Les fonctionnalités administrateur utilisent :

```java
@PreAuthorize("hasRole('ADMIN')")
```

Les autorités utilisées sont :

```text
ROLE_ADMIN
ROLE_CUSTOMER
```

---

# 👤 Rôles

Deux rôles sont actuellement définis :

```text
ADMIN
CUSTOMER
```

## CUSTOMER

Un utilisateur authentifié peut :

- consulter les catégories ;
- consulter les produits ;
- consulter son profil ;
- modifier son profil ;
- consulter son panier ;
- ajouter un produit au panier ;
- modifier la quantité d'un article ;
- supprimer un article ;
- vider son panier.

## ADMIN

Un administrateur peut également :

- créer une catégorie ;
- modifier une catégorie ;
- supprimer une catégorie ;
- créer un produit ;
- modifier un produit ;
- supprimer un produit ;
- créer un autre administrateur.

---

# 👑 Administration

## Créer un administrateur

```http
POST /api/admin
```

Accès :

```text
ADMIN uniquement
```

### Requête

```json
{
  "name": "Admin OBVX",
  "email": "admin@obvx.com",
  "password": "password123"
}
```

La requête utilise les mêmes contraintes de validation que l'inscription.

Un utilisateur `CUSTOMER` ne peut pas accéder à cette route.

---

# 👤 Profil utilisateur

## Consulter son profil

```http
GET /api/users/me
```

Authentification requise.

### Réponse

```json
{
  "id": 1,
  "name": "Christ Amien",
  "email": "amien@example.com",
  "role": "CUSTOMER"
}
```

L'utilisateur connecté est récupéré directement depuis :

```java
Authentication authentication
```

---

## Modifier son profil

```http
PUT /api/users/me
```

### Requête

```json
{
  "name": "Christ Amien Updated",
  "email": "christ@obvx.com"
}
```

Le rôle n'est pas modifiable avec cette route.

---

# 📂 Catégories

Base URL :

```text
/api/category
```

| Méthode | Endpoint | Accès |
|---|---|---|
| GET | `/api/category` | Authentifié |
| GET | `/api/category/{id}` | Authentifié |
| POST | `/api/category` | ADMIN |
| PUT | `/api/category/{id}` | ADMIN |
| DELETE | `/api/category/{id}` | ADMIN |

### Création

```http
POST /api/category
```

Exemple :

```json
{
  "name": "T-Shirts"
}
```

Une catégorie ne peut pas être supprimée lorsqu'elle possède encore des produits associés.

---

# 👕 Produits

Base URL :

```text
/api/products
```

Les produits possèdent actuellement :

```text
id
name
description
price
stock
imageUrl
category
```

Les montants sont représentés avec :

```java
BigDecimal
```

## Endpoints

| Méthode | Endpoint | Accès |
|---|---|---|
| GET | `/api/products` | Authentifié |
| GET | `/api/products/{id}` | Authentifié |
| POST | `/api/products` | ADMIN |
| PUT | `/api/products/{id}` | ADMIN |
| DELETE | `/api/products/{id}` | ADMIN |

---

# 🖼️ Images des produits

Les images sont stockées dans **Supabase Storage**.

La configuration actuelle utilise :

```properties
supabase.url=${SUPABASE_URL}
supabase.key=${SUPABASE_KEY}
supabase.bucket=products-image
```

Le flux est :

```text
Frontend
   ↓
Spring Boot
   ↓
Supabase Storage
   ↓
URL publique de l'image
   ↓
PostgreSQL
```

L'URL de l'image est ensuite enregistrée dans :

```text
products.imageUrl
```

---

## 📤 Création d'un produit

La création utilise :

```text
multipart/form-data
```

avec deux parties :

```text
products → JSON du produit
image    → fichier image
```

Endpoint :

```http
POST /api/products
```

Authentification :

```text
ADMIN
```

### Exemple du JSON `products`

```json
{
  "name": "OBVX T-Shirt",
  "description": "T-shirt OBVX",
  "price": 25000,
  "stock": 20,
  "categoryId": 1
}
```

L'image est obligatoire lors de la création.

---

## 🔄 Modification d'un produit

```http
PUT /api/products/{id}
```

Authentification :

```text
ADMIN
```

La requête utilise également :

```text
multipart/form-data
```

Le JSON contient les informations du produit :

```json
{
  "name": "OBVX T-Shirt Updated",
  "description": "Nouveau descriptif",
  "price": 28000,
  "stock": 15,
  "categoryId": 1
}
```

L'image est facultative.

Si une nouvelle image est fournie :

```text
Ancienne image
      ↓
Upload nouvelle image
      ↓
Mise à jour imageUrl
      ↓
Suppression ancienne image
```

---

## 🗑️ Suppression d'un produit

```http
DELETE /api/products/{id}
```

Avant de supprimer le produit de PostgreSQL, le backend tente également de supprimer son image dans Supabase Storage.

---

# 🛒 Panier

Chaque utilisateur authentifié possède son propre panier.

Architecture :

```text
User
  │
  └── Cart
       │
       ├── CartItem
       │    └── Product
       │
       └── CartItem
            └── Product
```

Le panier contient :

- un utilisateur ;
- plusieurs articles ;
- un produit par article ;
- une quantité par article.

---

## 🛒 Consulter son panier

```http
GET /api/cart
```

Si l'utilisateur n'a pas encore de panier, le backend en crée automatiquement un.

### Exemple

```json
{
  "id": 1,
  "items": [
    {
      "id": 1,
      "productId": 5,
      "name": "OBVX T-Shirt",
      "price": 25000,
      "imageUrl": "https://...",
      "quantity": 2,
      "subtotal": 50000
    }
  ],
  "total": 50000
}
```

---

## ➕ Ajouter un produit

```http
POST /api/cart/items
```

### Requête

```json
{
  "productId": 5,
  "quantity": 2
}
```

La quantité doit être supérieure à `0`.

Si le produit existe déjà dans le panier, la quantité est augmentée.

Exemple :

```text
T-Shirt × 2
+
T-Shirt × 3
=
T-Shirt × 5
```

---

## 🔄 Modifier la quantité

```http
PUT /api/cart/items/{cartItemId}
```

### Requête

```json
{
  "quantity": 5
}
```

La quantité doit être supérieure à `0`.

Le backend vérifie que l'article appartient bien au panier de l'utilisateur connecté.

---

## 🗑️ Supprimer un article

```http
DELETE /api/cart/items/{cartItemId}
```

Aucun body n'est nécessaire.

Le backend vérifie que l'article appartient au panier de l'utilisateur connecté.

---

## 🧹 Vider le panier

```http
DELETE /api/cart
```

Tous les `CartItem` sont supprimés.

Le panier lui-même est conservé.

Cette opération utilise une transaction :

```java
@Transactional
```

---

## 💰 Calcul du panier

Le sous-total est calculé avec :

```text
prix × quantité
```

Le total correspond à la somme des sous-totaux.

Les valeurs monétaires utilisent :

```java
BigDecimal
```

---

# 🗄️ Base de données

Le backend utilise :

```text
PostgreSQL
```

avec une connexion configurée vers :

```text
Supabase PostgreSQL
```

Architecture :

```text
Spring Boot
     ↓
Spring Data JPA
     ↓
Hibernate
     ↓
PostgreSQL
     ↓
Supabase
```

La stratégie Hibernate actuellement configurée est :

```properties
spring.jpa.hibernate.ddl-auto=update
```

---

# 🔐 Variables d'environnement

Les informations sensibles sont fournies par des variables d'environnement.

```env
SUPABASE_DB_HOST=your_host
SUPABASE_DB_PORT=5432
SUPABASE_DB_NAME=postgres
SUPABASE_DB_USER=your_user
SUPABASE_DB_PASSWORD=your_password

SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key

JWT_SECRET=your_jwt_secret
```

Le fichier `.env` local est chargé grâce à :

```properties
spring.config.import=optional:file:./.env[.properties]
```

Le fichier `.env` ne doit jamais être publié sur GitHub.

---

# ⚠️ Gestion des exceptions

Les erreurs sont centralisées avec :

```text
GlobalExceptionHandler
```

Exceptions personnalisées :

```text
RessourceNotFoundException
RessourceAlreadyExistsException
```

Les validations Jakarta Validation sont également traitées globalement.

Codes HTTP utilisés notamment :

| Code | Signification |
|---|---|
| 400 | Données invalides |
| 401 | Authentification requise |
| 403 | Accès interdit |
| 404 | Ressource introuvable |
| 409 | Conflit / ressource existante / règle métier |

---

# 🐳 Docker

Le projet possède un `Dockerfile`.

Il utilise actuellement :

```dockerfile
FROM eclipse-temurin:25-jdk
```

Construction :

```bash
docker build -t obvx-backend .
```

Lancement :

```bash
docker run --rm -p 8080:8080 --env-file .env obvx-backend
```

Le conteneur expose :

```text
8080
```

Le port Spring Boot est configurable avec :

```properties
server.port=${PORT:8080}
```

Cette configuration permet notamment d'utiliser un environnement de déploiement fournissant dynamiquement la variable `PORT`.

---

# ⚙️ Configuration actuelle

Le fichier principal est :

```text
src/main/resources/application.properties
```

Configuration actuelle :

```properties
spring.config.import=optional:file:./.env[.properties]

spring.application.name=obvx

spring.datasource.url=jdbc:postgresql://${SUPABASE_DB_HOST}:${SUPABASE_DB_PORT}/${SUPABASE_DB_NAME}?sslmode=require
spring.datasource.username=${SUPABASE_DB_USER}
spring.datasource.password=${SUPABASE_DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

supabase.url=${SUPABASE_URL}
supabase.key=${SUPABASE_KEY}
supabase.bucket=products-image

server.servlet.context-path=/api

server.port=${PORT:8080}

jwt.secret=${JWT_SECRET}
```

L'API est donc accessible localement à partir de :

```text
http://localhost:8080/api
```

---

# 🧪 Tests automatisés

Le projet possède une suite de tests unitaires couvrant principalement :

- services ;
- contrôleurs ;
- JWT ;
- filtre d'authentification ;
- gestion globale des exceptions ;
- chargement du contexte Spring Boot.

Technologies utilisées :

- JUnit 5
- Mockito
- AssertJ
- Spring MockMvc

La liste actuellement documentée représente **90 tests**.

### Services

```text
AdminServiceTest
AuthServiceTest
CartServiceTest
CategoryServiceTest
JwtServiceTest
ProductsServiceTest
SupabaseStorageServiceTest
UserServiceTest
```

### Contrôleurs

```text
AdminControllerTest
AuthControllerTest
CartControllerTest
CategoryControllerTest
ProductsControllerTest
UserControllerTest
```

### Sécurité

```text
JwtAuthenticationFilterTest
```

### Exceptions

```text
GlobalExceptionHandlerTest
```

### Application

```text
BackendApplicationTests
```

---

## 🚀 Exécuter les tests

Tous les tests :

```bash
./mvnw test
```

Windows :

```powershell
.\mvnw.cmd test
```

Tests des services :

```bash
./mvnw test -Dtest="*ServiceTest"
```

Tests des contrôleurs :

```bash
./mvnw test -Dtest="*ControllerTest"
```

Test spécifique :

```bash
./mvnw test -Dtest=CartServiceTest
```

---

# 📌 Endpoints disponibles

| Méthode | Endpoint | Accès |
|---|---|---|
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| POST | `/api/admin` | ADMIN |
| GET | `/api/users/me` | Authentifié |
| PUT | `/api/users/me` | Authentifié |
| GET | `/api/category` | Authentifié |
| GET | `/api/category/{id}` | Authentifié |
| POST | `/api/category` | ADMIN |
| PUT | `/api/category/{id}` | ADMIN |
| DELETE | `/api/category/{id}` | ADMIN |
| GET | `/api/products` | Authentifié |
| GET | `/api/products/{id}` | Authentifié |
| POST | `/api/products` | ADMIN |
| PUT | `/api/products/{id}` | ADMIN |
| DELETE | `/api/products/{id}` | ADMIN |
| GET | `/api/cart` | Authentifié |
| POST | `/api/cart/items` | Authentifié |
| PUT | `/api/cart/items/{cartItemId}` | Authentifié |
| DELETE | `/api/cart/items/{cartItemId}` | Authentifié |
| DELETE | `/api/cart` | Authentifié |

---

# 🛣️ État actuel du projet

## Authentification & sécurité

- [x] Inscription
- [x] Connexion
- [x] Hashage des mots de passe
- [x] JWT
- [x] JwtAuthenticationFilter
- [x] Spring Security
- [x] Rôles `ADMIN` / `CUSTOMER`
- [x] Protection des endpoints
- [x] Création d'administrateurs
- [x] Protection au niveau des méthodes avec `@PreAuthorize`

## Utilisateurs

- [x] Consultation du profil
- [x] Modification du profil
- [x] Protection du profil
- [x] Gestion des rôles

## Catalogue

- [x] CRUD catégories
- [x] CRUD produits
- [x] Validation des données
- [x] Upload d'images
- [x] Stockage Supabase Storage
- [x] Suppression de l'ancienne image lors d'un remplacement
- [x] Suppression de l'image lors de la suppression du produit
- [x] Gestion globale des exceptions

## Panier

- [x] Création automatique du panier
- [x] Consultation du panier
- [x] Ajout de produits
- [x] Gestion des quantités
- [x] Modification de la quantité
- [x] Suppression d'un article
- [x] Vidage du panier
- [x] Calcul des sous-totaux
- [x] Calcul du total
- [x] Vérification de la propriété des articles
- [x] Transaction lors du vidage

## Infrastructure

- [x] PostgreSQL / Supabase
- [x] Supabase Storage
- [x] Variables d'environnement
- [x] Dockerisation
- [x] Configuration du port dynamique
- [ ] Harmonisation Java Maven / Docker

## Tests

- [x] Tests des services
- [x] Tests des contrôleurs
- [x] Tests JWT
- [x] Tests du filtre JWT
- [x] Tests du gestionnaire d'exceptions
- [x] Test de démarrage Spring Boot
- [ ] Tests d'intégration complets
- [ ] Tests end-to-end

---

# 🛒 Fonctionnalités e-commerce à développer

Les fonctionnalités suivantes ne sont **pas encore implémentées** dans le backend actuel :

- [ ] Gestion avancée du stock
- [ ] Commandes
- [ ] `Order`
- [ ] `OrderItem`
- [ ] Passage du panier vers une commande
- [ ] Historique des commandes
- [ ] Statuts des commandes
- [ ] Paiement
- [ ] Gestion des adresses de livraison
- [ ] Gestion de la livraison
- [ ] Annulation de commande
- [ ] Remboursement

---

# 🔒 Améliorations techniques prévues

- [ ] Harmoniser Java 17 / Java 25
- [ ] Sécuriser davantage les uploads
- [ ] Vérifier les types MIME des images
- [ ] Limiter la taille des fichiers
- [ ] Améliorer la gestion des erreurs Supabase
- [ ] Ajouter Swagger / OpenAPI
- [ ] Ajouter des tests d'intégration
- [ ] Ajouter des tests de sécurité
- [ ] Optimiser certaines requêtes JPA
- [ ] Ajouter une gestion structurée des logs
- [ ] Améliorer la configuration de production
- [ ] Ajouter une stratégie de migration de base de données

---

# 🌐 Frontend

Le frontend OBVX n'est pas inclus dans ce dépôt.

Il devra consommer l'API REST du backend pour :

- l'inscription ;
- la connexion ;
- l'authentification JWT ;
- l'affichage des catégories ;
- l'affichage des produits ;
- la gestion du panier ;
- la gestion du profil ;
- les fonctionnalités administrateur.

---

# 📈 Évolution prévue du backend

L'évolution du backend suivra progressivement cette architecture :

```text
                    ┌──────────────┐
                    │   Frontend   │
                    └──────┬───────┘
                           │
                           ▼
                    ┌──────────────┐
                    │ REST API     │
                    │ Spring Boot  │
                    └──────┬───────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
   Authentication      Catalogue         Panier
       JWT             Products           Cart
       Users           Category         CartItem
          │                │                │
          └────────────────┼────────────────┘
                           ▼
                    ┌──────────────┐
                    │ PostgreSQL   │
                    │  Supabase    │
                    └──────────────┘
                           │
                           ▼
                    ┌──────────────┐
                    │   Supabase   │
                    │   Storage    │
                    └──────────────┘
```

La prochaine grande étape fonctionnelle sera l'ajout du système de **commandes**, puis la gestion du stock, du paiement et du cycle de vie d'une commande.

---

# 👨‍💻 Auteur(s)

**Christ Amien** & **Ossey Yvan**

Projet **OBVX**.

---

## 📄 Repository

Le code source est disponible sur GitHub :

https://github.com/obvxindustry/obvx-backend