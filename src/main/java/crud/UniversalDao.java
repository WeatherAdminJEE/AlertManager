package crud;

import crud.facade.IUniversalDao;
import imt.org.web.commonmodel.entities.SensorAlertEntity;
import imt.org.web.commonmodel.entities.SensorDataEntity;
import imt.org.web.commonmodel.entities.SensorEntity;
import main.Main;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Generic CRUD facade implementation
 * @param <T> Entity type
 */
public class UniversalDao<T> implements IUniversalDao<T> {

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("WeatherDatabase");

    /**
     * Insert object
     * @param entity Entity
     */
    @Override
    public void create(final T entity) {
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        EntityTransaction transaction = null;

        try {
            transaction = manager.getTransaction();
            transaction.begin();
            Main.log.debug("CRUD facade - create() - Begin transaction");

            manager.persist(entity);
            transaction.commit();
            Main.log.debug("CRUD facade - create() - Transaction success");
        } catch (PersistenceException hibernateEx) {
            Main.log.error("CRUD facade - create() - Insert error - " + hibernateEx.getMessage());
            if (transaction != null) {
                transaction.rollback();
                Main.log.debug("CRUD facade - create() - Action rollback !\n" + hibernateEx.getMessage());
            }
        } finally {
            manager.close();
            Main.log.debug("CRUD facade - create() - EntityManager closed");
        }
    }

    /**
     * Select object
     * @param entity Entity class
     * @param primaryKey PrK
     * @return Requested object
     */
    @Override
    public T findByPrimaryKey(final Class<T> entity, final Serializable primaryKey) {
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        T entities = null;

        try {
            Main.log.debug("CRUD facade - findByPrimaryKey() - Begin findByPrimaryKey");
            entities = manager.find(entity, primaryKey);
            Main.log.debug("CRUD facade - findByPrimaryKey() - Read success");
        } catch (PersistenceException hibernateEx) {
            Main.log.debug("CRUD facade - findByPrimaryKey() - Read error - " + hibernateEx.getMessage());

        } finally {
            manager.close();
            Main.log.debug("CRUD facade - findByPrimaryKey() - EntityManager closed");
            return entities;
        }
    }


    public Collection<SensorEntity> getAllSensor (){
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        Collection<SensorEntity> lstSensor = new ArrayList<>();
        try {
            Main.log.info("getAllSensor début "+ new Date(System.currentTimeMillis()));
            lstSensor = manager.createQuery("Select s from SensorEntity s").getResultList();
            Main.log.info("getAllSensor terminée "+new Date(System.currentTimeMillis()));
        }catch (Exception e){
            Main.log.error("getAllSensor error "+e);
        }finally {
            manager.close();
        }
        return lstSensor;
    }

    /**
     *
     * @param sensor sensor
     * @return les données insérées il y a moins d'une heure par le capteur donné
     */
    public List<SensorDataEntity> getDataBySensor(SensorEntity sensor){
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        List<SensorDataEntity> lstSensorData = new ArrayList<>();
        StringBuilder sb = new StringBuilder("Select sd from SensorDataEntity sd where ");
        sb.append("sd.sensor = :sensor");
        sb.append(" and ");
        sb.append("sd.date > :date");
        sb.append(" order by sd.date ");
        try {
            Query query = manager.createQuery(sb.toString());
            query.setParameter("sensor", sensor);
            query.setParameter("date", new Date(System.currentTimeMillis() - 1000 * 60 * 60));
            lstSensorData = query.getResultList();
            Main.log.debug("List des data récupérée avec succes pour le capteur "+sensor.getNameSensor());
        } catch (Exception e){
            Main.log.error("getDataBySensor error : "+e);
        }finally {
            manager.close();
        }
        return lstSensorData;
    }


    public SensorAlertEntity getLastAlertBySensor(SensorEntity sensor){
        EntityManager manager = ENTITY_MANAGER_FACTORY.createEntityManager();
        SensorAlertEntity lastAlert = null;
        String s = "Select sae from SensorAlertEntity sae where sae.sensor = :sensor order by sae.startDate desc";
        try{
            Query query = manager.createQuery(s);
            query.setParameter("sensor",sensor);
            lastAlert = (SensorAlertEntity) query.getResultList().get(0);
        }catch (IndexOutOfBoundsException e){
            Main.log.info("Pas d'alert pour le capteur "+sensor.getNameSensor());
        } catch (Exception e){
            Main.log.error("getLastAlertBySensor error : "+e);
        }finally {
            manager.close();
        }
        return lastAlert;
    }


}