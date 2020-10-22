package Util;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class ConnectionUtil {
    static Logger logger = Logger.getLogger(ConnectionUtil.class.getName());
    private static StandardServiceRegistry standardServiceRegistry;
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                standardServiceRegistry = new StandardServiceRegistryBuilder().configure().build();
                MetadataSources sources = new MetadataSources(standardServiceRegistry);
                Metadata metadata = sources.getMetadataBuilder().build();
                sessionFactory = metadata.getSessionFactoryBuilder().build();

            } catch (Exception ex) {
                logger.error("Exception while setting up session factory", ex);
            }
        }
        return sessionFactory;
    }
}
