# Cash Manager Frontend

Frontend приложение для финансового дашборда, построенное на React + Vite + Tailwind CSS + React Router.

## Технологии

- **React 19** - UI библиотека
- **TypeScript** - типизация
- **Vite** - сборщик и dev server
- **Tailwind CSS** - utility-first CSS framework
- **React Router** - маршрутизация
- **React Query** - управление состоянием и кэширование
- **Axios** - HTTP клиент
- **Recharts** - библиотека для графиков
- **Zustand** - глобальное состояние

## Установка

```bash
npm install
```

## Запуск development сервера

```bash
npm run dev
```

Приложение будет доступно по адресу `http://localhost:5173`

## Сборка для production

```bash
npm run build
```

## Предпросмотр production сборки

```bash
npm run preview
```

## Структура проекта

```
src/
  components/     # Переиспользуемые компоненты
  pages/          # Страницы приложения
  hooks/          # Custom React hooks
  utils/          # Утилиты
  types/          # TypeScript типы
```

## Страницы

- `/` - редирект на `/dashboard`
- `/dashboard` - дашборд с графиками и статистикой
- `/transactions` - список транзакций
- `/import` - импорт файлов и изображений
- `/categories` - управление категориями
- `/reports` - отчеты
- `/settings` - настройки
- `/login` - вход
- `/register` - регистрация
