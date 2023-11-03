## [REST API](http://localhost:8080/doc)

## Концепция:

- Spring Modulith
    - [Spring Modulith: достигли ли мы зрелости модульности](https://habr.com/ru/post/701984/)
    - [Introducing Spring Modulith](https://spring.io/blog/2022/10/21/introducing-spring-modulith)
    - [Spring Modulith - Reference documentation](https://docs.spring.io/spring-modulith/docs/current-SNAPSHOT/reference/html/)

```
  url: jdbc:postgresql://localhost:5432/jira
  username: jira
  password: JiraRush
```

- Есть 2 общие таблицы, на которых не fk
    - _Reference_ - справочник. Связь делаем по _code_ (по id нельзя, тк id привязано к окружению-конкретной базе)
    - _UserBelong_ - привязка юзеров с типом (owner, lead, ...) к объекту (таска, проект, спринт, ...). FK вручную будем
      проверять

## Аналоги

- https://java-source.net/open-source/issue-trackers

## Тестирование

- https://habr.com/ru/articles/259055/

Список выполненных задач:
- №1. Разобраться со структурой проекта (onboarding).
- №2. Удалить социальные сети: vk, yandex.
- №3. Вынести чувствительную информацию в отдельный проперти файл
- №6. Сделать рефакторинг метода com.javarush.jira.bugtracking.attachment.FileUtil#upload чтоб он использовал современный подход для работы с файловой системмой.
- №9. Написать Dockerfile для основного сервера
- №8. Добавить подсчет времени сколько задача находилась в работе и тестировании. Написать 2 метода на уровне сервиса, которые параметром принимают задачу и возвращают затраченное время. Для написания этого задания, нужно добавить в конец скрипта инициализации базы данных changelog.sql 3 записи в таблицу ACTIVITY.
  (Так и не понял, как что-то можно вставить в скрипт changelog.sql, когда он защищен от этого и приложение падает при старте. Просто написал методы).
- №10. Написать docker-compose файл для запуска контейнера сервера вместе с БД и nginx. Для nginx используй конфиг-файл config/nginx.conf. При необходимости файл конфига можно редактировать. - Корявое задание. Пришлось еще править DI для бина src/main/java/com/javarush/jira/common/internal/config/RestAuthenticationEntryPoint.java Иначе почему-то в докере это падало и квалифайер не помогал, который там по-умолчанию.
Также в докере не получилось пофиксить, чтобы файлы прикреплялись, думаю тут дело с правами в самом контейнере. Локально всё работает ок на компьютере.)
Для запуска в докере выполнить docker-compose up -d
