1. Три сервиса: inventory-service, order-service, product-service.
Первый и второй используют MySql, последний - MongoDB.
В product-service написаны тесты с помощью MockMvc.
Базы данных запускаются в docker контейнерах, скрипты расположены в папке scripts.

2. Добавлено взаимодействие между inventory и product сервисами.
Посылается синхронный http запрос.

3. Добавлен Discovery Server (Netflix Eureka), можно зайти по http://localhost:8761/ после запуска всех сервисов.

4. Добавлен API Gateway.

5. Реализация Event Driven архитектуры с помощью Kafka, отсылает уведомления для notification-service, когда заказ
послан в order-service.

6. Прочитал подробную статью на Habr о Mockito, в модуле mockito-learn есть примеры тестов.

7. Circuit Breaker pattern между order и inventory сервисами, используя Resilience4J.

8. Изучен механизм миграции БД с помощью Flyway.
