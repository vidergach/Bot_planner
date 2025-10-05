package org.example;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Обработчик сообщений для Telegram бота планировщика задач.
 * Управляет задачами пользователей, обеспечивает изоляцию данных между пользователями.
 * Поддерживает многопоточный доступ через ConcurrentHashMap.
 *
 * @see UserTasks
 * @see ConcurrentHashMap
 */

public class MessageHandler {

    private final ConcurrentHashMap<String, UserTasks> userDataMap = new ConcurrentHashMap<>();
    /**
     * Обрабатывает ввод пользователя и возвращает ответ.
     * Автоматически создает хранилище данных для нового пользователя.
     *
     * @param userInput текст сообщения от пользователя
     * @param userId уникальный идентификатор пользователя
     * @return текстовый ответ бота
     */
    public String processUserInput(String userInput, String userId) {
        System.out.println("сообщение: " + userInput + " от: " + userId);

        // Получаем данные пользователя
        UserTasks userTasks = userDataMap.computeIfAbsent(userId, k -> new UserTasks());

        String outputText = processCommand(userInput, userTasks);
        System.out.println("Ответ: " + outputText);
        return outputText;
    }
    /**
     * Определяет и выполняет команду пользователя.
     *
     * @param userInput текст команды
     * @param userTasks данные пользователя
     * @return результат выполнения команды
     */
    private String processCommand(String userInput, UserTasks userTasks) {
        if ("/start".equals(userInput)) {
            return startMessage();
        } else if ("/help".equals(userInput)) {
            return helpMessage();
        } else if(userInput.startsWith("/done")){
            return markTaskDone(userInput, userTasks);
        } else if("/dTask".equals(userInput)) {
            return donedTasks(userTasks);
        }
        else if(userInput.startsWith("/add")){
            return addTask(userInput, userTasks);
        } else if("/tasks".equals(userInput)){
            return showTasks(userTasks);
        }
        else if(userInput.startsWith("/delete")){
            return deleteTask(userInput, userTasks);
        }
        else {
            return "Неизвестная команда.\n" +
                    "Введите /help для просмотра доступных команд.";
        }
    }

    /**
     * Отмечает задачу как выполненную.
     * Перемещает задачу из списка текущих в список выполненных.
     *
     * @param userInput текст команды с задачей
     * @param userTasks данные пользователя
     * @return сообщение о результате операции
     */
    private String markTaskDone(String userInput, UserTasks userTasks) {
        if (userInput.length() <= 6) {
            return "Упс\uD83D\uDE05, похоже вы " +
                    "забыли указать задачу после команды /done \n" +
                    "Например: /done Полить цветы";
        }
        String task = userInput.substring(6).trim();
        if (!userTasks.getTasks().contains(task)) {
            return "Задача \"" + task + "\" не найдена в списке!";
        }
        userTasks.getTasks().remove(task);
        userTasks.getCompletedTasks().add(task);
        return "Задача \"" + task + "\" отмечена выполненной!";
    }

    /**
     * Показывает список выполненных задач пользователя.
     *
     * @param userTasks данные пользователя
     * @return форматированный список выполненных задач
     */
    private String donedTasks(UserTasks userTasks) {
        if (userTasks.getCompletedTasks().isEmpty()) {
            return "Список выполненных задач пуст!";
        }
        StringBuilder compl_tasks = new StringBuilder("✅ Вот список выполненных задач:\n");
        for (int i = 0; i < userTasks.getCompletedTasks().size(); i++) {
            compl_tasks.append("  ").append(i + 1).append(". ").append(userTasks.getCompletedTasks().get(i)).append(" ✔\n");
        }
        return compl_tasks.toString();
    }

    /**
     * Добавляет новую задачу в список пользователя.
     * Проверяет на дубликаты и пустые значения.
     *
     * @param userInput текст команды с задачей
     * @param userTasks данные пользователя
     * @return сообщение о результате добавления
     */
    private String addTask(String userInput, UserTasks userTasks) {
        if (userInput.length() <= 5) {
            return "Упс\uD83D\uDE05, похоже вы " +
                    "забыли указать задачу после команды /add \n" +
                    "Например: /add Полить цветы";
        }
        String task = userInput.substring(5).trim();
        if (task.isEmpty()) {
            return "Задача не может быть пустой!";
        }

        // Проверка на существующую задачу
        if (userTasks.getTasks().contains(task)) {
            return "Задача \"" + task + "\" уже есть в списке!";
        }

        userTasks.getTasks().add(task);
        return "Задача \"" + task + "\" добавлена!";
    }
    /**
     * Показывает список текущих задач пользователя.
     *
     * @param userTasks данные пользователя
     * @return форматированный список задач
     */
    private String showTasks(UserTasks userTasks) {
        if (userTasks.getTasks().isEmpty())
            return "Список задач пуст!";
        StringBuilder list_tasks = new StringBuilder("Вот список ваших задач:\n");
        for (int i = 0; i < userTasks.getTasks().size(); i++) {
            list_tasks.append("").append(i + 1).append(". ").append(userTasks.getTasks().get(i)).append("\n");
        }
        return list_tasks.toString();
    }

    /**
     * Удаляет задачу из списка пользователя.
     *
     * @param userInput текст команды с задачей
     * @param userTasks данные пользователя
     * @return сообщение о результате удаления
     */
    private String deleteTask(String userInput, UserTasks userTasks) {
        if (userInput.length() <= 8) {
            return "Упс\uD83D\uDE05, похоже вы забыли указать задачу после команды /delete.\n" +
                    "Например: /delete Полить цветы";
        }
        String task = userInput.substring(8).trim();
        if (!userTasks.getTasks().contains(task)) {
            return "Задача \"" + task + "\" не найдена в списке!";
        }
        userTasks.getTasks().remove(task);
        return "🗑️ Задача \"" + task + "\" удалена из списка задач!";
    }


    /**
     * Возвращает приветственное сообщение со списком команд.
     *
     * @return приветственное сообщение
     */
    private String startMessage () {
        return "Добро пожаловать в планировщик задач! \uD83D\uDC31 📝 \n" +
                "Я могу организовывать ваши задачи.\n" +
                "Команды: \n" +
                "/add - добавить задачу\n" +
                "/tasks - показать список задач\n" +
                "/done - отметить выполненной\n"+
                "/dTask - список выполненных задач\n" +
                "/delete - удалить задачу\n"+
                "/help - помощь\n";
        }
    /**
     * Возвращает подробную справку по командам с примерами использования.
     *
     * @return справочное сообщение
     */
    private String helpMessage () {
        return "Справка по работе:\n" +
                "Я планировщик задач😊 📝\n" +
                "Мои команды: \n" +
                "/add - добавить задачу\n" +
                "/tasks - показать список задач\n" +
                "/done - отметить выполненной\n"+
                "/dTask - список выполненных задач\n" +
                "/delete - удалить задачу\n"+
                "/help - помощь\n"+
                "\n"+
                "Например: \n"+
                "/add Полить цветы\n" +
                "- Задача \"Полить цветы\" добавлена!\n\n" +
                "/add Накормить кота\n" +
                "- Задача \"Накормить кота\" добавлена!\n\n" +
                "/add Полить цветы\n" +
                "- Задача \"Полить цветы\" уже есть в списке!\n\n" +
                "/tasks\n" +
                "- Вот список ваших задач:\n" +
                "  1. Полить цветы\n" +
                "  2. Накормить кота\n\n" +
                "/done Полить цветы\n" +
                "- Задача \"Полить цветы\" отмечена выполненной!\n\n" +
                "/dTask\n" +
                "- ✅ Вот список выполненных задач:\n" +
                "  1. Полить цветы ✔\n\n" +
                "/delete Накормить кота\n" +
                "- 🗑️ Задача \"Накормить кота\" удалена из списка задач!";
        }
    }