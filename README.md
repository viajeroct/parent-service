1. Три сервиса: inventory-service, order-service, product-service.
Первый и второй используют MySql, последний - MongoDB.
В product-service написаны тесты с помощью MockMvc.
Базы данных запускаются в docker контейнерах, скрипты расположены в папке scripts.

2. Добавлено взаимодействие между inventory и product сервисами.
Посылается синхронный http запрос.
