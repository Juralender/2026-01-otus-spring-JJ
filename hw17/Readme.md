Задание:
Пределать веб-приложение каталога книг в библиотеке на использование архитектуроного стиля REST. Задание выполняется на базе предыдущей работы.


Требования к реализации:
Для получения данных на страницах приложения использовать JavaScript, fetch api и REST-контроллеры;
Минимум: переделать CRUD операции над книгами;
URL эндпойнтов должны соответствовать ресурсному стилю REST (см. пример типового api на соответствующем слайде презентации к лекции);
Действия над сущностями должны быть выражены исключительно через HTTP-методы. Глаголов в URL быть не должно;
Протестировать все эндпойнты REST-контроллеров с помощью @WebMvcTest и моков сервисов;
Без фанатизма)

## Docker

Локально приложение по умолчанию использует встроенную БД H2, а фронтенд собирается
отдельно скриптом `./build-frontend.sh`. В Docker-сборке фронтенд собирается внутри
образа, а хранилищем служит PostgreSQL.

```bash
cp .env.example .env

docker compose up -d --build
```

Приложение доступно на `http://localhost:8080` (API — под `/api/**`, остальное отдаётся
как статика фронтенда). Схема БД создаётся автоматически через Liquibase при старте.

Режим разработки с хот-релоадом бэкенда (`./build-frontend.sh`):

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build
```

## Kubernetes

Имя и тег образа заданы в одном месте — в `images:` секции
[`k8s/manifests/kustomization.yaml`](k8s/manifests/kustomization.yaml).

```bash
IMAGE=jardineiro/otus:hw17

docker build --target runtime -t "$IMAGE" .
```

Загрузите в демон Docker Minikube:

```bash
minikube image load "$IMAGE"
```

```bash
cp k8s/manifests/secret-db-credentials.example.yaml k8s/manifests/secret-db-credentials.yaml
kubectl apply -f k8s/manifests/secret-db-credentials.yaml

helm install library-db k8s/helm/postgresql -f k8s/helm/postgresql/values.yaml
kubectl rollout status statefulset/library-postgresql

kubectl apply -k k8s/manifests
kubectl rollout status deployment/library
```

```bash
curl -H "Host: arch.homework" http://localhost:8080/api/books
```

```bash
kubectl delete -k k8s/manifests
helm uninstall library-db
kubectl delete -f k8s/manifests/secret-db-credentials.yaml
```