# Список пользователей

Минимальный CRUD API на Spring Boot для управления списком пользователей с ролевым
контролем доступа. Веб-интерфейс отсутствует — только API.

## Стек

- Java 21
- Spring Boot 4.1.0 (Web MVC, Spring Data JPA, Spring Security, Bean Validation)
- PostgreSQL 18 (`postgres:18-trixie`)
- Maven (сборка только внутри Docker — локальная установка Java/Maven не требуется)
- Docker / Docker Compose

## Как это работает

- Каждый пользователь имеет роль `ADMIN` или `USER`.
- **Первый пользователь-администратор** создается автоматически при запуске приложения

*если указаны `ADMIN_USERNAME` / `ADMIN_PASSWORD` — инициализация приложения
необязательна, и приложение запускается нормально без них (пропуская инициализацию и
регистрируя ее). Это позволяет выполнить отдельный разовый шаг (например, задание миграции Kubernetes — см. [`k8s/`](k8s/)) по созданию собственного администратора вместо
создания учетных данных администратора для каждого экземпляра приложения.

- Аутентификация — HTTP Basic, без сохранения состояния (без сессий/cookie).
- Пароли хранятся в виде хешей BCrypt и никогда не возвращаются API.
- Запросы `GET /actuator/health/liveness` и `GET /actuator/health/readiness`

(Spring Boot Actuator) открыты без аутентификации и используются в качестве
проверок работоспособности/готовности контейнера; все остальные конечные точки остаются за
HTTP Basic аутентификацией, как указано выше.

### Правила авторизации

| Конечная точка | Метод | Кто |

|---|---|---|
| `/api/users` | GET | любой аутентифицированный пользователь |
| `/api/users/{id}` | GET | любой авторизованный пользователь |
| `/api/users` | POST | только для администраторов |
| `/api/users/{id}` | PUT | только для администраторов |
| `/api/users/{id}` | DELETE | только для администраторов |

Обычные пользователи могут просматривать список всех пользователей, но не могут создавать, изменять или удалять
кого-либо (включая себя). Только администратор может управлять пользователями.

## Запуск с Docker

```
cp .env.example .env

docker compose up -d --build
```

## Kubernetes

Сначала соберите и опубликуйте образ приложения:

```bash
docker build --target runtime -t jardineiro/otus:hw15 ..
```

Затем загрузите его в собственный демон Docker Minikube (по той же логике, что и для hw12 —
образы Docker хоста и внутреннее хранилище образов Minikube разделены):

```bash
minikube image load jardineiro/otus:hw15
```

```bash
# 1. Секреты — скопируйте примеры и сначала заполните реальные значения
cp k8s/manifests/secret-db-credentials.example.yaml k8s/manifests/secret-db-credentials.yaml
cp k8s/manifests/secret-admin-credentials.example.yaml k8s/manifests/secret-admin-credentials.yaml
# отредактируйте оба файла, затем:
kubectl apply -f k8s/manifests/secret-db-credentials.yaml -f k8s/manifests/secret-admin-credentials.yaml
kubectl apply -f k8s/manifests/configmap.yaml

# 2. База данных (диаграмма Helm считывает секрет, примененный выше)
helm install userlist-db k8s/helm/postgresql -f k8s/helm/postgresql/values.yaml
kubectl rollout status statefulset/userlist-postgresql

# 3. Задание миграции — создает схему и первого пользователя-администратора, затем завершает работу
kubectl apply -f k8s/manifests/job-migrate.yaml
kubectl wait --for=condition=complete job/userlist-migrate --timeout=120s

# 4. Приложение
kubectl apply -f k8s/manifests/deployment.yaml -f k8s/manifests/service.yaml -f k8s/manifests/ingress.yaml
kubectl rollout status deployment/userlist
```

```bash
curl -H "Host: arch.homework" http://localhost:8080/api/users
# 401 - нет учетных данных

curl -H "Host: arch.homework" -u admin:<ADMIN_PASSWORD> http://localhost:8080/api/users
# 200 - [{"id":1,"username":"admin",...,"role":"ADMIN"}]
```

```bash
kubectl delete -f k8s/manifests/deployment.yaml -f k8s/manifests/service.yaml -f k8s/manifests/ingress.yaml
kubectl delete -f k8s/manifests/job-migrate.yaml
helm uninstall userlist-db
kubectl delete -f k8s/manifests/configmap.yaml
kubectl delete -f k8s/manifests/secret-db-credentials.yaml -f k8s/manifests/secret-admin-credentials.yaml
```