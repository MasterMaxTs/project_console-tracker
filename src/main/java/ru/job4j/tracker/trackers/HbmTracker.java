package ru.job4j.tracker.trackers;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import ru.job4j.tracker.models.Item;

import java.util.List;

/**
 * Реализация доступа к хранилищу заявок в БД с помощью Hibernate
 */
public class HbmTracker implements Store, AutoCloseable {

    /**
     * Зависимость от объекта StandardServiceRegistry
     */
    private final StandardServiceRegistry registry;

    /**
     * Зависимость от объекта SessionFactory
     */
    private SessionFactory sf;

    /**
     * Конструктор
     * @param registry StandardServiceRegistry
     */
    public HbmTracker(StandardServiceRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void init() {
        MetadataSources metadataSources = new MetadataSources(registry);
        metadataSources.addAnnotatedClass(Item.class);
        sf = metadataSources.buildMetadata().buildSessionFactory();
    }

    @Override
    public Item add(Item item) {
        Session session = sf.openSession();
        session.beginTransaction();
        session.save(item);
        session.getTransaction().commit();
        session.close();
        return item;
    }

    @Override
    public boolean replace(int id, Item item) {
        Session session = sf.openSession();
        session.beginTransaction();
        boolean rsl = session.createQuery(
                        "update Item set name = :newName, description = "
                                + ":newDescription, created = :newCreated"
                                + " where id = :fId"
                ).setParameter("newName", item.getName())
                .setParameter("newDescription", item.getDescription())
                .setParameter("newCreated", item.getCreated())
                .setParameter("fId", id)
                .executeUpdate() > 0;
        session.getTransaction().commit();
        session.close();
        return rsl;
    }

    @Override
    public boolean delete(int id) {
        Session session = sf.openSession();
        session.beginTransaction();
        boolean rsl = session.createQuery(
                        "delete from Item where id = :fId"
                ).setParameter("fId", id)
                .executeUpdate() > 0;
        session.getTransaction().commit();
        session.close();
        return rsl;
    }

    public void deleteAll() {
        Session session = sf.openSession();
        session.beginTransaction();
        session.createQuery("delete from Item").executeUpdate();
        session.getTransaction().commit();
        session.close();
    }

    @Override
    public List<Item> findAll() {
        Session session = sf.openSession();
        session.beginTransaction();
        List<Item> rsl = session.createQuery("from Item", Item.class).list();
        session.getTransaction().commit();
        session.close();
        return rsl;
    }

    @Override
    public List<Item> findByKeyInName(String key) {
        Session session = sf.openSession();
        session.beginTransaction();
        List<Item> rsl = session.createQuery(
                        "from Item where name like :key order by created", Item.class)
                .setParameter("key", "%" + key + "%")
                .list();
        session.getTransaction().commit();
        session.close();
        return rsl;
    }

    @Override
    public Item findById(int id) {
        Session session = sf.openSession();
        session.beginTransaction();
        Item rsl = session.createQuery("from Item where id = :fId", Item.class)
                        .setParameter("fId", id)
                        .uniqueResult();
        session.getTransaction().commit();
        session.close();
        return rsl;
    }

    @Override
    public void close() {
        StandardServiceRegistryBuilder.destroy(registry);
    }
}
