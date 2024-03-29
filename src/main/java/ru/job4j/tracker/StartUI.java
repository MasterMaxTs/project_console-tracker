package ru.job4j.tracker;

import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.tracker.actions.*;
import ru.job4j.tracker.input.ConsoleInput;
import ru.job4j.tracker.input.Input;
import ru.job4j.tracker.input.ValidateInput;
import ru.job4j.tracker.output.ConsoleOutput;
import ru.job4j.tracker.output.Output;
import ru.job4j.tracker.trackers.HbmTracker;
import ru.job4j.tracker.trackers.MemTracker;
import ru.job4j.tracker.trackers.Store;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

/**
 * Главный класс приложения
 */
public class StartUI {

    /**
     * Зависимость от интерфейса отображения данных
     */
    private final Output out;

    /**
     * Конструктор
     * @param out реализация отображения данных
     */
    public StartUI(Output out) {
        this.out = out;
    }

    /**
     * Инициализирует интерфейс консольного приложения
     * @param input реализация интерфейса ввода данных на входе
     * @param tracker реализация хранилища заявок на входе
     * @param actions реализации действий пользователя в меню приложения
     * в виде списка на входе
     */
    public void init(Input input, Store tracker, List<UserAction> actions) {
        this.printLogo();
        boolean run = true;
        while (run) {
            this.showMenu(actions);
            int select = input.askInt("Select: ");
            if (select < 0 || select >= actions.size()) {
                out.println("Wrong input, you can select: 0 .. " + (actions.size() - 1));
                continue;
            }
            UserAction action = actions.get(select);
            run = action.execute(input, tracker);
        }
    }

    /**
     * Печатает в консоли логотип приложения
     * @return логотип приложения в виде строки
     */
    public String printLogo() {
        String name = "Console Tracker";
        String line = "********************";
        String ln = System.lineSeparator();
        String logo = line + ln + name + ln + line;
        out.println(logo);
        return logo;
    }

    /**
     * Отображает текстовое меню в консоли приложения
     * @param actions реализации действий пользователя в меню приложения
     * в виде списка
     */
    private void showMenu(List<UserAction> actions) {
        out.println("Menu.");
        for (int i = 0; i < actions.size(); i++) {
            out.println(i + ". " + actions.get(i).name());
        }
    }

    /**
     * Возвращает значение системной переменной по имени
     * @param sysEnv название системной переменной на входе
     * @param key название ключа из файла настройки приложения
     * classpath: app.properties
     * @param config объект конфигурации приложения в виде Properties
     * @return значение существующей системной переменной по имени
     * иначе-значение по ключу из объекта конфигурации
     */
    private static String loadSysEnvIfNullThenConfig(String sysEnv,
                                                     String key,
                                                     Properties config) {
        String value = System.getenv(sysEnv);
        if (value == null) {
            value = config.getProperty(key);
        }
        return value;
    }

    /**
     * Создает соединение c хранилищем заявок в БД с помощью Jdbc
     * @return объект Connection
     */
    private static Connection loadConnection()
                                    throws ClassNotFoundException, SQLException {
        var config = new Properties();
        try (InputStream in = StartUI.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            config.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        String url = loadSysEnvIfNullThenConfig("JDBC_URL", "url", config);
        String username = loadSysEnvIfNullThenConfig("JDBC_USERNAME", "username", config);
        String password = loadSysEnvIfNullThenConfig("JDBC_PASSWORD", "password", config);
        String driver = loadSysEnvIfNullThenConfig("JDBC_DRIVER", "driver-class-name", config);
        System.out.println("url=" + url);
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Производит предварительную настройку для соединения c хранилищем
     * заявок в БД с помощью Hibernate
     * @return объект StandardServiceRegistry
     */
    private static StandardServiceRegistry buildRegistry() {
        var config = new Properties();
        try (InputStream in = StartUI.class.getClassLoader()
                .getResourceAsStream("app.properties")) {
            config.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Map<String, String> settings = new HashMap<>();
        settings.put("hibernate.connection.url",
                loadSysEnvIfNullThenConfig("JDBC_URL", "url", config));
        settings.put("hibernate.connection.username",
                loadSysEnvIfNullThenConfig("JDBC_USERNAME", "username", config));
        settings.put("hibernate.connection.password",
                loadSysEnvIfNullThenConfig("JDBC_PASSWORD", "password", config));
        settings.put("hibernate.connection.driver_class",
                loadSysEnvIfNullThenConfig("JDBC_DRIVER", "driver-class-name", config));
        settings.put("hibernate.connection.pool_size", "10");
        settings.put("hibernate.current_session_context_class", "thread");
        settings.put("hibernate.show_sql", "false");
        settings.put("format_sql", "true");
        settings.put("use_sql_comments", "true");
        settings.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
        settings.put("hibernate.hbm2ddl.auto", "none");
        return new StandardServiceRegistryBuilder().applySettings(settings).build();
    }

    /**
     * Главный исполняемый метод приложения
     * @param args массив из аргументов командной строки
     */
    public static void main(String[] args) throws Exception {
        Output output = new ConsoleOutput();
        Input input =
                new ValidateInput(output, new ConsoleInput());
        try (Store tracker = new HbmTracker(buildRegistry())) {
            tracker.init();
            List<UserAction> actions = new ArrayList<>();
            actions.add(new CreateAction(output));
            actions.add(new ShowAllAction(output));
            actions.add(new ReplaceAction(output));
            actions.add(new DeleteAction(output));
            actions.add(new FindItemByIdAction(output));
            actions.add(new FindItemByNameAction(output));
            actions.add(new Exit(output));
            new StartUI(output).init(input, tracker, actions);
        }
    }
}
